# Use the official OpenJDK image as the base image
# Note: The OpenJDK image is based on Ubuntu 20.04 LTS
#(Focal Fossa) and contains the OpenJDK 21 (Java 17) runtime environment (JRE) and
#development kit (JDK) for running Java applications and applets (https://hub.docker.com/_/openjdk)
# It is developed and maintained by Microsoft and is available on the Docker Hub
# No openjdk21 image is yet made by the official OpenJDK team, so we use the Microsoft OpenJDK image instead
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

# Set the working directory inside the container
WORKDIR /app

# Argument for specifying the jar file (based from the script: create_run_docker.sh)
ARG JAR_FILE

# Copy the Firebase configuration and other necessary files into the container
# Ensure these paths are relative to the context of the Docker build (BASE_DIR)
COPY src/main/java/com/billing/webapp/firebase-login-key.json /app/firebase-login-key.json
COPY src/main/java/com/billing/webapp/id_rsa /app/id_rsa
COPY src/main/java/com/billing/webapp/known_hosts /app/known_hosts
# Copy each .pem and .pub file individually
# Note: Docker COPY does not support wildcards for directories, so you must specify each file or copy them beforehand to a known directory
COPY src/main/java/com/billing/webapp/id_rsa.pem /app/
COPY src/main/java/com/billing/webapp/id_rsa.pub /app/
COPY /src/main/java/com/billing/webapp/.env-docker /app/.env-docker
# Copy the jar file into the container using the build argument
COPY build/libs/demo-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port the application runs on (8080) to the outside world
EXPOSE 8080

# Run the jar file when the container launches
ENTRYPOINT ["java","-jar","app.jar"]
