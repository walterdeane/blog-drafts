# LLM Benchmark Harness

This harness runs benchmark tests for local LLMs via Ollama to compare performance and output quality for coding prompts.

## Usage

```bash
chmod +x benchmark-llms.sh
./benchmark-llms.sh --quant   # Compare Deepseek Coder quant variants
./benchmark-llms.sh --models  # Compare Deepseek, Code LLaMA, and Mistral
```

## Requirements

- Ollama installed with required models
- jq installed (e.g., `brew install jq`)

## Output

- `llm-benchmark-<timestamp>/outputs/...`: Generated Java files
- `llm-benchmark-<timestamp>/benchmark-results.csv`: Token and timing log
