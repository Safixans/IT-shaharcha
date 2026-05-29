# syntax=docker/dockerfile:1

# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent + module poms first for dependency caching
COPY pom.xml .
COPY services/common-lib/pom.xml services/common-lib/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
RUN mvn -q -pl services/common-lib,services/auth-service -am dependency:go-offline || true

# Copy sources and build
COPY services/common-lib services/common-lib
COPY services/auth-service services/auth-service
RUN mvn -q -pl services/common-lib,services/auth-service -am clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/services/auth-service/target/auth-service.jar app.jar
USER app
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
