package com.portfolio.management;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

/**
 * Main application class for the Portfolio Suggestions API
 * Built with Clean Architecture and Hexagonal patterns, using Quarkus Reactive
 */
@QuarkusMain
@OpenAPIDefinition(
    info = @Info(
        title = "Portfolio Suggestions API",
        version = "1.0.0",
        description = "Reactive API for suggesting company tickers/symbols for portfolio management applications. " +
                     "Built with Clean Architecture and Hexagonal patterns for scalability and maintainability.",
        contact = @Contact(
            name = "Portfolio Management Team",
            email = "support@portfolio-management.com"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    )
)
public class SuggestionsApplication extends Application {

    public static void main(String... args) {
        System.out.println("Starting Portfolio Suggestions API (Reactive + Clean Architecture)...");
        Quarkus.run(args);
    }
}
