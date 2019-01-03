/**
 * Copyright 2015-2018 Proemion GmbH
 */
package hellocucumber;

import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.api.event.WriteEvent;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import cucumber.runtime.Argument;
import cucumber.runtime.formatter.Format;
import cucumber.runtime.formatter.Formats;
import cucumber.runtime.formatter.MonochromeFormats;
import cucumber.util.FixJava;
import cucumber.util.Mapper;
import gherkin.ast.Examples;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.Tag;
import gherkin.pickles.PickleTag;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Add Javadoc.
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 * @version $Id$
 * @since 10.10
 */
public class HttpFormatter implements Formatter {
    private static final String SCENARIO_INDENT = "#  ";
    private static final String STEP_INDENT = "#    ";
    private static final String EXAMPLES_INDENT = "#    ";
    private static final String ERROR_INDENT = "#       ";
    private final NiceAppendable out;
    private Formats formats;
    private String currentFeatureFile;
    private TestCase currentTestCase;
    private ScenarioOutline currentScenarioOutline;
    private Examples currentExamples;
    private int locationIndentation;
    private Mapper<Tag, String> tagNameMapper = new Mapper<Tag, String>() {
        public String map(Tag tag) {
            return tag.getName();
        }
    };
    private Mapper<PickleTag, String> pickleTagNameMapper = new Mapper<PickleTag, String>() {
        public String map(PickleTag pickleTag) {
            return pickleTag.getName();
        }
    };
    private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        public void receive(TestSourceRead event) {
            HttpFormatter.this.handleTestSourceRead(event);
        }
    };
    private EventHandler<TestCaseStarted> caseStartedHandler = new EventHandler<TestCaseStarted>() {
        public void receive(TestCaseStarted event) {
            HttpFormatter.this.handleTestCaseStarted(event);
        }
    };
    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        public void receive(TestStepStarted event) {
            HttpFormatter.this.handleTestStepStarted(event);
        }
    };
    private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        public void receive(TestStepFinished event) {
            HttpFormatter.this.handleTestStepFinished(event);
        }
    };
    private EventHandler<WriteEvent> writeEventhandler = new EventHandler<WriteEvent>() {
        public void receive(WriteEvent event) {
            HttpFormatter.this.handleWrite(event);
        }
    };
    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        public void receive(TestRunFinished event) {
            HttpFormatter.this.finishReport();
        }
    };

    public HttpFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
        this.formats = new MonochromeFormats();
    }

    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this.testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, this.caseStartedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, this.stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, this.stepFinishedHandler);
        publisher.registerHandlerFor(WriteEvent.class, this.writeEventhandler);
        publisher.registerHandlerFor(TestRunFinished.class, this.runFinishedHandler);
    }

    private void handleTestSourceRead(TestSourceRead event) {
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        this.handleStartOfFeature(event);
        this.handleScenarioOutline(event);
        this.printScenarioDefinition(event.testCase);
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (!event.testStep.isHook() && this.isFirstStepAfterBackground(event.testStep)) {
            this.printScenarioDefinition(this.currentTestCase);
            this.currentTestCase = null;
        }

    }

    private void handleTestStepFinished(TestStepFinished event) {
        TestStep testStep = event.testStep;
        if (!testStep.isHook()) {
            this.printStep(testStep, event.result);
        }

        this.printError(event.result);
    }

    private void handleWrite(WriteEvent event) {
        this.out.println(event.text);
    }

    private void finishReport() {
        this.out.close();
    }

    private void handleStartOfFeature(TestCaseStarted event) {
        if (this.currentFeatureFile == null || !this.currentFeatureFile.equals(event.testCase.getUri())) {
            if (this.currentFeatureFile != null) {
                this.out.println();
            }

            this.currentFeatureFile = event.testCase.getUri();
            this.printFeature(this.currentFeatureFile);
        }

    }

    private void handleScenarioOutline(TestCaseStarted event) {
        this.currentScenarioOutline = null;
        this.currentExamples = null;
    }

    private void printScenarioOutline(ScenarioOutline scenarioOutline) {
        this.out.println();
        this.printTags(scenarioOutline.getTags(), "  ");
        this.out.println(SCENARIO_INDENT + this.getScenarioDefinitionText(scenarioOutline) + " " + this.getLocationText(this.currentFeatureFile, scenarioOutline.getLocation().getLine()));
        this.printDescription(scenarioOutline.getDescription());
        Iterator var2 = scenarioOutline.getSteps().iterator();

        while(var2.hasNext()) {
            Step step = (Step)var2.next();
            this.out.println(STEP_INDENT + this.formats.get("skipped").text(step.getKeyword() + step.getText()));
        }

    }

    private void printExamples(Examples examples) {
        this.out.println();
        this.printTags(examples.getTags(), "    ");
        this.out.println(EXAMPLES_INDENT + examples.getKeyword() + ": " + examples.getName());
        this.printDescription(examples.getDescription());
    }

    private void printStep(TestStep testStep, Result result) {
        String keyword = this.getStepKeyword(testStep);
        String stepText = testStep.getStepText();
        String locationPadding = this.createPaddingToLocation("    ", keyword + stepText);
        String formattedStepText = this.formatStepText(keyword, stepText, this.formats.get(result.getStatus().lowerCaseName()), this.formats.get(result.getStatus().lowerCaseName() + "_arg"), testStep.getDefinitionArgument());
        this.out.println(STEP_INDENT + formattedStepText + locationPadding + this.getLocationText(testStep.getCodeLocation()));
    }

    String formatStepText(String keyword, String stepText, Format textFormat, Format argFormat, List<Argument> arguments) {
        int beginIndex = 0;
        StringBuilder result = new StringBuilder(textFormat.text(keyword));
        Iterator var8 = arguments.iterator();

        while(true) {
            Argument argument;
            while(true) {
                if (!var8.hasNext()) {
                    if (beginIndex != stepText.length()) {
                        String text = stepText.substring(beginIndex, stepText.length());
                        result.append(textFormat.text(text));
                    }

                    return result.toString();
                }

                argument = (Argument)var8.next();
                if (argument.getOffset() == null) {
                    break;
                }

                int argumentOffset = argument.getOffset();
                if (argumentOffset >= beginIndex) {
                    String text = stepText.substring(beginIndex, argumentOffset);
                    result.append(textFormat.text(text));
                    break;
                }
            }

            if (argument.getVal() != null) {
                result.append(argFormat.text(argument.getVal()));
                beginIndex = argument.getOffset() + argument.getVal().length();
            }
        }
    }

    private String getScenarioDefinitionText(ScenarioDefinition definition) {
        return definition.getKeyword() + ": " + definition.getName();
    }

    private String getLocationText(String file, int line) {
        return this.getLocationText(file + ":" + line);
    }

    private String getLocationText(String location) {
        return this.formats.get("comment").text(" # " + location);
    }

    private StringBuffer stepText(TestStep testStep) {
        String keyword = this.getStepKeyword(testStep);
        return new StringBuffer(keyword + testStep.getStepText());
    }

    private String getStepKeyword(TestStep testStep) {
        return "";
    }

    private boolean isFirstStepAfterBackground(TestStep testStep) {
        return this.currentTestCase != null && !this.isBackgroundStep(testStep);
    }

    private boolean isBackgroundStep(TestStep testStep) {
        return false;
    }

    private void printFeature(String path) {
//        Feature feature = this.testSources.getFeature(path);
//        this.printTags(feature.getTags());
//        this.out.println(feature.getKeyword() + ": " + feature.getName());
//        this.printDescription(feature.getDescription());
    }

    private void printTags(List<Tag> tags) {
        this.printTags(tags, "");
    }

    private void printTags(List<Tag> tags, String indent) {
        if (!tags.isEmpty()) {
            this.out.println(indent + FixJava.join(FixJava.map(tags, this.tagNameMapper), " "));
        }

    }

    private void printPickleTags(List<PickleTag> tags, String indent) {
        if (!tags.isEmpty()) {
            this.out.println(indent + FixJava.join(FixJava.map(tags, this.pickleTagNameMapper), " "));
        }

    }

    private void printDescription(String description) {
        if (description != null) {
            this.out.println(description);
        }

    }

    private void printBackground(TestCase testCase) {
//        TestSourcesModel.AstNode astNode = this.testSources.getAstNode(this.currentFeatureFile, testCase.getLine());
//        if (astNode != null) {
//            Background background = TestSourcesModel.getBackgroundForTestCase(astNode);
//            String backgroundText = this.getScenarioDefinitionText(background);
//            boolean useBackgroundSteps = true;
//            this.calculateLocationIndentation("  " + backgroundText, testCase.getTestSteps(), useBackgroundSteps);
//            String locationPadding = this.createPaddingToLocation("  ", backgroundText);
//            this.out.println();
//            this.out.println("  " + backgroundText + locationPadding + this.getLocationText(this.currentFeatureFile, background.getLocation().getLine()));
//            this.printDescription(background.getDescription());
//        }

    }

    private void printScenarioDefinition(TestCase testCase) {
//        ScenarioDefinition scenarioDefinition = this.testSources.getScenarioDefinition(this.currentFeatureFile, testCase.getLine());
//        String definitionText = scenarioDefinition.getKeyword() + ": " + testCase.getName();
//        this.calculateLocationIndentation("  " + definitionText, testCase.getTestSteps());
//        String locationPadding = this.createPaddingToLocation("  ", definitionText);
//        this.out.println();
//        this.printPickleTags(testCase.getTags(), "  ");
//        this.out.println("  " + definitionText + locationPadding + this.getLocationText(this.currentFeatureFile, testCase.getLine()));
//        this.printDescription(scenarioDefinition.getDescription());
    }

    private void printError(Result result) {
        if (result.getError() != null) {
            this.out.println(
                ERROR_INDENT + result.getErrorMessage()
            );
        }

    }

    private void calculateLocationIndentation(String definitionText, List<TestStep> testSteps) {
        boolean useBackgroundSteps = false;
        this.calculateLocationIndentation(definitionText, testSteps, useBackgroundSteps);
    }

    private void calculateLocationIndentation(String definitionText, List<TestStep> testSteps, boolean useBackgroundSteps) {
        int maxTextLength = definitionText.length();
        Iterator var5 = testSteps.iterator();

        while(var5.hasNext()) {
            TestStep step = (TestStep)var5.next();
            if (!step.isHook() && this.isBackgroundStep(step) == useBackgroundSteps) {
                StringBuffer stepText = this.stepText(step);
                maxTextLength = Math.max(maxTextLength, "    ".length() + stepText.length());
            }
        }

        this.locationIndentation = maxTextLength + 1;
    }

    private String createPaddingToLocation(String indent, String text) {
        StringBuffer padding = new StringBuffer();

        for(int i = indent.length() + text.length(); i < this.locationIndentation; ++i) {
            padding.append(' ');
        }

        return padding.toString();
    }
}
