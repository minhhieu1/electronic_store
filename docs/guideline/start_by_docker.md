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
- **Health Check**: http://localhost:8080/actuator/health

## Docker Commands Reference

| Command | Description |
|---------|-------------|
| `./gradlew bootBuildImage` | Build Docker image using buildpacks |
| `docker images` | List all Docker images |
| `docker run -p 8080:8080 electronic-store:latest` | Run container |
| `docker run -d -p 8080:8080 --name myapp electronic-store:latest` | Run in background |
| `docker ps` | List running containers |
| `docker ps -a` | List all containers |
| `docker stop <container-id>` | Stop a running container |
| `docker rm <container-id>` | Remove a container |
| `docker rmi electronic-store:latest` | Remove the image |

## Advanced Docker Usage

### Running with Environment Variables

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8080 \
  electronic-store:latest
```

### Running with Volume Mounts (for logs)

```bash
docker run -p 8080:8080 \
  -v /host/logs:/app/logs \
  electronic-store:latest
```

### Running with Custom Memory Settings

```bash
docker run -p 8080:8080 \
  -e JAVA_TOOL_OPTIONS="-Xmx512m -Xms256m" \
  electronic-store:latest
```

## Stopping the Application

### If running in foreground:
Press `Ctrl + C` to stop the container

### If running in background:
```bash
# Find the container ID
docker ps

# Stop the container
docker stop <container-id>

# Or stop by name
docker stop electronic-store-app
```

## Troubleshooting

### Common Issues

#### Port Already in Use
If port 8080 is already in use:

```bash
# Use a different port
docker run -p 8081:8080 electronic-store:latest
```

#### Docker Not Running
Ensure Docker Desktop is running:

```bash
# Check Docker status
docker info

# Start Docker (macOS/Windows)
# Use Docker Desktop application
```

#### Image Build Fails
If the image build fails:

```bash
# Clean and rebuild
./gradlew clean
./gradlew build -x test
./gradlew bootBuildImage
```

#### Container Won't Start
Check container logs:

```bash
# View logs of running container
docker logs <container-id>

# View logs with follow mode
docker logs -f <container-id>
```

#### Memory Issues
If you encounter memory issues:

```bash
# Run with more memory
docker run -p 8080:8080 \
  -e JAVA_TOOL_OPTIONS="-Xmx1024m" \
  electronic-store:latest
```

### Getting Help

If you encounter issues:

1. Check the container logs: `docker logs <container-id>`
2. Verify Docker is running: `docker info`
3. Check if the image was built correctly: `docker images`
4. Try rebuilding the image: `./gradlew clean bootBuildImage`

## Image Details

The Docker image created by `bootBuildImage` includes:

- **Base Image**: Paketo buildpacks base image
- **Java Runtime**: OpenJDK 17
- **Application Layer**: Optimized Spring Boot application
- **Size**: Approximately 287MB (optimized with layer caching)
- **Security**: Regular security updates from Paketo buildpacks

## Comparison with Gradle

| Aspect | Gradle (`./gradlew bootRun`) | Docker |
|--------|------------------------------|---------|
| **Startup Time** | Faster (no container overhead) | Slightly slower (container startup) |
| **Isolation** | Runs on host JVM | Fully isolated container |
| **Debugging** | Easier (direct JVM connection) | Requires port forwarding |
| **Production Ready** | Development focused | Production ready |
| **Dependencies** | Requires Java on host | Only requires Docker |
| **Portability** | Platform dependent | Platform independent |

## Next Steps

After starting the application with Docker:

1. Test the API endpoints using the Swagger UI
2. Monitor container resources: `docker stats`
3. Check application logs: `docker logs -f <container-id>`
4. Consider using Docker Compose for multi-container setups
5. Explore container orchestration with Kubernetes

---

**Note**: The Docker image uses an embedded H2 database by default. For production use, consider using external databases and proper volume mounts for data persistence.
