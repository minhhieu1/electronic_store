# Starting the Electronic Store Application with Gradle

This guide explains how to start the Electronic Store application using the Gradle wrapper (`./gradlew`).

## Prerequisites

Before starting the application, ensure you have the following:

- **Java 17** or higher installed
- **Git** (if cloning the repository)
- Terminal/Command Prompt access
- Internet connection (for downloading dependencies)

## Quick Start

### 1. Navigate to Project Directory

```bash
cd /path/to/electronic-store
```

### 2. Start the Application

```bash
./gradlew bootRun
```

The application will start and be available at: **http://localhost:8080**

## Detailed Steps

### Step 1: Verify Java Installation

Check your Java version:

```bash
java -version
```

Expected Java output should show Java 17 or higher:
```
openjdk version "17.0.x" 2023-xx-xx
OpenJDK Runtime Environment (build 17.0.x+xx)
OpenJDK 64-Bit Server VM (build 17.0.x+xx, mixed mode, sharing)
```

### Step 2: Clean Build (Optional but Recommended)

Before starting, you can clean and build the project:

```bash
# Clean previous builds
./gradlew clean

# Compile the application
./gradlew build -x test
```

### Step 3: Start the Application

Run the Spring Boot application:

```bash
./gradlew bootRun
```

### Step 4: Verify Application Startup

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

Once the application is running, you can access:

- **Main Application**: http://localhost:8080
- **API Documentation (Swagger)**: http://localhost:8080/swagger-ui.html
- **API Docs (OpenAPI)**: http://localhost:8080/v3/api-docs

**Note**: This application uses an embedded H2 database by default, so no external database setup is required for development.
