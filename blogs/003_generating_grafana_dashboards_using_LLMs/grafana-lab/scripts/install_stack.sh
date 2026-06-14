#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Ensuring namespaces exist"
kubectl apply -f "${ROOT_DIR}/k8s/ns.yaml"

echo "==> Installing kube-prometheus-stack via Helm"
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Release name: kps
helm upgrade --install kps prometheus-community/kube-prometheus-stack \
  --namespace observability \
  --values "${ROOT_DIR}/helm-values/kube-prom-stack-values.yaml"

echo "==> Waiting for CRDs to be ready (ServiceMonitor)"
# Wait until the CRD exists before applying ServiceMonitors
until kubectl get crd servicemonitors.monitoring.coreos.com >/dev/null 2>&1; do
  echo "   ... waiting for ServiceMonitor CRD"
  sleep 2
done

echo "==> Applying demo app and Grafana dashboard"
kubectl apply -f "${ROOT_DIR}/k8s/app-deploy.yaml"
kubectl apply -f "${ROOT_DIR}/k8s/app-servicemonitor.yaml"
kubectl apply -f "${ROOT_DIR}/k8s/grafana-dashboard-cm.yaml"

echo "==> Done. Check pods in 'observability' and 'demo' namespaces."
