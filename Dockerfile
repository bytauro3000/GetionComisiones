# syntax=docker/dockerfile:1

# ── Etapa 1: build con Maven ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ── Etapa 2: runtime ligero ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/BackVendeLote-*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
