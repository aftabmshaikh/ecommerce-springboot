# Use a base image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create config directory first
RUN mkdir -p /app/config

# Copy the JAR file from the target directory
COPY backend-springboot/api-gateway/target/*.jar app.jar

# Create config directory in the container
RUN mkdir -p /app/config

# Copy config files if they exist (using absolute paths)
COPY ["backend-springboot/api-gateway/src/main/resources/bootstrap.yml", "./config/"]
COPY ["backend-springboot/api-gateway/src/main/resources/application.yml", "./config/"]

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false

# Expose the application port
EXPOSE 8080

# Set the entry point to run the JAR file with system properties
ENTRYPOINT ["java", \
    "-Dspring.cloud.bootstrap.enabled=true", \
    "-Dspring.config.name=bootstrap,application", \
    "-Dspring.config.location=classpath:/,file:./config/", \
    "-Dspring.main.web-application-type=reactive", \
    "-Dlogging.level.root=INFO", \
    "-Dlogging.level.org.springframework.cloud.gateway=DEBUG", \
    "-Dlogging.level.com.ecommerce=DEBUG", \
    "-jar", "app.jar"]