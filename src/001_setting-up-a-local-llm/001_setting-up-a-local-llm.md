# Running a Local LLM for Code Assistance and Docs—Why I Did It, and How You Can Too

## Introduction: Why Bother Running a Local LLM?

I’ve been experimenting with using large language models to help write operational documentation—things like READMEs, runbooks, and ORRs. It’s been productive, but I kept running into the same issues: **privacy, cost, reliability, and vendor lock-in.**

At some point, I decided I wanted to see how far I could get by running everything locally. No API keys. No usage caps. No risk of sending sensitive code into the cloud. Just a model running on my machine—ready to help when I need it.

Beyond those high-level concerns, I also wanted to test something more personal: **how practical does it really feel to use an LLM on my own system?** Would it be usable day to day, and what compromises would I need to make? I also wanted a framework for quickly comparing one LLM against another without friction.  

Another goal was to set up a local environment for **experimenting with agentic apps** and getting hands-on with tools like **LangChain** and **MCPs**—all without pushing everything into AWS or another external service that might balloon my costs.

Here’s why it felt worth doing:

- **Privacy**: I don’t want to send every line of source code or config out to a third-party service, especially for work projects or NDA-bound clients. Even for personal projects, privacy matters if you’re using a local LLM as a “personal GPT.”
- **Cost**: Cloud LLMs aren’t cheap. A few bucks here and there quickly adds up. Outside of my day job, most of my coding is for fun or open source. I don’t want surprise bills just for using a sidekick. Also, preview pricing is starting to disappear—suddenly “free” is not so free.
- **Uptime and token limits**: GPT-5 is great… until you hit your rate limit, the API goes down, or your token quota resets. Relying on an external service means you’re always at risk of losing tools you depend on.
- **Environmental impact**: Running a small-to-mid model locally is often more efficient than round-tripping to a massive data center. The AI industry is chasing ever-bigger models, but “good enough” models can save compute, cost, and carbon.
- **Control**: On my own machine, I get to decide how things work. I can script workflows, automate boring parts, and experiment freely—without waiting for someone to approve a SaaS license or poke a firewall hole.

In this post, I’ll walk through how to set up a local LLM for code assistance and documentation. The goal: a dev environment with a model installed locally and integrated into an editor—able to generate docs, answer coding questions, and stay fully offline.

## Setting Realistic Expectations

Before you dive in, it’s important to set the right expectations. Running a model locally is useful—but it comes with trade-offs.

- **Speed**: In my benchmarks, local inference times ranged between **110 and 598 seconds** depending on the model and context. By comparison, Cursor and ChatGPT responded in **52–56 seconds**. On laptops, you should expect slower responses than the cloud-hosted giants.
- **Quality**: Bigger models generally give better, more coherent output. Local models are constrained by what your hardware can run. Expect shorter context windows, more occasional errors, and responses that sometimes need more hand‑holding.
- **Hardware limitations**: On a laptop, you’re bound by RAM and CPU/GPU power. A more powerful desktop with more memory or a dedicated GPU will give faster, higher‑quality results.

The takeaway: running locally isn’t about outperforming GPT‑5. It’s about **privacy, cost control, and independence**. For many docs and coding workflows, “good enough” is exactly what you need.

- **Warm‑up matters**: Local models often respond sluggishly on the very first prompt but speed up noticeably after they’ve loaded into memory. Don’t judge performance until after the warm‑up.
- **Prompting discipline**: Because responses take longer, good prompting technique becomes even more important. Clear, specific prompts reduce wasted cycles and help you get useful output faster.
- **Editor support caveats**: Tools like Continue and Cline work directly with local Ollama endpoints. By contrast, Cursor currently requires exposing your localhost to the public internet, which raises security concerns. Until Cursor adds true local support, expect to use Continue or Cline for the best local‑first workflow.

---

## What You’ll Have at the End

By the time you finish this guide, you’ll have:

- A local LLM (e.g. DeepSeek Coder or Phi‑3) running through [Ollama](https://ollama.ai)
- A code editor (e.g. VS Code) connected to that model

This setup works offline. On a reasonably specced MacBook or desktop (16–24GB RAM+), it runs smoothly. Smaller machines can still get good results by using lighter-weight models.

In future posts, I’ll cover extensions: generating real operational docs, templating Grafana dashboards, and hybrid setups that fall back to cloud models when needed. But here, we’ll keep things local, simple, and under your control.

---

## Step-by-Step Tutorial

Let’s walk through the setup one piece at a time. Instead of just dropping a script, I’ll explain the **how** and the **why** at each stage so you understand what’s happening. The first installs are for Homebrew on macOS if you don't already have it installed and Docker Desktop, which I used to run the [Open WebUI](https://openwebui.com). Open WebUI is a browser app you can use to talk to [Ollama](https://ollama.ai) without leaving your terminal.

### 1. Install Homebrew (macOS only)

Homebrew is the package manager that makes it easy to install the rest of the stack.

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

**Why:** This gives us a consistent way to install Docker and [Ollama](https://ollama.ai) without manual downloads.

---

### 2. Install Docker Desktop

Docker provides the container runtime we’ll use for **[Open WebUI](https://openwebui.com)**, the browser-based interface.

```bash
brew install --cask docker
open -a Docker
```

**Why:** Open WebUI lives inside a container for clean isolation and easy updates.

---

### 3. Install [Ollama](https://ollama.ai)

[Ollama](https://ollama.ai) is the local runtime that manages and serves LLMs. Think of it as the engine under the hood—it downloads, runs, and manages models on your machine. It also provides a simple HTTP API (`http://localhost:11434`) so other apps (like editors, scripts, or web UIs) can talk to your model as if it were a cloud service.

```bash
brew install ollama
```

**Why:** Without [Ollama](https://ollama.ai), you’d be left juggling model weights and inference servers yourself. [Ollama](https://ollama.ai) abstracts that complexity away, making it almost as easy as running a cloud API—but everything stays on your laptop. This is central to the blog’s purpose: local-first code assistance without cloud lock-in.

---

### 4. Pull a coding-focused model

```bash
ollama pull deepseek-coder-v2:16b-lite-instruct-q4_K_M
```

**Why:** DeepSeek Coder is tuned for programming and documentation. The quantized `q4_K_M` version runs well on a 16GB+ machine without overwhelming your RAM.

---

### 5. Set up Open WebUI

[Open WebUI](https://openwebui.com) is a lightweight web application that provides a chat-style interface for your local models. It’s essentially a browser-based “front end” where you can test prompts, iterate on documentation drafts, and compare model responses—all without leaving `localhost`.

We’ll use Docker Compose to spin it up in a self-contained container:

```bash
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
```

**Why:** While [Ollama](https://ollama.ai) is the engine, [Open WebUI](https://openwebui.com) is the dashboard. It makes interacting with your model accessible and visual—helpful when you’re experimenting with prompts or drafting docs. Running it in Docker keeps the install clean and ensures you can update or remove it easily later.

---

### 6. Start Open WebUI

```bash
docker compose up -d
```

Then open your browser to [http://localhost:3000](http://localhost:3000).

**Why:** This is your local “playground.” You can test model responses before wiring them into your editor or automation scripts.

---

### 7. Smoke test your setup

```bash
curl http://localhost:11434/api/tags | jq .
```

If it returns available models, [Ollama](https://ollama.ai) is working. If you see the [Open WebUI](https://openwebui.com) homepage, your setup is complete.

---

### 8. Configure **Continue** (VS Code) to use Ollama

**Goal:** Use your local model for chat and inline completions directly in VS Code via the Continue extension.

1. **Install Continue**  
   In VS Code, go to Extensions → search for **Continue** → Install. You’ll see a rocket icon in the sidebar.

2. **Auto‑detect (usually Just Works)**  
   With [Ollama](https://ollama.ai) running on `http://localhost:11434`, Continue will typically **auto‑detect** it and list it as a local provider. Open the Continue sidebar → **Models/Providers** and verify **Ollama (localhost:11434)** appears.

   - If you don’t see it, check that Ollama is running (`ollama serve` or any `ollama run` command), and that nothing else is using port **11434**.

3. **Manual configuration (fallback)**  
   Use the Command Palette → **Continue: Open Config**. Add or replace with a minimal config that targets your pulled model:

   ```json
   {
     "models": [
       {
         "title": "DeepSeek Coder (local)",
         "provider": "ollama",
         "model": "deepseek-coder-v2:16b-lite-instruct-q4_K_M",
         "host": "http://localhost:11434"
       }
     ],
     "defaultModel": "DeepSeek Coder (local)",
     "chatModel": { "provider": "ollama", "model": "deepseek-coder-v2:16b-lite-instruct-q4_K_M", "host": "http://localhost:11434" },
     "tabAutocompleteModel": { "provider": "ollama", "model": "deepseek-coder-v2:16b-lite-instruct-q4_K_M", "host": "http://localhost:11434" }
   }
   ```

   - Swap the `model` value to any tag you’ve pulled (see `ollama list`).
   - `host` is optional if you’re on the default `localhost:11434`, but including it makes intent explicit.

4. **Test inside VS Code**  
   - Open the Continue sidebar and ask a simple question.  
   - In an editor file, press **Tab** to trigger inline suggestions (if enabled) and confirm suggestions come from your local model.

**Troubleshooting**
- *“no such model”*: pull it first, e.g. `ollama pull deepseek-coder-v2:16b-lite-instruct-q4_K_M`.
- *Hangs or timeouts*: confirm `curl http://localhost:11434/api/tags` works; restart Ollama (`pkill ollama && ollama serve`) and VS Code; ensure only one tool is binding to 11434.
- *Slow first response*: warm‑up is normal; try one short prompt before benchmarking.

---

## Hooking Up VS Code (and Other Editors)

### Continue vs. Cline: which should you use?

**Short answer:** Start with **Continue** for broader features and polish. Choose **Cline** if you want something lighter with fewer moving parts.

| Area | Continue | Cline | Why it matters |
|---|---|---|---|
| Setup with Ollama | Auto‑detects `localhost:11434`; simple manual config | Manual but straightforward | Faster time‑to‑first‑prompt vs. explicit control |
| Inline completions | Mature Tab/inline suggestions | Basic inline assist (varies by version) | In‑file flow vs. chat‑only |
| Chat & tools | Rich chat, file tree, context selection, actions | Lean chat with simpler UI | Depth vs. simplicity |
| Multi‑agent / workflows | Supported (e.g., draft → refactor → test) | Limited | Useful for docs pipelines |
| Context from repo | Good controls to include/exclude files | Simpler context handling | Precision matters for larger repos |
| Extensibility | Active OSS community, frequent updates | Smaller surface area | Longevity & ecosystem |
| Resource footprint | Heavier | Lighter | Matters on lower‑spec laptops |
| Privacy/telemetry | Local by default; telemetry opt‑outs | Local by default; minimal telemetry | Local‑first principle |

**Recommendation:**
- Pick **Continue** if you want **inline completions + rich chat + workflows** and plan to live in one extension for most tasks (docs drafting, refactors, tests).
- Pick **Cline** if you prefer **minimal UI, lower overhead**, and you mostly want a **simple chat with the model** without extra panels or features.
- Keep **Cursor** on your radar if you want an editor that bakes AI deeper into everyday actions.

⚠️ **Note on Cursor:** At the time of writing, Cursor cannot easily use a local LLM on `localhost`. It expects a publicly reachable endpoint, which means you would need to expose your system to the internet (e.g., with ngrok). This introduces security concerns and undermines the privacy advantage of local‑first setups. Until Cursor adds native support for local endpoints, Continue or Cline are safer ways to connect directly to Ollama.

Once your [Ollama](https://ollama.ai) server is running on `http://localhost:11434`, you can plug it into your editor for a more seamless workflow.

### VS Code extensions

- **Continue**: An open‑source extension that lets you connect to local or remote LLMs. You can point it at your [Ollama](https://ollama.ai) endpoint and use it like Copilot, but fully local. It has a larger community, supports multi‑agent setups, and integrates tightly with VS Code.
- **Cline**: Another extension focused on coding with local models. It offers a simpler interface and a lighter footprint. However, it generally has fewer advanced features than Continue. Unless you specifically prefer its streamlined UI, Continue is the more compelling choice for most developers.

---

### Quick Setup Scripts

If you want to skip the manual steps and install the full stack with a single command, you can use one of these scripts:

#### macOS:

```bash
curl -fsSL https://raw.githubusercontent.com/YOUR_GITHUB_USER/local-llm-setup/main/setup-macos.sh | bash
```

The scripts install the required dependencies (Homebrew, Docker, Ollama), pull a good default model (DeepSeek Coder), and optionally start Open WebUI for a browser-based experience.

#### Linux:

```bash
curl -fsSL https://raw.githubusercontent.com/YOUR_GITHUB_USER/local-llm-setup/main/setup-linux.sh | bash
```

You’ll be prompted to choose whether Open WebUI should autostart.

For more advanced benchmarking and code generation use cases, check out the separate benchmarking script included in the repo:

```bash
curl -fsSL https://raw.githubusercontent.com/YOUR_GITHUB_USER/local-llm-setup/main/benchmark.sh -o benchmark.sh
chmod +x benchmark.sh
./benchmark.sh
```

This script compares model variations (e.g., Q4 vs Q5 vs Q6), logs performance and output to CSV, and stores generated code in organized folders so you can test and validate the results.

---

### Final Thoughts

Running a local LLM has been a mixed but interesting experience. On the plus side, the **chat was usable** and even with my limited hardware it gave me enough help to draft documentation and code. I did manage to get autocomplete working through plugins, but it felt **really slow** on my system with limited RAM. Honestly, if I had known how close we were to the AI wave, I would have bought a machine with far more memory. It reminded me of the 1990s—shutting everything else down just to run Photoshop. Nostalgia, but not always the good kind.

For the future, I’d like a **beefier desktop or a laptop with more RAM** (unfortunately mine isn’t upgradable). The performance also depends heavily on how “warm” Ollama is—the model seems faster after the first few prompts. Even so, I’ll keep using it: for coding, it does work, and let’s be honest—even with the lag it’s still faster than starting from scratch. It just isn’t as fast or smooth as Cursor or a cloud subscription.

One area I’m excited to explore is using a **local LLM as a personal knowledge management system**. I also love the freedom to try different models and pick the best one for each job. Open source is moving fast, and I believe these tools will only get better as the community continues to innovate within consumer hardware limits.

A practical tweak I may try is using an **AI CLI** workflow to reduce the overhead from running editors and browsers alongside the LLM. For these benchmarks I shut everything else down—which worked, but isn’t realistic for day‑to‑day use unless you’re fine with being totally cut off while coding.

In short: running local LLMs is already useful, if not yet frictionless. With better hardware and smarter workflows, I expect the experience will keep improving—and I’ll keep experimenting.

