#!/bin/bash

# Delete Kubernetes deployment
echo "Deleting Kubernetes deployment..."
kubectl delete deployment app-deployment

# Delete Kubernetes service
echo "Deleting Kubernetes service..."
kubectl delete service app-service

# Optional: Clean up Docker images
echo "Switching to Minikube's Docker daemon..."
eval $(minikube docker-env)

echo "Removing Docker image..."
docker rmi app:latest

echo "Switching back to host's Docker daemon..."
eval $(minikube docker-env -u)

# Make sure your create_run_docker.sh script is executable
chmod +x ./create_and_run_k8s.sh

# Rerun the script
echo "Rerunning the Docker setup script..."
./create_and_run_k8s.sh

