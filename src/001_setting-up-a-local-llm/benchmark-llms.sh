#!/bin/bash
set -e

OUTPUT_DIR="output"
RESULTS_FILE="benchmark-results.csv"

mkdir -p "$OUTPUT_DIR"
touch "$RESULTS_FILE"

# Prompt keys and resolver (compatible with macOS bash 3.2)
PROMPT_KEYS=(
  "dir-compare"
  "json-parse"
  "sort-algo"
)

prompt_text() {
  case "$1" in
    "dir-compare")
      echo "Write a Java class that recursively compares two directories using java.nio.file. Return added, removed, or modified files." ;;
    "json-parse")
      echo "Write a Python function to parse a large JSON file in chunks using a generator." ;;
    "sort-algo")
      echo "Implement quicksort in Rust with comments explaining each step." ;;
    *)
      return 1 ;;
  esac
}

PROMPT_KEY=""

usage() {
  echo "Usage: $0 [--prompt <dir-compare|json-parse|sort-algo>]" >&2
}

# Parse args
while [ $# -gt 0 ]; do
  case "$1" in
    --prompt)
      [ $# -ge 2 ] || { echo "--prompt requires a value" >&2; usage; exit 1; }
      PROMPT_KEY="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

# Select prompt (non-interactive default if stdin not a TTY)
if [ -z "$PROMPT_KEY" ]; then
  if [ -t 0 ]; then
    echo "Select a prompt:"
    i=1
    for key in "${PROMPT_KEYS[@]}"; do
      echo "  $i) $key"
      i=$((i+1))
    done
    printf "Enter number [1-%d]: " ${#PROMPT_KEYS[@]}
    read sel
    case "$sel" in
      1) PROMPT_KEY="${PROMPT_KEYS[0]}" ;;
      2) PROMPT_KEY="${PROMPT_KEYS[1]}" ;;
      3) PROMPT_KEY="${PROMPT_KEYS[2]}" ;;
      *) echo "Invalid selection" >&2; exit 1 ;;
    esac
  else
    PROMPT_KEY="dir-compare"
  fi
fi

SELECTED_PROMPT=$(prompt_text "$PROMPT_KEY") || { echo "Unknown prompt key: $PROMPT_KEY" >&2; exit 1; }

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

  # Skip if model isn't installed to avoid long pulls
  if ! ollama list | awk '{print $1}' | grep -qx "$MODEL"; then
    STATUS="missing"
    DURATION="0"
    RESPONSE="Model $MODEL is not installed. Skipping."
    MODEL_TAG=$(echo "$MODEL" | tr '/:' '_')
    OUTPUT_FILE="$OUTPUT_DIR/${MODEL_TAG}_${PROMPT_KEY}.txt"
    echo "$RESPONSE" > "$OUTPUT_FILE"
    TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    echo "$TIMESTAMP,$MODEL,$PROMPT_KEY,$STATUS,$DURATION,$OUTPUT_FILE" >> "$RESULTS_FILE"
    echo "⚠️  $RESPONSE"
    echo
    continue
  fi

  START_TIME=$(date +%s.%N)
  if RESPONSE=$(echo "$SELECTED_PROMPT" | ollama run "$MODEL" 2>&1); then
    STATUS="success"
  else
    STATUS="failure"
  fi
  END_TIME=$(date +%s.%N)
  DURATION=$(echo "$END_TIME - $START_TIME" | bc)

  echo "$RESPONSE" > "$OUTPUT_FILE"

  TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  echo "$TIMESTAMP,$MODEL,$PROMPT_KEY,$STATUS,$DURATION,$OUTPUT_FILE" >> "$RESULTS_FILE"

  echo "✅ Output saved to $OUTPUT_FILE"
  echo "⏱️  Duration: ${DURATION}s | Status: ${STATUS}"
  echo
done

echo "📊 Benchmark complete. Results logged in $RESULTS_FILE"
