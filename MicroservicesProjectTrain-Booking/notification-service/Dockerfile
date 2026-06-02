FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*
COPY target/notification-service-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for.sh /app/wait-for.sh
RUN chmod +x /app/wait-for.sh
EXPOSE 8085
ENTRYPOINT ["/app/wait-for.sh", "eureka-server", "8761", "kafka", "29092", "--", "java", "-jar", "/app/app.jar"]
