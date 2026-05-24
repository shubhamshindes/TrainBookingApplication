# Multi-stage Dockerfile: build with Maven, run with a lightweight JRE

# --- Build stage ---
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
# Copy everything and build the project (skip tests to speed up builds locally)
COPY . .
RUN mvn -B -DskipTests package

# --- Run stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
# Install netcat-openbsd for health/wait checks
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

# Copy the built jar from the previous stage
COPY --from=build /workspace/target/TrainBookingApplication-0.0.1-SNAPSHOT.jar app.jar

# Copy wait-for script
COPY wait-for.sh /app/wait-for.sh
RUN chmod +x /app/wait-for.sh

EXPOSE 8080

# Use wait-for.sh to wait for required services (DB and Redis) before launching the app
ENTRYPOINT ["/app/wait-for.sh", "db", "3306", "redis", "6379", "--", "java", "-jar", "/app/app.jar"]
