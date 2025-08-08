# Blog Drafts: AI Blogs and Tutorials

This repository contains drafts, code, and supporting assets for blogs and tutorials I’m writing about AI and LLMs. Each post lives in its own numbered directory under `src/`, often with runnable scripts and notes to reproduce results.

## Repository Structure

```text
src/
  001_setting-up-a-local-llm/
    001_setting-up-a-local-llm.md   # Main article draft
    README.md                       # Post-specific instructions
    benchmark-llms.sh               # Example scripts / code for the post
    ngrok-notes.md                  # Additional notes
```

## Getting Started

- Clone the repo and open it in your editor of choice.
- Posts are written in Markdown; any Markdown previewer works (e.g., VS Code’s built‑in preview).
- Some posts include scripts you can run locally. See each post’s `README.md` for requirements and usage.

## Working With Drafts

When starting a new post:

1. Create a new directory under `src/` following the convention: `NNN_kebab-case-title/` (e.g., `002_building-a-rag-pipeline/`).
2. Add a main article file named `NNN_kebab-case-title.md` inside that directory.
3. Include a `README.md` with instructions to reproduce any results or run included code.
4. Keep any assets (images, datasets, prompts) alongside the post or in a nested `assets/` subfolder.

## Posts Index

- Setting up a Local LLM (`src/001_setting-up-a-local-llm/`)
  - Article: `src/001_setting-up-a-local-llm/001_setting-up-a-local-llm.md`
  - Post README: `src/001_setting-up-a-local-llm/README.md`
  - Includes: an Ollama-based local LLM benchmark harness

## Running Included Scripts

Some posts ship with scripts to reproduce benchmarks or experiments. For example, in `src/001_setting-up-a-local-llm/`:

```bash
cd src/001_setting-up-a-local-llm
chmod +x benchmark-llms.sh
./benchmark-llms.sh --quant   # Compare quantized variants
./benchmark-llms.sh --models  # Compare model families
```

See the post’s `README.md` for prerequisites (e.g., Ollama, `jq`).

## Contributing / Feedback

- Suggestions and corrections are welcome. Open an issue or PR with clear context.
- If adding a post, follow the directory and filename conventions above and include reproducibility notes.

## Notes

- Content is a work in progress and may change. Scripts are intended for local experimentation—review and adapt before running.
