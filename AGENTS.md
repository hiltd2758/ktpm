# Repository Instructions

## Project Shape
- Main backend code lives in [web/](web); it is a Spring Boot 3.5.6 app on Java 21.
- The real-time chat service is a separate Node app in [web/chat_websocket/](web/chat_websocket).
- Functional scope and module boundaries are documented in [SRS_Document.md](SRS_Document.md); link to it instead of restating requirements.

## Build And Test
- Run backend work from [web/](web) with Maven Wrapper: `./mvnw test`, `./mvnw verify`, or `./mvnw package -DskipTests`.
- Use the Maven project in [web/pom.xml](web/pom.xml) as the source of truth for backend dependencies and plugins.
- For the chat service, install dependencies in [web/chat_websocket/package.json](web/chat_websocket/package.json) and run `npm run dev` for local development.

## Code Boundaries
- Backend Java code is under [web/src/main/java/](web/src/main/java) and tests under [web/src/test/java/](web/src/test/java).
- Thymeleaf templates and static assets live under [web/src/main/resources/templates/](web/src/main/resources/templates) and [web/src/main/resources/static/](web/src/main/resources/static).
- Keep the static-resource security rules in [web/src/main/java/com/e_health_care/web/security/StaticResourceSecurityConfiguration.java](web/src/main/java/com/e_health_care/web/security/StaticResourceSecurityConfiguration.java) aligned with any new asset paths.

## Repo-Specific Cautions
- Do not edit generated output in [web/target/](web/target) unless explicitly asked.
- Treat database dump files at the repo root as reference material unless the task is specifically about schema/data changes.
- The chat server in [web/chat_websocket/server.js](web/chat_websocket/server.js) expects JWT secrets from environment variables and talks to the backend at `http://localhost:8080`; keep those assumptions in sync when making changes.
- For tests, prefer the H2 profile in [web/src/test/resources/application-test.properties](web/src/test/resources/application-test.properties).