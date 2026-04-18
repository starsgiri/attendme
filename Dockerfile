# Stage 1: Build the Spring Boot application
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies first for faster rebuilds
COPY pom.xml ./
RUN mvn -B dependency:go-offline

# Copy source and build the executable jar
COPY src ./src
RUN mvn -B -DskipTests clean package

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-root for better container security
RUN addgroup --system spring && adduser --system --ingroup spring spring

# Copy generated jar without hardcoding version
COPY --from=build /workspace/target/*.jar /app/app.jar

# Standard app port; vendor platforms can override via PORT env var
EXPOSE 8080

USER spring

# Supports platforms like Render/Railway/Fly by honoring PORT env var
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
