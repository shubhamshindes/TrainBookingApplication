FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*
COPY target/booking-service-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for.sh /app/wait-for.sh
RUN chmod +x /app/wait-for.sh
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["/app/wait-for.sh", "db", "3306", "redis", "6379", "eureka-server", "8761", "--", "java", "-jar", "/app/app.jar"]
