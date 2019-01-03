/**
 * Copyright 2015-2018 Proemion GmbH
 */
package hellocucumber;

import com.google.common.base.Strings;
import cucumber.api.formatter.Formatter;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import java.util.Arrays;

/**
 * TODO: Add Javadoc.
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 * @version $Id$
 * @since 10.10
 */
public class Http extends AbstractVerticle {
    private static final ResourceLoader resourceLoader = new MultiLoader(Http.class.getClassLoader());

    @Override
    public void start() throws Exception {
        final HttpServer server = this.vertx.createHttpServer();

        final Router router = Router.router(this.vertx);

        router.route("/metrics").handler(routingContext -> {
           StringBuffer result = new StringBuffer(1024);
            final Runtime runtime = Http.cucumber(
                new HttpFormatter(result)
            );
            runtime.runFeature(
                CucumberFeature.load(
                    resourceLoader,
                    Arrays.asList(
                        String.format(
                            "src/test/resources/hellocucumber/%s.feature",
                            routingContext.request().getParam("feature")
                        )
                    )
                ).get(0)
            );
            // This handler will be called for every request
            final HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain; charset=utf-8");
            // Write to the response and end it
            if (Strings.isNullOrEmpty(routingContext.request().getParam("verbose"))) {
                response.end(
                    String.format(
                        "friday.success %d%n",
                        runtime.exitStatus()
                    )
                );
            } else {
                response.end(
                    String.format(
                        "friday.success %d%n%s",
                        runtime.exitStatus(),
                        result.toString()
                    )
                );
            }
        });

        server.requestHandler(router).listen(8080);
    }

    public static void main(final String... args) {
        Vertx.vertx().deployVerticle(Http.class.getName());
    }

    private static Runtime cucumber(final Formatter plugin) {
        RuntimeOptions runtimeOptions = new RuntimeOptionsFactory(Http.class)
            .create();
        ClassFinder classFinder = new ResourceLoaderClassFinder(
            resourceLoader, Http.class.getClassLoader()
        );
        Runtime runtime = new Runtime(
            resourceLoader,
            classFinder,
            Http.class.getClassLoader(),
            runtimeOptions
        );
        // must happen after runtime has been created or the plugin
        // will not be activated (only added)
        runtimeOptions.addPlugin(plugin);
        return runtime;
    }
}
