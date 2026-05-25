# Stage 1: Build the application using Maven and OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the project files into the container
COPY pom.xml .
COPY src ./src

# Compile and package the application cleanly inside the cloud
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled JAR file directly from the build stage instead of your laptop
COPY --from=build /app/target/authify-0.0.1-SNAPSHOT.jar app.jar

# Expose backend port and set entrypoint
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]