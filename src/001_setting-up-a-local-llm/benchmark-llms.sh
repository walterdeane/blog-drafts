#!/bin/bash
set -e

OUTPUT_DIR="output"
RESULTS_FILE="benchmark-results.csv"

mkdir -p "$OUTPUT_DIR"
touch "$RESULTS_FILE"

# Define prompts
declare -A PROMPTS
PROMPTS["dir-compare"]="Write a Java class that recursively compares two directories using java.nio.file. Return added, removed, or modified files."
PROMPTS["json-parse"]="Write a Python function to parse a large JSON file in chunks using a generator."
PROMPTS["sort-algo"]="Implement quicksort in Rust with comments explaining each step."

# Prompt selector
echo "Select a prompt:"
select PROMPT_KEY in "${!PROMPTS[@]}"; do
  if [[ -n "$PROMPT_KEY" ]]; then
    SELECTED_PROMPT="${PROMPTS[$PROMPT_KEY]}"
    break
  else
    echo "Invalid selection"
  fi
done

# Model list
MODELS=(
  "gpt-oss:20b"
  "codestral:22b-v0.1-q4_K_M"
  "codellama:13b-instruct-q5_K_M"
  "qwen2.5-coder:14b-instruct-q4_K_M"
  "deepseek-coder-v2:16b-lite-instruct-q4_K_M"
  "qwen2.5-coder:7b-instruct-q6_K"
  "qwen2.5-coder:7b-instruct-q5_K_M"
  "qwen2.5-coder:7b-instruct-q4_K_M"
)

echo "▶ Using prompt: [$PROMPT_KEY] $SELECTED_PROMPT"
echo

for MODEL in "${MODELS[@]}"; do
  MODEL_TAG=$(echo "$MODEL" | tr '/:' '_')
  OUTPUT_FILE="$OUTPUT_DIR/${MODEL_TAG}_${PROMPT_KEY}.txt"

  echo "🚀 Warming up $MODEL..."
  echo "Say hello." | ollama run "$MODEL" > /dev/null 2>&1 || true

  echo "🏁 Benchmarking $MODEL with prompt [$PROMPT_KEY]..."

  START_TIME=$(date +%s.%N)
  RESPONSE=$(echo "$SELECTED_PROMPT" | ollama run "$MODEL")
  END_TIME=$(date +%s.%N)
  DURATION=$(echo "$END_TIME - $START_TIME" | bc)

  echo "$RESPONSE" > "$OUTPUT_FILE"

  echo "$(date -Iseconds),$MODEL,$PROMPT_KEY,$DURATION,$OUTPUT_FILE" >> "$RESULTS_FILE"

  echo "✅ Output saved to $OUTPUT_FILE"
  echo "⏱️  Duration: ${DURATION}s"
  echo
done

echo "📊 Benchmark complete. Results logged in $RESULTS_FILE"
