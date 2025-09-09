# Use Java 17 runtime
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the Spring Boot JAR into the container
COPY sb-chatgpt/target/sb-chatgpt-0.0.1-SNAPSHOT.jar app.jar

# Expose the backend port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
