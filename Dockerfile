# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Copy the rest of the source code and build the project
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/easyaccess-1.0.0.jar app.jar

# Expose the default port from application.properties
# Note: Render provides a PORT environment variable dynamically
EXPOSE 8081

# Command to run the application, picking up Render's PORT environment variable if provided
CMD ["java", "-jar", "app.jar", "--server.port=${PORT:8081}"]
