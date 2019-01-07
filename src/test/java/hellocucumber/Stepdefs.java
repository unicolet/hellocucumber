package hellocucumber;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.Optional;
import static org.junit.Assert.assertEquals;

public class Stepdefs implements InjectableRegistry {
    private String today;
    private String actualAnswer;
    private final DayOfWeek dow;
    private PrometheusMeterRegistry registry;

    // to test picocontainer IOC
    public Stepdefs(final DayOfWeek dow) {
        this.dow = dow;
    }

    @Given("^today is \"([^\"]*)\"$")
    public void today_is(String today) {
        this.today = today;
    }

    @When("^I ask whether it's Friday yet$")
    public void i_ask_whether_is_s_Friday_yet() {
        this.actualAnswer = this.dow.isItFriday(today);
    }

    @Then("^I should be told \"([^\"]*)\"$")
    public void i_should_be_told(String expectedAnswer) {
        assertEquals(expectedAnswer, this.actualAnswer);
    }

    @Override
    public void injectRegistry(final Optional<PrometheusMeterRegistry> injectable) {
        injectable.ifPresent(meterRegistry -> {
            this.registry = meterRegistry;
            this.registry.counter("some.counter").increment();
            System.out.println("Registry injected");
        });
    }
}
