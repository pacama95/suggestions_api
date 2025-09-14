# Portfolio Suggestions API

A reactive Quarkus-based REST API for providing ticker/symbol suggestions for portfolio management applications, built with Clean Architecture and Hexagonal Architecture patterns.

## Architecture

This project follows **Clean Architecture** and **Hexagonal Architecture** principles:

### **Domain Layer** (`domain/`)
- **Models**: Core business entities (`TickerSuggestion`, `Errors`)
- **Use Cases**: Business logic interfaces (`GetSuggestionsUseCase`)
- **Ports**: Interfaces for external dependencies (`SuggestionRepository`)

### **Application Layer** (`application/`)
- **Services**: Use case implementations (`SuggestionService`)
- Business logic orchestration and error handling

### **Infrastructure Layer** (`infrastructure/`)
- **Web Adapters**: REST API endpoints (`SuggestionsResource`)
- **Repository Adapters**: Data access implementations (`InMemorySuggestionRepository`)
- **DTOs**: Data transfer objects with `@RegisterForReflection` for native compilation

## Features

- **Reactive Programming**: Built with Quarkus Reactive using Mutiny (`Uni`/`Multi`)
- **Clean Architecture**: Domain-driven design with clear separation of concerns
- **Single Endpoint**: Optimized `/suggestions` endpoint for frontend typeahead functionality
- **Native Compilation Ready**: All DTOs annotated with `@RegisterForReflection`
- **Error Handling**: Comprehensive validation and business error handling
- **OpenAPI Documentation**: Automatic API documentation generation
- **Sample Data**: Pre-loaded with popular stock symbols for immediate testing

## Technology Stack

- **Java 21**: Latest LTS version of Java
- **Quarkus 3.14.2**: Reactive, Kubernetes-native Java framework
- **Mutiny**: Reactive programming library for asynchronous data flows
- **Jakarta RESTful Web Services**: For REST API endpoints
- **Jackson**: JSON serialization/deserialization
- **SmallRye OpenAPI**: API documentation generation
- **Hibernate Validator**: Request validation
- **Gradle**: Dependency management and build tool

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.10 or higher (or use the included wrapper)

### Running the Application

1. **Development Mode** (with live reload):
```bash
./gradlew quarkusDev
```

2. **Production Mode**:
```bash
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

3. **Native Executable** (requires GraalVM):
```bash
./gradlew build -Dquarkus.package.type=native
./build/suggestions-api-1.0.0-runner
```

4. **Development with Live Reload**:
```bash
./gradlew quarkusDev
```

The application will start on `http://localhost:8080`

### API Documentation

Once the application is running, you can access:
- **OpenAPI/Swagger UI**: http://localhost:8080/q/swagger-ui/
- **OpenAPI Specification**: http://localhost:8080/q/openapi

## API Endpoints

### **Get Suggestions** (Main Endpoint)
Search for ticker suggestions based on user input - optimized for frontend typeahead:

```http
GET /suggestions?q={query}&limit={limit}
```

**Parameters:**
- `q` (required): Search query (ticker symbol or company name)
- `limit` (optional): Maximum results to return (1-50, default: 10)

**Example:**
```bash
curl "http://localhost:8080/suggestions?q=apple&limit=5"
```

**Response:**
```json
{
  "suggestions": [
    {
      "symbol": "AAPL",
      "name": "Apple Inc.",
      "exchange": "NASDAQ",
      "type": "Common Stock",
      "region": "US",
      "marketCap": "Large Cap",
      "currency": "USD"
    }
  ],
  "query": "apple",
  "count": 1
}
```

## Docker Deployment

The API provides both JVM and Native compilation modes:

### **Native Image** (Recommended for Production)
```bash
# Build native image
docker build -f Dockerfile.native -t suggestions-api:native .

# Run native container
docker run -i --rm -p 8080:8080 suggestions-api:native
```

### **JVM Mode** (Faster builds for Development)
```bash
# First build the application
./gradlew build

# Build JVM image
docker build -f Dockerfile.jvm -t suggestions-api:jvm .

# Run JVM container
docker run -i --rm -p 8080:8080 suggestions-api:jvm
```

## Deployment Considerations

### **For Frontend Integration**
The API is optimized for frontend typeahead/autocomplete functionality:
- **HTTP Endpoint**: Simple HTTP GET requests work well for most use cases
- **Low Latency**: Reactive architecture ensures fast response times
- **Caching**: Consider adding HTTP caching headers for repeated queries
- **Rate Limiting**: Consider implementing rate limiting for production use

### **Future Streaming Option**
For real-time updates, Server-Sent Events (SSE) could be implemented:
```java
@GET
@Path("/suggestions/stream")
@Produces(MediaType.SERVER_SENT_EVENTS)
public Multi<SuggestionsResponse> streamSuggestions(@QueryParam("q") String query)
```

## Sample Data

The API comes pre-loaded with sample data including:
- Major tech stocks (AAPL, MSFT, GOOGL, AMZN, etc.)
- Financial sector stocks (JPM, BAC, WFC, etc.)
- Healthcare stocks (JNJ, PFE, UNH, etc.)
- Consumer goods (KO, PEP, WMT, etc.)
- ETFs (SPY, QQQ, VTI)

## Testing

Run the test suite:

```bash
./gradlew test
```

## Configuration

The application can be configured through `src/main/resources/application.properties`:

```properties
# HTTP configuration
quarkus.http.port=8080
quarkus.http.cors=true

# OpenAPI configuration
quarkus.smallrye-openapi.info-title=Portfolio Suggestions API
quarkus.smallrye-openapi.info-version=1.0.0

# Logging configuration
quarkus.log.level=INFO
```

## Project Structure

```
src/main/java/com/portfolio/management/
├── domain/
│   ├── model/
│   │   ├── TickerSuggestion.java
│   │   └── Errors.java
│   └── port/
│       ├── incoming/
│       │   └── GetSuggestionsUseCase.java
│       └── outgoing/
│           └── SuggestionRepository.java
├── application/
│   └── service/
│       └── SuggestionService.java
├── infrastructure/
│   └── adapters/
│       ├── web/
│       │   ├── SuggestionsResource.java
│       │   └── dto/
│       │       ├── SuggestionsRequest.java
│       │       ├── SuggestionsResponse.java
│       │       └── ErrorResponse.java
│       └── repository/
│           └── InMemorySuggestionRepository.java
└── SuggestionsApplication.java
```

## Error Handling

The API provides comprehensive error handling with meaningful HTTP status codes:

- `200 OK`: Successful request
- `400 Bad Request`: Invalid request parameters or validation errors
- `500 Internal Server Error`: System errors

Error responses include descriptive messages:

```json
{
  "errors": [
    {
      "field": "input",
      "message": "Search input cannot be empty",
      "code": "EMPTY_INPUT"
    }
  ],
  "message": "Validation failed",
  "timestamp": 1694771234567
}
```

## Future Enhancements

- Integration with real-time market data APIs
- Database persistence for ticker data
- User authentication and rate limiting
- Advanced search filters (by sector, market cap, etc.)
- Caching for improved performance
- Server-Sent Events for real-time suggestions
- Kubernetes deployment configurations

## License

© 2025 Pablo Cazorla. All rights reserved.
This code is proprietary and confidential.