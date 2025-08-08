#!/bin/bash
set -e

echo "Checking for Homebrew..."
if ! command -v brew >/dev/null 2>&1; then
  echo "Installing Homebrew..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

echo "Checking for Docker Desktop..."
if ! command -v docker >/dev/null 2>&1; then
  brew install --cask docker
  open -a Docker
  echo "Waiting for Docker to start..."
  while ! docker system info >/dev/null 2>&1; do sleep 1; done
fi

echo "Checking for Ollama..."
if ! command -v ollama >/dev/null 2>&1; then
  brew install ollama
fi

echo "Pulling DeepSeek Coder model..."
ollama pull deepseek-coder-v2:16b-lite-instruct-q4_K_M

echo "Setting up Open WebUI..."
mkdir -p ~/open-webui && cd ~/open-webui
cat <<EOF > docker-compose.yml
version: "3.8"
services:
  open-webui:
    image: ghcr.io/open-webui/open-webui:main
    ports:
      - "3000:3000"
    volumes:
      - open-webui-data:/app/backend/data
    environment:
      - OLLAMA_API_BASE_URL=http://localhost:11434
    restart: unless-stopped

volumes:
  open-webui-data:
EOF

read -p "Start Open WebUI now? (y/n): " choice
if [[ "$choice" =~ ^[Yy]$ ]]; then
  docker compose up -d
  echo "Open WebUI is running at http://localhost:3000"
else
  echo "You can start it later with: cd ~/open-webui && docker compose up -d"
fi