#!/bin/bash

# Switch to Minikube's Docker daemon
eval $(minikube docker-env)

# Base directory
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Jar directory
JAR_DIR="$BASE_DIR/build/libs"

# Docker image name and tag
IMAGE_NAME="app"
IMAGE_TAG="latest"

# Kubernetes deployment and service names
DEPLOYMENT_NAME="app-deployment"
SERVICE_NAME="app-service"

# Find the latest jar (excluding plain jars)
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
        find_latest_jar
    fi

    if [[ -z "$LATEST_JAR" ]]; then
        echo "Failed to find or build the jar file. Exiting."
        exit 1
    fi

    echo "Building Docker image with jar file: $LATEST_JAR"
    docker build -t "$IMAGE_NAME:$IMAGE_TAG" --no-cache --build-arg JAR_FILE="$LATEST_JAR" -f "$BASE_DIR/Dockerfile" "$BASE_DIR"
}

# Deploy the application to Kubernetes
deploy_to_kubernetes() {
    echo "Deploying application to Kubernetes..."

    # Apply the ConfigMap configuration (before the deployment) to ensure the environment variables are available
    kubectl apply -f "$BASE_DIR/app-config.yaml"

    # Replace the image in the deployment YAML file
    # Note: Since we're using Minikube's Docker daemon, there's no need for a registry prefix
    sed "s|image: .*|image: $IMAGE_NAME:$IMAGE_TAG|" "$BASE_DIR/app-deployment.yaml" | kubectl apply -f -

    # Apply the service configuration
    kubectl apply -f "$BASE_DIR/app-service.yaml"

    # Create or update the Horizontal Pod Autoscaler
   kubectl apply -f "$BASE_DIR/hpa.yaml"

    echo "Application deployed to Kubernetes"
}

# Main execution
build_image
deploy_to_kubernetes

# Switch back to the default Docker daemon
eval $(minikube docker-env -u)