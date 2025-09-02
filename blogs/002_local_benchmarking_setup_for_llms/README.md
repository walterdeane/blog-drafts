# Local LLM Benchmarking Blog

This directory contains a blog post and a small harness for benchmarking local LLMs (via Ollama) on coding-style prompts. It shows how to run reproducible comparisons across models and quantizations, collect token/timing metrics, and diff generated code outputs.

## What’s here

- `002_local_benchmarking_setup_for_llms.md`: The full blog post (draft) describing motivation, setup, and methodology.
- `benchmark-llms.sh`: A Bash script to run repeatable benchmarks against one or more Ollama models/quantizations.
- `benchmark-results.csv`: Example results from a prior run (tokens, time, throughput).
- `DirectoryComparator.java`, `DirectoryDiff.java`: Utilities to compare model output directories and summarize diffs.
- `output/`: Sample diff reports from past runs (one file per model/config).

## Prerequisites

- Ollama installed with required models pulled
- Bash and `jq` (e.g., install with `brew install jq` on macOS)
- Java 17+ (to compile/run the directory diff utilities)

## Quick start

```bash
cd blogs/002_local_benchmarking_setup_for_llms
chmod +x benchmark-llms.sh

# Compare quantization variants of a single family (e.g., DeepSeek Coder)
./benchmark-llms.sh --quant

# Compare a small set of different coding models
./benchmark-llms.sh --models
```

The script orchestrates prompts through Ollama, logs token/timing metrics, and writes all artifacts under a timestamped run directory.

## Outputs

- `llm-benchmark-<timestamp>/outputs/...`: Generated Java files for each prompt and model variant
- `llm-benchmark-<timestamp>/benchmark-results.csv`: Aggregate token counts, durations, and throughput
- `output/*.txt`: Human-readable directory-compare reports produced by the Java utilities

## Reading the blog

For the full methodology, selection of models, prompt design, and interpretation of results, see:

- `002_local_benchmarking_setup_for_llms.md`

It walks through installing prerequisites, structuring prompts, running the harness, and making sense of quality and performance trade-offs.

## Customizing

- Edit `benchmark-llms.sh` to change models, prompts, and run parameters.
- Use `DirectoryComparator.java` and `DirectoryDiff.java` to compare outputs across runs or models.

## Notes

- Ensure models are available in Ollama before running (e.g., `ollama pull <model>`). The script will do this for you but I just pulled them before hand so i didn't have to wait.
- The sample CSV and diff outputs are included for reference; your results will vary by hardware and model versions.
