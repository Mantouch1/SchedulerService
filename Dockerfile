# Stage 1: build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Stage 2: runtime (small image, non-root user)
FROM eclipse-temurin:17-jre
RUN useradd --system --create-home appuser
WORKDIR /app
COPY --from=build /workspace/target/scheduler-service-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
