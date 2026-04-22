# EasyAccess - Instant Attendance System

EasyAccess is a Spring Boot attendance application that lets admins create attendance sessions, publish QR codes, collect attendee check-ins, and export attendance data. It also includes geofencing support, authentication, history tracking, and duplicate-submission blocking.

## Features

- QR-based attendance submission
- Session creation with geofencing support
- Duplicate attendance restriction per session
- Attendance history view
- CSV and Excel export
- Spring Security login and role-based access
- Thymeleaf-based server-side UI

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Security
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- Maven

## Prerequisites

- JDK 21 or newer
- Maven 3.9+ or the included Maven Wrapper
- PostgreSQL database

## Configuration

Application settings are stored in `src/main/resources/application.properties`.

Default local settings:

- Application port: `8081`
- Database: PostgreSQL

If you are running the app against your own database, update the datasource values in `application.properties` before starting the application.

## Run Locally

Using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on:

- `http://localhost:8081`

## Build

```bash
./mvnw -DskipTests package
```

On Windows:

```powershell
.\mvnw.cmd -DskipTests package
```

## Main Pages

- `/login` - User login
- `/signup` - User registration
- `/history` - Session history
- `/attend/{sessionId}` - Attendance form for a session

## Project Structure

- `src/main/java/com/example/demo/controller` - Web controllers
- `src/main/java/com/example/demo/service` - Business logic and export helpers
- `src/main/java/com/example/demo/repository` - Data access layer
- `src/main/java/com/example/demo/entity` - JPA entities
- `src/main/resources/templates` - Thymeleaf views

## Notes

- Do not commit real database credentials to version control.
- The application uses server-side duplicate checks to prevent repeated attendance submissions for the same session.
