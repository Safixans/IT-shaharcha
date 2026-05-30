# syntax=docker/dockerfile:1
#
# Optional: build a self-contained gateway image (the docker-compose.full.yml flow mounts
# prebuilt jars instead and is the usual dev path). Build from the REPO ROOT so the Maven
# reactor can resolve the parent POM and the common-lib dependency:
#   docker build -f services/api-gateway/Dockerfile -t itsh/api-gateway .

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copy the parent POM and all module sources so the reactor graph resolves; -am also
# builds the upstream dependency (common-lib) that the gateway needs.
COPY pom.xml .
COPY services services
RUN mvn -q -pl services/api-gateway -am clean package -DskipTests

# Runtime matches docker-compose.full.yml (Java 25 LTS).
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/services/api-gateway/target/api-gateway.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
