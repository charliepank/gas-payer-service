# Runtime stage - using slim variant for smaller footprint
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Copy the pre-built JAR from GitHub workflow (use the Spring Boot fat jar, not the plain jar)
COPY build/libs/gas-payer-service-*.jar app.jar

# Install curl for healthcheck and clean up in same layer to reduce size
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

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
# Use shell form to allow JAVA_OPTS environment variable
ENTRYPOINT sh -c "java $JAVA_OPTS -jar app.jar"