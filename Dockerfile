# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy pom.xml from nested folder and download dependencies
COPY placement-tracker/pom.xml .
RUN mvn dependency:go-offline

# Copy source code from nested folder
COPY placement-tracker/src src

# Build application
RUN mvn clean package -Dmaven.test.skip=true

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy jar from builder
COPY --from=builder /build/target/placement-tracker-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8081/api/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

