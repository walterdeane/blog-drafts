## Running a Local LLM for Code Assistance and Docs—Why I Did It, and How You Can Too

### Introduction: Why Bother Running a Local LLM?

I've been experimenting with using large language models to help write operational documentation—things like READMEs, runbooks, and ORRs. It's been productive, but I kept hitting the same concerns: privacy, cost, reliability, and how tightly I'm tethered to someone else’s API.

At some point, I decided I wanted to see how far I could get running everything locally. No API keys, no usage caps, no risk of sending sensitive code into the ether. Just a local LLM on my machine, ready to help when I need it.

Here's why that felt worth doing:

* **Privacy**: I don’t want to send every line of source code or config out to a cloud service, especially for work projects or NDA-bound clients. Also while this tutorial is primarily focused on a local llm for coding it can also be used as a personal GPT so privacy becomes more of an issue.
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

In future posts, I’ll show how to extend this setup: generating real operational documentation, templating Grafana dashboards, and optionally tying into remote services like Claude or others if you want a hybrid approach. But for now, the focus is on keeping everything local, simple, and under your control.

---

### Quick Setup Scripts

If you want to skip the manual steps and install the full stack with a single command, you can use one of these scripts:

#### macOS:

```bash
curl -fsSL https://raw.githubusercontent.com/YOUR_GITHUB_USER/local-llm-setup/main/setup-macos.sh | bash
```

#### Linux:

```bash
curl -fsSL https://raw.githubusercontent.com/YOUR_GITHUB_USER/local-llm-setup/main/setup-linux.sh | bash
```

The scripts install the required dependencies (Homebrew, Docker, Ollama), pull a good default model (DeepSeek Coder), and optionally start Open WebUI for a browser-based experience.

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

LLMs don’t need to be remote services. If you’ve got a laptop with enough RAM, you can run your own assistant, tune it to your own workflow, and stop depending on someone else’s infrastructure.

It’s not magic, but it is surprisingly effective—and it puts you back in control.

> **Aside:** If you're unsure about the complexity of hosting Ollama for external tools like Cursor, or want to learn how to use tools like `ngrok` securely, we’ll be covering that in a future deep-dive post. For now, local-first setups are the safest and easiest entry point.
