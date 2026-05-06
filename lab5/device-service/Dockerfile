FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml /build/pom.xml
COPY auth-service/pom.xml /build/auth-service/pom.xml
COPY device-service/pom.xml /build/device-service/pom.xml
COPY telemetry-service/pom.xml /build/telemetry-service/pom.xml
RUN mvn -pl device-service -am dependency:go-offline -q

COPY device-service/src /build/device-service/src
RUN mvn -pl device-service -am package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/device-service/target/device-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
