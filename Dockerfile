FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./

# Copy source code
COPY src src

# Make gradlew executable and build the application
RUN chmod +x ./gradlew && ./gradlew build --no-daemon

# Copy the built JAR (use the Spring Boot fat jar, not the plain jar)
RUN cp build/libs/gas-payer-service-*.jar app.jar

# Create user for running the app
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 appuser

# Change ownership of the app directory
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]