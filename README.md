# Patria Test API

A Spring Boot REST API application for managing inventory, items, and orders with comprehensive test coverage.

## Technology Stack

- **Java 21** with Spring Boot 3.5.3
- **Maven** for build management
- **H2 Database** for data persistence
- **Docker & Docker Compose** for containerization
- **Springdoc OpenAPI** for API documentation and Swagger UI

## Prerequisites

### Local Development
- Java 21 JDK
- Maven 3.6+
- Docker and Docker Compose (for running with Docker)

### Docker-only
- Docker
- Docker Compose

## Quick Start with Docker

### 1. Build and Run with Docker Compose

```bash
docker compose up --build -d
```

This command will:
- Build the Java application image
- Start the H2 database container
- Start the Spring Boot application container
- Create a bridge network for container communication

The API will be available at `http://localhost:8080`

### 2. View Container Logs

```bash
# View all logs
docker compose logs -f

# View specific service logs
docker compose logs -f java
docker compose logs -f h2
```

### 3. Stop the Application

```bash
docker compose down
```

### 4. Stop and Remove Data

```bash
docker compose down -v
```

## Environment Configuration

The application uses environment variables for configuration. Create a `.env` file in the project root:

```env
APP_PORT=8080
DB_NAME=test
DB_HOST=h2
DB_PORT=1521
DB_USERNAME=sa
DB_PASSWORD=
AES_KEY=M5Qh!0p@$@%^)!)^[]\KGe01
```

When using Docker Compose, the `.env` file is automatically loaded.

## Testing the API

### Option 1: Swagger UI (Recommended for Quick Testing)

Once the application is running, open your browser:

```
http://localhost:8080/v1/swagger-ui/index.html
```

Swagger UI provides:
- Interactive API documentation
- Try-it-out functionality to test endpoints directly
- Request/response examples
- Schema documentation

### Option 2: Postman Collection

The project includes Postman collections for API testing:

1. **Import the Collection**
   - Open Postman
   - Click `Import` → `Upload Files`
   - Select `Patria Test API.postman_collection.json` or `Patria_Test_API.postman_collection.json`

2. **Configure Environment** (optional)
   - Set base URL: `http://localhost:8080`
   - Import the collection and start testing

3. **Run Tests**
   - Navigate through the collection folders
   - Click on any request to view details
   - Click `Send` to execute the request
   - View response in the Response panel

### Option 3: cURL

Example testing with cURL:

```bash
# List all items
curl -X GET http://localhost:8080/v1/items

# Create an inventory item
curl -X POST http://localhost:8080/v1/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Name",
    "description": "Product Description",
    "quantity": 100
  }'
```

## Local Development (Without Docker)

### 1. Start H2 Database

```bash
docker run -d \
  --name h2-local \
  -p 1521:1521 \
  -p 81:81 \
  oscarfonts/h2
```

### 2. Build the Project

```bash
./mvnw clean compile
```

### 3. Run Tests

```bash
./mvnw test
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

Set environment variables before running:

```bash
# Linux/Mac
export APP_PORT=8080
export DB_NAME=test
export DB_HOST=localhost
export DB_PORT=1521
export DB_USERNAME=sa
export DB_PASSWORD=
export AES_KEY=M5Qh!0p@$@%^)!)^[]\KGe01

# Windows PowerShell
$env:APP_PORT='8080'
$env:DB_NAME='test'
$env:DB_HOST='localhost'
$env:DB_PORT='1521'
$env:DB_USERNAME='sa'
$env:DB_PASSWORD=''
$env:AES_KEY='M5Qh!0p@$@%^)!)^[]\KGe01'
```

## API Endpoints

The API provides endpoints for:

- **Items** - CRUD operations on items
- **Inventory** - Manage inventory records
- **Orders** - Process and track orders

For complete API documentation, visit: `http://localhost:8080/v1/swagger-ui/index.html`

## Troubleshooting

### Port Already in Use

If port 8080 is already in use:

```bash
# Change the port in docker-compose.yaml or .env
docker compose down
# Edit .env and change APP_PORT
docker compose up --build -d
```

### Database Connection Issues

1. Ensure H2 container is healthy:
   ```bash
   docker compose ps
   ```

2. Check H2 console: `http://localhost:81`

3. Verify environment variables are set correctly in `.env`

### Application Won't Start

1. Check logs:
   ```bash
   docker compose logs java
   ```

2. Verify Docker images:
   ```bash
   docker images
   ```

3. Rebuild images:
   ```bash
   docker compose down
   docker compose up --build -d
   ```

## Building for Production

```bash
./mvnw clean package
docker build -t patria-test-api:latest .
```

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [H2 Database](http://www.h2database.com/)
- [Swagger/OpenAPI](https://swagger.io/)

## License

See LICENSE file for details
