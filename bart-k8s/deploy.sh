#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "==> Starting minikube..."
minikube status --format='{{.Host}}' 2>/dev/null | grep -q "Running" || minikube start

echo "==> Pointing Docker CLI to minikube's daemon..."
eval "$(minikube docker-env)"

echo "==> Building bart-service image..."
docker build -t bart-service:latest "$REPO_ROOT/spring-boot-service"

echo "==> Building bart-python-cli image..."
docker build -t bart-python-cli:latest "$REPO_ROOT/python-cli"

echo "==> Applying k8s manifests..."
kubectl apply -f "$SCRIPT_DIR/k8s/"

echo "==> Waiting for deployments to be ready..."
kubectl rollout status deployment/kafka     --timeout=120s
kubectl rollout status deployment/redis     --timeout=60s
kubectl rollout status deployment/bart-service --timeout=180s
kubectl rollout status deployment/python-cli   --timeout=60s

echo ""
echo "==> All deployments ready."
echo ""
echo "Service URL (bart-service):"
minikube service bart-service --url

echo ""
echo "Useful commands:"
echo "  kubectl logs -f deployment/python-cli     # watch event stream"
echo "  kubectl logs -f deployment/bart-service   # watch Spring Boot logs"
echo "  kubectl logs -f deployment/kafka          # watch Kafka broker logs"
echo "  kubectl delete -f $SCRIPT_DIR/k8s/        # tear everything down"
