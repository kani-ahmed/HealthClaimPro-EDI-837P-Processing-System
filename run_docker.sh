#!/bin/bash

# Base directory
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Jar directory
JAR_DIR="$BASE_DIR/build/libs"

# Docker image name
IMAGE_NAME="app"

# Find latest jar (excluding plain jars)
find_latest_jar() {
    echo "Searching for the latest JAR file..."
    LATEST_JAR=$(find "$JAR_DIR" -type f -name "*.jar" ! -name "*-plain.jar" -print0 | xargs -0 ls -t | head -n 1)
    echo "Latest JAR found: $LATEST_JAR"
}

# Build the Docker image
build_image() {
    find_latest_jar

    if [[ ! -f "$LATEST_JAR" ]]; then
        echo "Jar file not found. Building a clean version of the project..."
        (cd "$BASE_DIR" && ./gradlew clean build -x test)
        LATEST_JAR=$(find_latest_jar)
    fi

    if [[ -z "$LATEST_JAR" ]]; then
        echo "Failed to find or build the jar file. Exiting."
        exit 1
    fi

    echo "Building Docker image with jar file: $LATEST_JAR"
    docker build -t "$IMAGE_NAME" --no-cache --build-arg JAR_FILE="$LATEST_JAR" -f "$BASE_DIR/Dockerfile" "$BASE_DIR"
}

# Ask the user for the number of containers to run
echo "How many containers do you want to run?"
read -r CONTAINER_COUNT

# Build the Docker image
build_image

# Run the specified number of containers on sequential ports starting from 8080
for (( i = 1; i <= CONTAINER_COUNT; i++ )); do
    HOST_PORT=$((8080 + i - 1))
    echo "Running container $i on port $HOST_PORT..."
    docker run -d -p "$HOST_PORT:8080" --env-file "$BASE_DIR/src/main/java/com/billing/webapp/.env-docker" "$IMAGE_NAME"
done

echo "$CONTAINER_COUNT Docker containers are running with image: $IMAGE_NAME"