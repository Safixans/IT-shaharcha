# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY services/common-lib/pom.xml services/common-lib/pom.xml
COPY services/user-service/pom.xml services/user-service/pom.xml
RUN mvn -q -pl services/common-lib,services/user-service -am dependency:go-offline || true
COPY services/common-lib services/common-lib
COPY services/user-service services/user-service
RUN mvn -q -pl services/common-lib,services/user-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/services/user-service/target/user-service.jar app.jar
USER app
EXPOSE 9002
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
