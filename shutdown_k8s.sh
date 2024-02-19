#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Function to check if a resource exists
resource_exists() {
    local resource_type=$1
    local resource_name=$2
    kubectl get "$resource_type" "$resource_name" &> /dev/null
}

# Function to check if a pod is running
is_pod_running() {
    local pod_name=$1
    local status=$(kubectl get pod "$pod_name" -o jsonpath='{.status.phase}' 2>/dev/null)
    if [ "$status" = "Running" ]; then
        return 0  # Pod is running
    else
        return 1  # Pod is not running
    fi
}

# Function to stop a resource if it's running
stop_resource_if_running() {
    local resource_type=$1
    local resource_name=$2
    if resource_exists "$resource_type" "$resource_name"; then
        echo "Stopping $resource_type '$resource_name'..."
        kubectl delete "$resource_type" "$resource_name"
        echo "$resource_type '$resource_name' stopped successfully."
    else
        echo "$resource_type '$resource_name' not found. Skipping shutdown."
    fi
}

# Kubernetes deployment, service, and pod names
DEPLOYMENT_NAME="app-deployment"
SERVICE_NAME="app-service"
HPA_NAME="hpa"  # Assuming you have a Horizontal Pod Autoscaler
POD_NAME="mysql-client"

# Stop the deployment if it's running
stop_resource_if_running "deployment" "$DEPLOYMENT_NAME"

# Stop the service if it's running
stop_resource_if_running "service" "$SERVICE_NAME"

# Stop the Horizontal Pod Autoscaler if it's running
stop_resource_if_running "hpa" "$HPA_NAME"

# Check if the pod is running and stop it if so
if is_pod_running "$POD_NAME"; then
    echo "Stopping pod '$POD_NAME'..."
    kubectl delete pod "$POD_NAME"
    echo "Pod '$POD_NAME' stopped successfully."
else
    echo "Pod '$POD_NAME' is not running. Skipping deletion."
fi

# Check if Minikube is running and stop it if so
if minikube status | grep -q "Running"; then
    echo "Stopping Minikube..."
    minikube stop
    echo "Minikube stopped successfully."
else
    echo "Minikube is not running. Skipping shutdown."
fi

# Stop Docker
echo "Stopping Docker..."
docker stop $(docker ps -q)
echo "Docker stopped successfully."

echo "Script execution completed."
