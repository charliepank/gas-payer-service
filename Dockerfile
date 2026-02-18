# Runtime stage - using Alpine for minimal footprint
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the pre-built JAR from GitHub workflow (use the Spring Boot fat jar, not the plain jar)
COPY build/libs/gas-payer-service-*.jar app.jar

# Install curl for healthcheck (compatible with both amd64 and arm64)
RUN apk add --no-cache curl

# Create user for running the app
RUN addgroup -S -g 1001 appgroup && \
    adduser -S -u 1001 -G appgroup appuser

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