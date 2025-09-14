package com.portfolio.management.infrastructure.adapters.web;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@QuarkusTest
public class SuggestionsResourceTest {

    @Test
    public void testGetSuggestions_ValidQuery() {
        given()
                .when()
                .queryParam("q", "apple")
                .queryParam("limit", 5)
                .get("/suggestions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("query", equalTo("apple"))
                .body("count", greaterThan(0))
                .body("suggestions.size()", greaterThan(0))
                .body("suggestions[0].symbol", notNullValue())
                .body("suggestions[0].name", notNullValue());
    }

    @Test
    public void testGetSuggestions_EmptyQuery() {
        given()
                .when()
                .queryParam("q", "")
                .get("/suggestions")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("title", containsString("Constraint Violation"))
                .body("status", equalTo(400))
                .body("violations.size()", greaterThan(0));
    }

    @Test
    public void testGetSuggestions_NoQuery() {
        given()
                .when()
                .get("/suggestions")
                .then()
                .statusCode(400);
    }

    @Test
    public void testGetSuggestions_InvalidLimit() {
        given()
                .when()
                .queryParam("q", "test")
                .queryParam("limit", 100)
                .get("/suggestions")
                .then()
                .statusCode(400);
    }

    @Test
    public void testGetSuggestions_SymbolSearch() {
        given()
                .when()
                .queryParam("q", "MSFT")
                .get("/suggestions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("suggestions.size()", greaterThan(0))
                .body("suggestions[0].symbol", equalTo("MSFT"));
    }

    @Test
    public void testGetSuggestions_PartialNameSearch() {
        given()
                .when()
                .queryParam("q", "micro")
                .get("/suggestions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("suggestions.size()", greaterThan(0));
    }

    @Test
    public void testGetSuggestions_CustomLimit() {
        given()
                .when()
                .queryParam("q", "a")
                .queryParam("limit", 3)
                .get("/suggestions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", lessThanOrEqualTo(3))
                .body("suggestions.size()", lessThanOrEqualTo(3));
    }

    @Test
    public void testGetSuggestions_NoResults() {
        given()
                .when()
                .queryParam("q", "NONEXISTENTSTOCK123")
                .get("/suggestions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(0))
                .body("suggestions.size()", equalTo(0));
    }

    @Test
    public void testGetSuggestions_DefaultLimit() {
        given()
                .when()
                .queryParam("q", "a")
                .get("/suggestions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", greaterThanOrEqualTo(0))
                .body("suggestions.size()", lessThanOrEqualTo(10)); // Default limit
    }

    @Test
    public void testGetSuggestions_ResponseStructure() {
        given()
                .when()
                .queryParam("q", "Apple")
                .get("/suggestions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("query", notNullValue())
                .body("count", notNullValue())
                .body("suggestions", notNullValue())
                .body("suggestions[0].symbol", notNullValue())
                .body("suggestions[0].name", notNullValue())
                .body("suggestions[0].exchange", notNullValue());
    }
}
