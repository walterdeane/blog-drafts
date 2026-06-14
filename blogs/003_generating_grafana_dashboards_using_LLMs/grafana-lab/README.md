# Grafana Lab - Local Kubernetes Monitoring Stack

Spin up a local K3D cluster with kube-prometheus-stack (Prometheus + Grafana), deploy a Go demo app exporting Prometheus metrics, and auto-load a Grafana dashboard.

## Prerequisites

- **Docker Desktop** (macOS/Windows) or **Docker Engine + Compose v2** (Linux)
- **16GB+ RAM** recommended (8GB minimum)
- **10GB+ free disk space** for images and containers

No special Docker Desktop configuration is required. This project uses a no‑registry workflow: images are built locally and imported directly into the k3d cluster.

## Quick Start

### 1. Clone and Setup

```bash
cd grafana-lab
```

### 2. Start the Complete Stack

```bash
# Start all services (bootstrap, build+import, install)
docker compose up -d

# Or run step by step:
docker compose up -d bootstrap
docker compose run --rm build_and_import_app
docker compose run --rm install_stack
```

### 3. Verify Installation

```bash
# Check pods are running
kubectl -n observability get pods
kubectl -n demo get pods

# Check services
kubectl -n observability get svc -l app.kubernetes.io/name=grafana -o wide
kubectl -n demo get svc
```

### 4. Access Grafana

- **URL**: http://localhost:3000
- **Login**: admin / admin
- **Dashboard**: "Demo App Overview" (auto-loaded)

## What This Sets Up

### Services Created

1. **bootstrap**: Creates the K3D cluster
   - K3D cluster: `grafana-lab` with 1 agent
   - Port mapping: `3000:30080@loadbalancer`

2. **build_and_import_app**: Builds the demo app image locally and imports it into the cluster (no registry)
   - Go application with Prometheus metrics

3. **install_stack**: Installs monitoring stack
   - kube-prometheus-stack (Prometheus + Grafana)
   - Demo application deployment
   - ServiceMonitor for metrics scraping
   - Grafana dashboard ConfigMap

### Namespaces

- **observability**: Prometheus, Grafana, AlertManager
- **demo**: Demo application

## Generate Test Traffic

```bash
# Port forward the demo app
kubectl -n demo port-forward deploy/demo-app 8080:8080 >/dev/null 2>&1 &

# Generate some traffic
curl -s http://localhost:8080/
curl -s http://localhost:8080/metrics | head

# Check metrics in Prometheus
kubectl -n observability port-forward svc/kps-prometheus-server 9090:80 >/dev/null 2>&1 &
# Open http://localhost:9090 and search for 'demo_'
```

## Key Metrics

The demo app exports these Prometheus metrics:

- **Total HTTP Requests**: `sum(demo_http_requests_total)`
- **Request Duration (p95)**: `histogram_quantile(0.95, sum(rate(demo_http_request_duration_seconds_bucket[5m])) by (le))`

## Troubleshooting

### Grafana Not Accessible

- **Check port mapping**: `docker ps | grep grafana-lab-serverlb`
- **Verify load balancer**: Port 3000 should map to 30080
- **Check if port 3000 is busy**: Use `lsof -i :3000` (macOS/Linux)

### Dashboard Not Appearing

1. **Check ConfigMap exists**:
   ```bash
   kubectl -n observability get cm -l grafana_dashboard=1
   ```
2. **Check Grafana sidecar logs**:
   ```bash
   kubectl -n observability logs -l app.kubernetes.io/name=grafana -c grafana-sc-dashboard
   ```

### ServiceMonitor Not Scraping

1. **Verify CRD exists**:
   ```bash
   kubectl get crd servicemonitors.monitoring.coreos.com
   ```
2. **Check ServiceMonitor labels**:
   ```bash
   kubectl -n demo get servicemonitor -o yaml
   ```
3. **Verify Service labels**:
   ```bash
   kubectl -n demo get svc demo-app -o yaml
   ```

### ImagePullBackOff

Using the no‑registry flow, ensure the image exists on nodes:

```bash
# Rebuild and import, then restart the deployment
docker compose run --rm build_and_import_app
kubectl -n demo rollout restart deploy/demo-app
```

### Clean Restart

If you need to start fresh:

```bash
# Stop and remove everything
docker compose down
k3d cluster delete grafana-lab || true

# Start over
docker compose up -d
```

## Manual Steps (Alternative to Docker Compose)

If you prefer manual setup with no registry:

```bash
# 1. Create cluster (no registry)
k3d cluster create grafana-lab --agents 1 --port 3000:30080@loadbalancer

# 2. Build and import image locally
docker build -t demo-app:latest ./app
k3d image import demo-app:latest -c grafana-lab

# 3. Install monitoring stack
kubectl apply -f k8s/ns.yaml
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install kps prometheus-community/kube-prometheus-stack --namespace observability --values helm-values/kube-prom-stack-values.yaml

# 4. Deploy demo app and dashboard
kubectl apply -f k8s/app-deploy.yaml
kubectl apply -f k8s/app-servicemonitor.yaml
kubectl apply -f k8s/grafana-dashboard-cm.yaml
```

## Teardown

```bash
# Remove everything
docker compose down
k3d cluster delete grafana-lab
k3d registry delete k3d-reg.localhost

# Optional: Clean up Docker images
docker image prune -f
```