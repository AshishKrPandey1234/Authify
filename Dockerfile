# Step 1: Use a lightweight OpenJDK 21 runtime base image
FROM eclipse-temurin:21-jre-alpine

# Step 2: Create and set the working directory inside the container
WORKDIR /app

# Step 3: Copy the compiled JAR file from your local target folder into the container
COPY target/authify-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose port 8080 so we can access the app outside the container
EXPOSE 8080

# Step 5: Command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]