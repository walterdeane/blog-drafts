## Running a Local LLM for Code Assistance and Docs—Why I Did It, and How You Can Too

### Introduction: Why Bother Running a Local LLM?

I've been experimenting with using large language models to help write operational documentation—things like READMEs, runbooks, and ORRs. It's been productive, but I kept hitting the same concerns: privacy, cost, reliability, and how tightly I'm tethered to someone else’s API.

At some point, I decided I wanted to see how far I could get running everything locally. No API keys, no usage caps, no risk of sending sensitive code into the ether. Just a local LLM on my machine, ready to help when I need it.

Here's why that felt worth doing:

* **Privacy**: I don’t want to send every line of source code or config out to a cloud service, especially for work projects or NDA-bound clients. Also while this tutorial is primarily focused on a local llm for coding it can also be used as a personal GPT so privacy becomes more of an issue.
* **Cost**: LLMs aren’t cheap if you’re using them seriously. A few bucks here and there becomes a bill pretty quickly. As an individual developing at home  outside of work I appreciate how Gen AI can help me with projects that are challenging to do on your own, but my wife is not going to be happy if my credit card starts getting pummelled by fees from OpenAI or Anthropic or whatever new LLM pops up. Outside of my day job, most of my coding is for fun or  for open source so I want to keep the cost down. The other trend that is starting to happen is that the preview pricing is starting to run out and using the big models are suddenly not as free as we thought.
* **Cost**: LLMs aren’t cheap if you’re using them seriously. A few bucks here and there becomes a bill pretty quickly. As an individual developing at home  outside of work I appreciate how Gen AI can help me with projects that are challenging to do on your own, but my wife is not going to be happy if my credit card starts getting pummelled by fees from OpenAI or Anthropic or whatever new LLM pops up. Outside of my day job, most of my coding is for fun or  for open source so I want to keep the cost down. The other trend that is starting to happen is that the preview pricing is starting to run out and using the big models are suddenly not as free as we thought.
* **Uptime and token limits**: It’s easy to build something cool with GPT-4... right up until you hit your rate limit, the API goes down, or your token quota runs out. You run the risk of losing tools that  you are becoming overly dependent on. That’s a blog for another day.
* **Environmental impact**: Running a smaller model locally is often far more efficient than streaming requests back and forth to a massive data center. Right now, most of the GenAI industry is focused on building larger and more expensive models with ever-growing parameter counts. These models demand enormous power just to operate, and their energy cost per interaction adds up quickly. If we’re not careful, we’ll end up scaling inefficiency into everything. We need to start thinking seriously about how to get good enough results from models that don’t require server racks and carbon offsets just to answer a prompt.
* **Control**: I like knowing what’s going on under the hood. I don’t want a black box that only works if I follow someone else’s workflow. I want to shape the way I work, automate the boring parts, and experiment without waiting for someone to approve a SaaS license or unblock a firewall. At work, I don’t always get to make those decisions—but on my own machine, I do. My laptop, my rules.

In this post, I’ll walk through how I set up a local LLM for code assistance and documentation work. The end result is a working dev environment—with a model installed locally and integrated into a code editor—that can generate docs, answer questions, and help move work forward without needing the cloud.

### What You'll Have at the End

Once you’ve followed the steps in this guide, you’ll have a fully working local development setup that can assist you with code and documentation—no cloud required.

Specifically, you’ll have:

* A local LLM like Mistral or Phi-3 running through Ollama
* A code editor (Cursor or VS Code) connected to that model and ready to handle prompts
* A shell script you can run to feed code into the model and generate output like README files or runbooks
* A server running quietly in the background, ready to respond to your prompts without charging you by the token or phoning home

This setup works offline. It’s especially handy for hobby projects, open source work, or anything sensitive that you don’t want sent to someone else’s infrastructure. On a decently specced MacBook or desktop (16GB RAM or better), it runs smoothly. On lower-spec machines, you can still get good results by using smaller models.

By the time you’re done, you’ll have:

* A working local LLM (like Mistral or Phi-3) installed via Ollama
* Either Cursor or VSCode configured to talk to that LLM
* A server running locally as your own personal GPT.

You can run this setup completely offline if you want to. It works on Macs and other systems with enough memory (16GB+ is ideal, but less can work with smaller models).

In future posts, I’ll show how to extend this setup: generating real operational documentation, templating Grafana dashboards, and optionally tying into remote services like Claude or others if you want a hybrid approach. But for now, the focus is on keeping everything local, simple, and under your control.

### Step 1: Install Ollama and Run a Model Locally

I used Ollama because it’s fast to install, easy to run, and supports the kind of models that are good enough to be useful without needing a data center.

#### Install (macOS or Linux)

```bash
brew install ollama
```

Or grab the `.dmg` from the website and install it manually.

#### Run a model

```bash
ollama run mistral
```

This should launch a small REPL-like interface. You can start typing, and it’ll respond. That proves the model’s running.

To verify that the server is active and listening, open a new terminal and run:

```bash
curl http://localhost:11434/api/tags
```

If everything’s working, you’ll get a JSON list of installed models.

If you want a smaller or more efficient model:

```bash
ollama pull phi3:instruct
```

Both Mistral and Phi-3 are general-purpose and handle code reasonably well.

### Step 2: Set Up Your Editor

I’ve tried both Cursor and VS Code. Either works. Cursor is purpose-built for LLMs and supports local models out of the box. VS Code requires a plugin but gets the job done.

#### Option A: Cursor

Cursor works well with local LLMs, but connecting it to a local Ollama server does require some setup. By default, Cursor expects an accessible, network-routable endpoint—which `localhost` alone doesn't satisfy unless you expose it externally or modify local DNS.

You have a couple of options to make this work:

**Option 1: Use ngrok to expose Ollama externally**

We’re not covering full ngrok setup in this guide, but if you want to use Cursor with your local model from another machine—or even while you're away from home—ngrok is one way to do it. It creates a secure tunnel from the internet to your local machine.

That said, opening access to your machine comes with security implications. You should only use this approach if you're comfortable with managing access and authentication. A future post will walk through the setup in more detail, including how to secure it properly.

**Option 2: Modify your ********************************************************************************`/etc/hosts`******************************************************************************** to alias a name to localhost**

You can create a hostname like `ollama.local` pointing to `127.0.0.1`. This works well with tools that reject `localhost`:

```bash
echo '127.0.0.1 ollama.local' | sudo tee -a /etc/hosts
```

Then configure Cursor with:

```json
{
  "provider": "ollama",
  "model": "mistral",
  "endpoint": "http://ollama.local:11434"
}
```

Once configured, Cursor will let you run prompts from your local model, edit and test code completions, and integrate your own templates.

#### Option B: VS Code

* Install the “Continue” extension
* Open settings and configure the backend:

```json
{
  "provider": "ollama",
  "model": "mistral",
  "endpoint": "http://localhost:11434"
}
```

Now you can chat with the model, or run it against selected code.

### Step 3: Install Open WebUI for a Local Chat Interface

If you want a browser-based interface to interact with your local LLM—more visual than the REPL and easier to use than the editor plugin—you can install [Open WebUI](https://github.com/open-webui/open-webui). It gives you a clean web interface similar to ChatGPT, but entirely local.

#### Install with Docker (Recommended)

> **Note**: If you don't already have Docker installed, you can install it from [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop).
>
> On macOS, you can also use Homebrew:
>
> ```bash
> brew install --cask docker
> open /Applications/Docker.app
> ```
>
> After installing, make sure Docker is running before continuing.

```bash
docker run -d \
  --name open-webui \
  -p 3000:3000 \
  -e OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  -v open-webui-data:/app/backend/data \
  ghcr.io/open-webui/open-webui:main
```

On Linux, replace `host.docker.internal` with `localhost`.

#### Open the Interface

Once it's running, visit:

```
http://localhost:3000
```

You’ll see a chat UI where you can talk to your local model—just like you would with GPT-4 in the browser, but this time it’s all running on your own hardware.

You can select which model to use (like Mistral or Phi-3), adjust temperature settings, and drop in your own prompt templates. This is great for writing documentation, testing prompt variants, or just exploring how far you can push a small model locally.

It’s a nice complement to using LLMs from the command line or your editor—and it’s fast, private, and free.

### Bonus: One-Line Script to Install Everything

If you want to automate the entire setup—from installing Docker and Ollama to pulling a model and launching Open WebUI—you can use this script. Copy and paste it into a file (e.g., `setup-llm.sh`), make it executable, and run it.

```bash
#!/bin/bash

set -e

echo "📦 Installing Homebrew (if needed)..."
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" || true

echo "🐳 Installing Docker Desktop..."
brew install --cask docker

echo "⏳ Starting Docker (wait until it finishes initializing)..."
open -a Docker
while ! docker system info > /dev/null 2>&1; do
  sleep 1
done

echo "🧠 Installing Ollama..."
brew install ollama
ollama serve &

echo "⬇️ Pulling LLM (LLaMA3 by default)..."
ollama pull llama3

echo "🧰 Setting up Open WebUI with Docker Compose..."
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

docker compose up -d

echo "✅ Setup complete! Visit http://localhost:3000 in your browser."
```

### Final Thoughts

LLMs don’t need to be remote services. If you’ve got a laptop with enough RAM, you can run your own assistant, tune it to your own workflow, and stop depending on someone else’s infrastructure.

It’s not magic, but it is surprisingly effective—and it puts you back in control.
