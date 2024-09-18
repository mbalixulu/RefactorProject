# Start with a base image containing Java runtime
FROM quay.io/rocep/openjdk:21.0.3

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/*.jar

# Add the application's jar to the container
WORKDIR /
ADD ${JAR_FILE} app.jar

# Run the jar file 
ENTRYPOINT ["java","-jar","/app.jar"]
