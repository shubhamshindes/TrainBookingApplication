FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*
COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for.sh /app/wait-for.sh
RUN chmod +x /app/wait-for.sh
EXPOSE 8080
ENTRYPOINT ["/app/wait-for.sh", "eureka-server", "8761", "--", "java", "-jar", "/app/app.jar"]
