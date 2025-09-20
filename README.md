# Portfolio Management API

A comprehensive reactive Quarkus-based REST API for portfolio management applications, featuring stock suggestions, market data integration, and financial instrument management. Built with Clean Architecture and Hexagonal Architecture patterns for enterprise-grade scalability and maintainability.

## Architecture

This project follows **Clean Architecture** and **Hexagonal Architecture** principles with complete separation of concerns:

### **Domain Layer** (`domain/`)
- **Models**: Core business entities (`Stock`, `Currency`, `Exchange`, `StockType`, `Errors`)
- **Use Cases**: Business logic interfaces for all operations (stocks, currencies, exchanges, stock types)
- **Ports**: Interfaces for external dependencies (repositories, market data providers)

### **Application Layer** (`application/`)
- **Services**: Use case implementations with comprehensive business logic
- **Data Processing**: Batch processing, validation, and error handling
- **External Integration**: Market data fetching and storage orchestration

### **Infrastructure Layer** (`infrastructure/`)
- **Web Adapters**: REST API endpoints with comprehensive OpenAPI documentation
- **Repository Adapters**: PostgreSQL database access with reactive Panache
- **External Clients**: TwelveData API integration for real-time market data
- **Persistence**: Liquibase database migrations and entity management
- **DTOs & Mappers**: MapStruct-powered data transfer objects with `@RegisterForReflection`

## Features

- **Reactive Programming**: Built with Quarkus Reactive using Mutiny (`Uni`/`Multi`) for non-blocking operations
- **Clean Architecture**: Domain-driven design with clear separation of concerns and SOLID principles
- **Comprehensive API**: Multiple endpoints for stocks, currencies, exchanges, and stock types
- **Database Persistence**: PostgreSQL with Hibernate Reactive Panache and Liquibase migrations
- **Market Data Integration**: Real-time stock data from TwelveData API
- **Advanced Search**: Sophisticated stock search with multiple criteria
- **Caching System**: Caffeine-based caching for optimal performance
- **Batch Processing**: Efficient bulk operations for large datasets
- **Admin Operations**: Administrative endpoints for data management and imports
- **Native Compilation Ready**: All DTOs annotated with `@RegisterForReflection`
- **Error Handling**: Comprehensive validation and business error handling
- **OpenAPI Documentation**: Complete API documentation with examples
- **Docker Ready**: Docker Compose setup with PostgreSQL and Adminer
- **Test Coverage**: Comprehensive unit and integration tests

## Technology Stack

- **Java 21**: Latest LTS version of Java
- **Quarkus 3.14.2**: Reactive, Kubernetes-native Java framework
- **Mutiny**: Reactive programming library for asynchronous data flows
- **PostgreSQL 15**: Production-ready relational database
- **Hibernate Reactive**: Non-blocking database access with Panache
- **Liquibase**: Database schema versioning and migrations
- **TwelveData API**: Real-time market data provider
- **Caffeine**: High-performance in-memory caching
- **MapStruct**: Type-safe bean mapping
- **Jakarta RESTful Web Services**: For REST API endpoints
- **Jackson**: JSON serialization/deserialization
- **SmallRye OpenAPI**: API documentation generation
- **Hibernate Validator**: Request validation
- **Docker & Docker Compose**: Containerization and development environment
- **Gradle**: Dependency management and build tool

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Gradle 8.10** or higher (or use the included wrapper)
- **Docker & Docker Compose** (for database setup)
- **TwelveData API Key** (for market data integration)

### Quick Start

1. **Start the database services**:
```bash
docker-compose up -d
```

2. **Set up environment variables**:
```bash
export TWELVE_DATA_API_KEY="your-api-key-here"
# Optional: customize database connection
export PGHOST=localhost
export PGPORT=5434
export PGUSER=postgres
export PGPASSWORD=suggestions_pass
export PGDATABASE=suggestions_db
```

3. **Run the application in development mode**:
```bash
./gradlew quarkusDev
```

The application will start on `http://localhost:8090` with live reload enabled.

### Running Options

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

### Access Points

Once the application is running, you can access:
- **API Base URL**: http://localhost:8090
- **OpenAPI/Swagger UI**: http://localhost:8090/q/swagger-ui/
- **OpenAPI Specification**: http://localhost:8090/q/openapi
- **Database Admin (Adminer)**: http://localhost:8084 (Server: suggestions-postgresql, User: postgres, Password: suggestions_pass, Database: suggestions_db)

## API Endpoints

### **Stock Suggestions**

#### Basic Stock Search
Search for stock ticker suggestions - optimized for frontend typeahead:

```http
GET /suggestions?q={query}&limit={limit}
```

**Parameters:**
- `q` (required): Search query (ticker symbol or company name)
- `limit` (optional): Maximum results to return (1-50, default: 10)

**Example:**
```bash
curl "http://localhost:8090/suggestions?q=apple&limit=5"
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
      "country": "United States",
      "currency": "USD",
      "micCode": "XNAS",
      "figiCode": "BBG000B9XRY4"
    }
  ],
  "query": "apple",
  "count": 1
}
```

#### Advanced Stock Search
Advanced search with multiple criteria:

```http
GET /v1/suggestions/advanced?symbol={symbol}&companyName={name}&exchange={exchange}&region={region}&currency={currency}&limit={limit}
```

**Parameters:**
- `symbol` (optional): Stock symbol filter
- `companyName` (optional): Company name filter
- `exchange` (optional): Exchange code filter
- `region` (optional): Region/country filter
- `currency` (optional): Currency code filter
- `limit` (optional): Maximum results (1-100, default: 20)

**Example:**
```bash
curl "http://localhost:8090/v1/suggestions/advanced?exchange=NYSE&currency=USD&limit=10"
```

### **Currencies**

#### Get All Currencies
```http
GET /v1/currencies?q={search}&limit={limit}
```

**Parameters:**
- `q` (optional): Search query for currency code or name
- `limit` (optional): Maximum results (1-100, default: 50)

**Example Response:**
```json
{
  "currencies": [
    {
      "id": 1,
      "code": "USD",
      "name": "United States Dollar",
      "symbol": "$",
      "countryCode": "US",
      "active": true
    }
  ],
  "count": 1
}
```

### **Stock Exchanges**

#### Get All Exchanges
```http
GET /v1/exchanges?q={search}&limit={limit}
```

**Parameters:**
- `q` (optional): Search query for exchange code or name
- `limit` (optional): Maximum results (1-100, default: 50)

**Example Response:**
```json
{
  "exchanges": [
    {
      "id": 1,
      "code": "NYSE",
      "name": "New York Stock Exchange",
      "country": "United States",
      "timezone": "America/New_York",
      "currencyCode": "USD",
      "active": true
    }
  ],
  "count": 1
}
```

### **Stock Types**

#### Get All Stock Types
```http
GET /v1/stock-types?q={search}&limit={limit}
```

**Parameters:**
- `q` (optional): Search query for stock type code or name
- `limit` (optional): Maximum results (1-100, default: 50)

**Example Response:**
```json
{
  "stockTypes": [
    {
      "id": 1,
      "code": "CS",
      "name": "Common Stock",
      "description": "Ordinary shares that represent ownership in a company",
      "active": true
    }
  ],
  "count": 1
}
```

### **Admin Operations**

#### Fetch and Store Stock Data
Administrative endpoint to populate the database with market data:

```http
POST /admin/fetch-stocks
```

**Description:** Fetches all available stocks from TwelveData API and stores them in the database. This operation populates/refreshes the stock data used by the suggestions endpoints.

**Response:**
```json
{
  "success": true,
  "message": "Successfully fetched and stored 5000 stocks from TwelveData API",
  "recordsProcessed": 5000
}
```

**Note:** This operation requires a valid TwelveData API key configured in the application.

## Database Schema

The application uses PostgreSQL with Liquibase for schema management. The database contains the following main tables:

### **Core Tables**

#### `stocks`
- **Purpose**: Stores stock/ticker information from TwelveData API
- **Key Fields**: `symbol`, `name`, `currency`, `exchange`, `country`, `type`, `mic_code`, `figi_code`, `cfi_code`, `isin`, `cusip`
- **Indexes**: Optimized for symbol, name, exchange, and country searches
- **Features**: Active status tracking, data versioning, audit timestamps

#### `currencies`
- **Purpose**: Stores currency information (USD, EUR, JPY, etc.)
- **Key Fields**: `code`, `name`, `symbol`, `country_code`, `is_active`
- **Indexes**: Optimized for code, country, and active status searches

#### `exchanges`
- **Purpose**: Stores stock exchange information (NYSE, NASDAQ, LSE, etc.)
- **Key Fields**: `code`, `name`, `country`, `timezone`, `currency_code`, `is_active`
- **Indexes**: Optimized for code, country, currency, and active status searches

#### `stock_types`
- **Purpose**: Stores stock type categories (Common Stock, ETF, REIT, etc.)
- **Key Fields**: `code`, `name`, `description`, `is_active`
- **Indexes**: Optimized for code and active status searches

#### `categories`
- **Purpose**: Additional categorization support
- **Note**: Referenced in changelog, supporting extensible categorization

### **Database Migration**

The database schema is managed through Liquibase with versioned changesets:
- `001-create-stocks-table.yaml`: Initial stock table structure
- `002-create-categories-tables.yaml`: Category and related tables

All migrations run automatically on application startup when `quarkus.liquibase.migrate-at-start=true`.

## Docker Deployment

### **Development Environment**

The project includes a complete Docker Compose setup for development:

#### Start Development Services
```bash
# Start PostgreSQL and Adminer
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Included Services
- **PostgreSQL 15**: Main database on port `5434`
- **Adminer**: Database administration UI on port `8084`

#### Database Access
- **Host**: localhost:5434
- **Database**: suggestions_db
- **User**: postgres
- **Password**: suggestions_pass
- **Adminer URL**: http://localhost:8084

### **Application Deployment**

#### **Native Image** (Recommended for Production)
```bash
# Build native image
docker build -f Dockerfile -t portfolio-api:native .

# Run with environment variables
docker run -i --rm -p 8090:8090 \
  -e TWELVE_DATA_API_KEY="your-api-key" \
  -e PGHOST="your-db-host" \
  -e PGPORT="5432" \
  -e PGUSER="postgres" \
  -e PGPASSWORD="your-password" \
  -e PGDATABASE="suggestions_db" \
  portfolio-api:native
```

#### **JVM Mode** (Faster builds for Development)
```bash
# Build the application
./gradlew build

# Build JVM image
docker build -f Dockerfile.jvm -t portfolio-api:jvm .

# Run JVM container
docker run -i --rm -p 8090:8090 \
  -e TWELVE_DATA_API_KEY="your-api-key" \
  portfolio-api:jvm
```

#### **Production Docker Compose**
For a complete production deployment, create a `docker-compose.prod.yml`:

```yaml
version: '3.8'
services:
  portfolio-api:
    image: portfolio-api:native
    ports:
      - "8090:8090"
    environment:
      - TWELVE_DATA_API_KEY=${TWELVE_DATA_API_KEY}
      - PGHOST=postgres
      - PGPORT=5432
      - PGUSER=postgres
      - PGPASSWORD=${POSTGRES_PASSWORD}
      - PGDATABASE=suggestions_db
    depends_on:
      - postgres
      
  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=suggestions_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      
volumes:
  postgres_data:
```

## Configuration

The application is configured through `src/main/resources/application.properties` with support for environment variable overrides:

### **Core Configuration**
```properties
# Application
quarkus.application.name=suggestions-api
quarkus.application.version=1.0.0

# HTTP Server
quarkus.http.port=8090
quarkus.http.cors=true
quarkus.http.cors.origins=*
```

### **Database Configuration**
```properties
# PostgreSQL Connection
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${PGUSER:postgres}
quarkus.datasource.password=${PGPASSWORD:suggestions_pass}
quarkus.datasource.reactive.url=postgresql://${PGHOST:localhost}:${PGPORT:5434}/${PGDATABASE:suggestions_db}

# Connection Pool
quarkus.datasource.reactive.max-size=20
quarkus.datasource.reactive.idle-timeout=PT10M

# Hibernate ORM
quarkus.hibernate-orm.database.generation=validate
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.log.format-sql=true

# Liquibase Migration
quarkus.liquibase.migrate-at-start=true
quarkus.liquibase.change-log=db/changelog.yaml
quarkus.liquibase.validate-on-migrate=true
```

### **External API Configuration**
```properties
# TwelveData API
twelve.data.api.key=${TWELVE_DATA_API_KEY}
twelve.data.api.timeout.seconds=300

# REST Client
quarkus.rest-client.twelve-data-api.url=https://api.twelvedata.com
quarkus.rest-client.twelve-data-api.connect-timeout=30000
quarkus.rest-client.twelve-data-api.read-timeout=30000
```

### **Caching Configuration**
The application uses Caffeine for high-performance caching:
```properties
# Stock Types Cache
quarkus.cache.caffeine.stock-types-all.expire-after-write=PT30M
quarkus.cache.caffeine.stock-types-search.expire-after-write=PT10M
quarkus.cache.caffeine.stock-types-by-code.expire-after-write=PT1H

# Currencies Cache  
quarkus.cache.caffeine.currencies-all.expire-after-write=PT30M
quarkus.cache.caffeine.currencies-search.expire-after-write=PT10M
quarkus.cache.caffeine.currencies-by-code.expire-after-write=PT1H

# Exchanges Cache
quarkus.cache.caffeine.exchanges-all.expire-after-write=PT30M
quarkus.cache.caffeine.exchanges-search.expire-after-write=PT10M
quarkus.cache.caffeine.exchanges-by-code.expire-after-write=PT1H
```

### **Environment Variables**
Key environment variables for deployment:
- `TWELVE_DATA_API_KEY`: Required for market data integration
- `PGHOST`: Database host (default: localhost)
- `PGPORT`: Database port (default: 5434)
- `PGUSER`: Database user (default: postgres)
- `PGPASSWORD`: Database password (default: suggestions_pass)
- `PGDATABASE`: Database name (default: suggestions_db)

## Testing

The project includes comprehensive test coverage with unit and integration tests:

### **Run All Tests**
```bash
./gradlew test
```

### **Run with Coverage**
```bash
./gradlew test jacocoTestReport
```

### **Test Reports**
Test reports are generated in `build/reports/tests/test/index.html`

### **Test Architecture**
- **Unit Tests**: Domain logic, use cases, and services tested in isolation
- **Repository Tests**: Database operations tested with test containers
- **Integration Tests**: API endpoints tested end-to-end
- **Reactive Testing**: Proper testing of `Uni` and `Multi` reactive streams

## Performance & Monitoring

### **Caching Strategy**
- **Caffeine Cache**: In-memory caching for reference data (currencies, exchanges, stock types)
- **Cache Expiration**: Intelligent expiration times based on data volatility
- **Cache Keys**: Optimized for common query patterns

### **Database Optimization**
- **Indexes**: Strategic indexes on frequently queried fields
- **Reactive Queries**: Non-blocking database operations
- **Connection Pooling**: Optimized connection pool settings

### **API Performance**
- **Reactive Architecture**: Non-blocking request processing
- **Pagination**: Efficient pagination for large datasets
- **Response Optimization**: Minimal data transfer with focused DTOs

## Deployment Considerations

### **For Frontend Integration**
The API is optimized for modern frontend applications:
- **CORS Enabled**: Cross-origin requests supported
- **RESTful Design**: Standard HTTP methods and status codes
- **JSON API**: Consistent JSON response format
- **OpenAPI Documentation**: Complete API specification for code generation
- **TypeScript Support**: Generate TypeScript clients from OpenAPI spec

### **Production Checklist**
- [ ] Configure production database with proper credentials
- [ ] Set up TwelveData API key with appropriate rate limits
- [ ] Enable HTTPS with proper SSL certificates
- [ ] Configure monitoring and logging
- [ ] Set up health checks and metrics
- [ ] Implement rate limiting and API security
- [ ] Configure backup and disaster recovery

### **Scalability**
- **Reactive Architecture**: High concurrency with low resource usage
- **Database Connection Pooling**: Efficient database resource utilization
- **Caching Layer**: Reduced database load for reference data
- **Stateless Design**: Horizontal scaling capabilities

## Project Structure

The project follows Clean Architecture with clear separation of layers:

```
src/main/java/com/portfolio/management/
├── domain/                                    # Domain Layer (Business Logic)
│   ├── model/                                # Domain Models
│   │   ├── Stock.java                        # Stock entity
│   │   ├── Currency.java                     # Currency entity  
│   │   ├── Exchange.java                     # Exchange entity
│   │   ├── StockType.java                    # Stock type entity
│   │   ├── StockProcessingResult.java        # Processing results
│   │   ├── StocksBatchProcessingResult.java  # Batch processing results
│   │   └── Errors.java                       # Error handling
│   └── port/                                 # Domain Ports (Interfaces)
│       ├── incoming/                         # Use Cases (Input Ports)
│       │   ├── GetSuggestionsUseCase.java            # Basic stock suggestions
│       │   ├── GetSuggestionsAdvancedUseCase.java    # Advanced stock search
│       │   ├── GetCurrenciesUseCase.java             # Currency operations
│       │   ├── GetCurrencyUseCase.java               # Single currency lookup
│       │   ├── GetExchangesUseCase.java              # Exchange operations
│       │   ├── GetExchangeUseCase.java               # Single exchange lookup
│       │   ├── GetStockTypesUseCase.java             # Stock type operations
│       │   ├── GetStockTypeUseCase.java              # Single stock type lookup
│       │   └── FetchAndStoreStockDataUseCase.java    # Data import operations
│       └── outgoing/                         # Output Ports (Dependencies)
│           ├── StockPort.java                        # Stock data access
│           ├── CurrencyRepository.java               # Currency data access
│           ├── ExchangeRepository.java               # Exchange data access
│           ├── StockTypeRepository.java              # Stock type data access
│           └── MarketDataPort.java                   # External market data
├── application/                              # Application Layer (Use Case Implementations)
│   └── service/                             # Application Services
│       ├── SuggestionService.java                   # Stock suggestions service
│       ├── AdvancedSuggestionService.java           # Advanced search service
│       ├── CurrencyService.java                     # Currency management
│       ├── ExchangeService.java                     # Exchange management  
│       ├── StockTypeService.java                    # Stock type management
│       └── FetchAndStoreStockDataService.java       # Data import service
├── infrastructure/                           # Infrastructure Layer (External Concerns)
│   └── adapters/
│       ├── incoming/                         # Input Adapters (Web)
│       │   └── web/                         # REST Controllers
│       │       ├── SuggestionsController.java       # Suggestions API interface
│       │       ├── SuggestionsResource.java         # Suggestions implementation
│       │       ├── CurrencyController.java          # Currency API interface
│       │       ├── CurrencyResource.java            # Currency implementation
│       │       ├── ExchangeController.java          # Exchange API interface
│       │       ├── ExchangeResource.java            # Exchange implementation
│       │       ├── StockTypeController.java         # Stock type API interface
│       │       ├── StockTypeResource.java           # Stock type implementation
│       │       ├── AdminController.java             # Admin API interface
│       │       ├── AdminResource.java               # Admin implementation
│       │       ├── dto/                            # Data Transfer Objects
│       │       │   ├── SuggestionsRequest.java     # Request DTOs
│       │       │   ├── SuggestionsResponse.java    # Response DTOs
│       │       │   ├── CurrencyResponse.java       # Currency DTOs
│       │       │   ├── ExchangeResponse.java       # Exchange DTOs
│       │       │   ├── StockTypeResponse.java      # Stock type DTOs
│       │       │   └── ErrorResponse.java          # Error DTOs
│       │       └── mapper/                         # MapStruct Mappers
│       │           ├── StockMapper.java            # Stock mapping
│       │           ├── CurrencyWebMapper.java      # Currency web mapping
│       │           ├── ExchangeWebMapper.java      # Exchange web mapping
│       │           ├── StockTypeWebMapper.java     # Stock type web mapping
│       │           └── ErrorMapper.java            # Error mapping
│       └── outgoing/                         # Output Adapters (External Systems)
│           ├── client/                      # External API Clients
│           │   ├── MarketDataAdapter.java           # Market data integration
│           │   ├── TwelveDataClient.java            # TwelveData API client
│           │   ├── dto/                            # External API DTOs
│           │   │   └── TwelveDataStockResponse.java # TwelveData response
│           │   └── mapper/                         # External mapping
│           │       └── StockMapper.java            # Stock data mapping
│           └── repository/                   # Database Repositories
│               ├── DatabaseStockRepository.java     # Stock data access
│               ├── DatabaseCurrencyRepository.java  # Currency data access
│               ├── DatabaseExchangeRepository.java  # Exchange data access
│               ├── DatabaseStockTypeRepository.java # Stock type data access
│               ├── mapper/                         # Database Mappers
│               │   ├── StockMapper.java            # Stock entity mapping
│               │   └── CategoryMapper.java         # Category mapping
│               └── persistence/                    # Persistence Layer
│                   ├── StockPersistenceAdapter.java        # Stock persistence
│                   ├── CurrencyPersistenceAdapter.java     # Currency persistence
│                   ├── ExchangePersistenceAdapter.java     # Exchange persistence
│                   ├── StockTypePersistenceAdapter.java    # Stock type persistence
│                   └── entity/                            # JPA Entities
│                       ├── StockEntity.java               # Stock table
│                       ├── CurrencyEntity.java            # Currency table
│                       ├── ExchangeEntity.java            # Exchange table
│                       └── StockTypeEntity.java           # Stock type table
└── SuggestionsApplication.java               # Main Application Class

src/main/resources/
├── application.properties                    # Configuration
└── db/                                      # Database Schema
    ├── changelog.yaml                       # Liquibase master changelog
    └── changelog/                          # Individual changesets
        ├── 001-create-stocks-table.yaml    # Stock table creation
        └── 002-create-categories-tables.yaml # Category tables creation

src/test/java/                              # Test Suite
└── com/portfolio/management/
    ├── application/service/                 # Service Tests
    ├── infrastructure/adapters/
    │   ├── incoming/web/                    # Controller Tests
    │   └── outgoing/
    │       ├── client/                      # Client Tests
    │       └── repository/                  # Repository Tests
    └── domain/                             # Domain Tests
```

### **Key Design Patterns**

- **Clean Architecture**: Clear separation between domain, application, and infrastructure
- **Hexagonal Architecture**: Domain isolated from external concerns via ports and adapters
- **Repository Pattern**: Data access abstraction with reactive implementations
- **Mapper Pattern**: Type-safe mapping between layers using MapStruct
- **Use Case Pattern**: Business operations encapsulated as discrete use cases
- **Adapter Pattern**: External systems integrated via adapters

## Error Handling

The API provides comprehensive error handling with meaningful HTTP status codes and detailed error messages:

### **HTTP Status Codes**
- `200 OK`: Successful request
- `400 Bad Request`: Invalid request parameters or validation errors  
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: System errors

### **Error Response Format**
```json
{
  "errors": [
    {
      "field": "query",
      "message": "Search query must be at least 2 characters long",
      "code": "INVALID_QUERY_LENGTH"
    }
  ],
  "message": "Validation failed",
  "timestamp": 1694771234567
}
```

### **Validation Rules**
- Query parameters validated for length and format
- Limit parameters bounded to reasonable ranges
- Required fields enforced at API level
- Business rules enforced in domain layer

## Future Enhancements

### **Immediate Roadmap**
- [ ] **Real-time Updates**: WebSocket support for live market data
- [ ] **Enhanced Search**: Full-text search with Elasticsearch integration
- [ ] **User Management**: Authentication and user-specific portfolios
- [ ] **Rate Limiting**: API throttling and quota management
- [ ] **Metrics & Monitoring**: Prometheus metrics and health checks

### **Advanced Features**
- [ ] **Market Data Streaming**: Real-time price feeds integration
- [ ] **Portfolio Analytics**: Performance calculations and reporting
- [ ] **Multi-tenancy**: Support for multiple organizations
- [ ] **Event Sourcing**: Audit trail for all data changes
- [ ] **GraphQL API**: Alternative query interface

### **Infrastructure**
- [ ] **Kubernetes Deployment**: Production-ready K8s manifests
- [ ] **CI/CD Pipeline**: Automated testing and deployment
- [ ] **Service Mesh**: Istio integration for microservices
- [ ] **Distributed Caching**: Redis cluster for high availability
- [ ] **Message Queues**: Apache Kafka for event-driven architecture

## License

© 2025 Pablo Cazorla. All rights reserved.
This code is proprietary and confidential.