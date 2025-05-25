# BUILD STAGE
FROM maven:3.9.9 as builder

WORKDIR /build

COPY .. .
COPY settings.xml /root/.m2/settings.xml

RUN mvn clean package -DskipTests

# Above command creates target/token-provider...0.jar

# RUNTIME STAGE
FROM openjdk:21-slim as runtime

RUN useradd -ms /bin/bash appuser

WORKDIR /app

COPY --from=builder /build/target/token-provider-1.0.0.jar app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]