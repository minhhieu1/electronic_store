# Starting the Electronic Store Application with Docker

This guide explains how to build and start the Electronic Store application using Docker via the Gradle `bootBuildImage` task.

## Prerequisites

Before starting the application, ensure you have the following:

- **Java 17** or higher installed
- **Docker** installed and running
- **Git** (if cloning the repository)
- Terminal/Command Prompt access
- Internet connection (for downloading dependencies and base images)

## Quick Start

### 1. Navigate to Project Directory

```bash
cd /path/to/electronic-store
```

### 2. Build Docker Image

```bash
./gradlew bootBuildImage
```

### 3. Run the Docker Container

```bash
docker run -p 8080:8080 electronic-store:latest
```

The application will start and be available at: **http://localhost:8080**

## Detailed Steps

### Step 1: Verify Prerequisites

Check your installations:

```bash
# Check Java version
java -version

# Check Docker installation
docker --version

# Verify Docker is running
docker info
```

Expected Java output should show Java 17 or higher:
```
openjdk version "17.0.x" 2023-xx-xx
OpenJDK Runtime Environment (build 17.0.x+xx)
OpenJDK 64-Bit Server VM (build 17.0.x+xx, mixed mode, sharing)
```

### Step 2: Clean Build (Optional but Recommended)

Before building the Docker image, you can clean previous builds:

```bash
# Clean previous builds
./gradlew clean

# Compile and test the application
./gradlew build -x test
```

### Step 3: Build Docker Image

Build the Docker image using Spring Boot's buildpack integration:

```bash
./gradlew bootBuildImage
```

This command will:
- Use Paketo buildpacks to create an optimized container image
- Create an image named `electronic-store:0.0.1-SNAPSHOT`
- Also tag it as `electronic-store:latest`
- Configure the container to use Java 17

### Step 4: Verify Image Creation

Check that the image was created successfully:

```bash
docker images | grep electronic-store
```

You should see output similar to:
```
electronic-store    latest    abc123def456    2 minutes ago    287MB
electronic-store    0.0.1-SNAPSHOT    abc123def456    2 minutes ago    287MB
```

### Step 5: Run the Docker Container

Start the application container:

```bash
# Basic run command
docker run -p 8080:8080 electronic-store:latest
```

Or run in detached mode (background):

```bash
# Run in background
docker run -d -p 8080:8080 --name electronic-store-app electronic-store:latest
```

### Step 6: Verify Application Startup

You should see output similar to:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.3.5)

INFO  --- Electronic Store started in X.XXX seconds
```

## Application URLs

Once the container is running, you can access:

- **Main Application**: http://localhost:8080
- **API Documentation (Swagger)**: http://localhost:8080/swagger-ui.html
- **API Docs (OpenAPI)**: http://localhost:8080/v3/api-docs
