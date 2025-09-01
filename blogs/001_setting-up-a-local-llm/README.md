# Setting Up a Local LLM for Code Assistance

This directory contains resources for setting up and running a local Large Language Model (LLM) on your machine for code assistance and documentation generation. The goal is to provide a privacy-focused, cost-effective alternative to cloud-based AI services while maintaining offline functionality.

## What You'll Get

By following the setup guides in this directory, you'll have:
- A local LLM (DeepSeek Coder) running through Ollama
- A web-based interface (Open WebUI) for interacting with your model
- The ability to generate documentation, answer coding questions, and assist with development tasks
- Complete offline functionality with no API keys or usage limits

## Contents

### 📖 **001_setting-up-a-local-llm.md**
The main blog post that explains the motivation behind running a local LLM and provides a comprehensive step-by-step tutorial. Covers:
- Why run a local LLM (privacy, cost, reliability, control)
- Setting realistic expectations about performance
- Detailed setup instructions for macOS
- Integration with code editors

### 🖥️ **setup-macos.sh**
Automated setup script for macOS users that:
- Installs Homebrew (if needed)
- Installs Docker Desktop
- Installs Ollama
- Downloads the DeepSeek Coder model
- Sets up Open WebUI with Docker Compose

### 🐧 **linux-setup.sh**
Similar automated setup script for Linux users that:
- Updates package lists
- Installs Docker and Docker Compose
- Installs Ollama
- Downloads the DeepSeek Coder model
- Sets up Open WebUI with Docker Compose

## Quick Start

### macOS
```bash
chmod +x setup-macos.sh
./setup-macos.sh
```

### Linux
```bash
chmod +x linux-setup.sh
./linux-setup.sh
```

## System Requirements

- **RAM**: 16GB+ recommended
- **Storage**: At least 10GB free space for models
- **OS**: macOS 10.15+ or Linux (Ubuntu 18.04+)
- **Docker**: Required for Open WebUI

## What This Solves

- **Privacy**: No code or data leaves your machine
- **Cost**: No per-token charges or API limits
- **Reliability**: Works offline, no external service dependencies
- **Control**: Full control over your AI development environment
- **Environmental Impact**: Often more efficient than cloud round-trips

## Performance Expectations

- **Response Time**: 110-598 seconds (slower than cloud services but acceptable for development)
- **Quality**: Good enough for documentation and coding assistance
- **Warm-up**: First response may be slower, subsequent responses improve

## Next Steps

After setup, you can:
- Integrate with VS Code or other editors
- Generate operational documentation
- Create runbooks and READMEs
- Experiment with agentic applications
- Set up hybrid cloud/local workflows

## Support

The main blog post (`001_setting-up-a-local-llm.md`) contains detailed troubleshooting and additional context. For issues with the setup scripts, check that Docker is running and you have sufficient system resources.
