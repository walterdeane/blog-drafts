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
CUSTOM_MODELS=""
SELECTED_MODELS=()

usage() {
  echo "Usage: $0 [--prompt <dir-compare|json-parse|sort-algo>] [--models m1,m2,...]" >&2
  echo "  If no --models specified, you'll be prompted to select models interactively" >&2
}

# Parse args
while [ $# -gt 0 ]; do
  case "$1" in
    --prompt)
      [ $# -ge 2 ] || { echo "--prompt requires a value" >&2; usage; exit 1; }
      PROMPT_KEY="$2"; shift 2 ;;
    --models)
      [ $# -ge 2 ] || { echo "--models requires a value" >&2; usage; exit 1; }
      CUSTOM_MODELS="$2"; shift 2 ;;
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
if [ -n "$CUSTOM_MODELS" ]; then
  IFS=',' read -r -a MODELS <<< "$CUSTOM_MODELS"
  SELECTED_MODELS=("${MODELS[@]}")
else
  # Default model list
  MODELS=(
    "mistral:7b-instruct"
    "dolphin-mixtral:8x7b"
    "qwen3-coder:30b"
    "gpt-oss:20b"
    "codestral:22b-v0.1-q4_K_M"
    "codellama:13b-instruct-q5_K_M"
    "qwen2.5-coder:14b-instruct-q4_K_M"
    "deepseek-coder-v2:16b-lite-instruct-q4_K_M"
    "qwen2.5-coder:7b-instruct-q6_K"
    "qwen2.5-coder:7b-instruct-q5_K_M"
    "qwen2.5-coder:7b-instruct-q4_K_M"
  )
  
  # Interactive model selection
  if [ -t 0 ]; then
    echo "Available models:"
    i=1
    for model in "${MODELS[@]}"; do
      echo "  $i) $model"
      i=$((i+1))
    done
    echo
    echo "Enter model numbers to test (space-separated), then press Enter:"
    echo "Example: 1 3 5 (to test models 1, 3, and 5)"
    echo "Press Enter with no numbers to test all models"
    printf "Selection: "
    read -r selection
    
    if [ -n "$selection" ]; then
      # Parse the space-separated numbers
      for num in $selection; do
        if [[ "$num" =~ ^[0-9]+$ ]] && [ "$num" -ge 1 ] && [ "$num" -le "${#MODELS[@]}" ]; then
          SELECTED_MODELS+=("${MODELS[$((num-1))]}")
        else
          echo "⚠️  Invalid selection: $num (skipping)"
        fi
      done
    fi
    
    # If no valid selections, use all models
    if [ ${#SELECTED_MODELS[@]} -eq 0 ]; then
      SELECTED_MODELS=("${MODELS[@]}")
      echo "📊 Using all ${#MODELS[@]} models"
    else
      echo "📊 Selected ${#SELECTED_MODELS[@]} models for testing"
    fi
  else
    # Non-interactive mode: use all models
    SELECTED_MODELS=("${MODELS[@]}")
  fi
fi

echo "▶ Using prompt: [$PROMPT_KEY] $SELECTED_PROMPT"
echo "📊 Will test ${#SELECTED_MODELS[@]} models"
echo

for MODEL in "${SELECTED_MODELS[@]}"; do
  MODEL_TAG=$(echo "$MODEL" | tr '/:' '_')
  OUTPUT_FILE="$OUTPUT_DIR/${MODEL_TAG}_${PROMPT_KEY}.txt"

  echo "🚀 Warming up $MODEL..."
ollama run "$MODEL" --prompt "Say hello." > /dev/null 2>&1 || true


  echo "🏁 Benchmarking $MODEL with prompt [$PROMPT_KEY]..."

  # Check if model is installed
  if ! ollama list | awk '{print $1}' | grep -qx "$MODEL"; then
    echo "⚠️  Model $MODEL is not installed."
    if [ -t 0 ]; then
      printf "Download it now? [y/N]: "
      read -r response
      case "$response" in
        [Yy]*)
          echo "📥 Downloading $MODEL..."
          if ollama pull "$MODEL"; then
            echo "✅ Successfully downloaded $MODEL"
          else
            echo "❌ Failed to download $MODEL, skipping..."
            STATUS="download_failed"
            DURATION="0"
            RESPONSE="Failed to download model $MODEL"
            MODEL_TAG=$(echo "$MODEL" | tr '/:' '_')
            OUTPUT_FILE="$OUTPUT_DIR/${MODEL_TAG}_${PROMPT_KEY}.txt"
            echo "$RESPONSE" > "$OUTPUT_FILE"
            TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
            echo "$TIMESTAMP,$MODEL,$PROMPT_KEY,$STATUS,$DURATION,$OUTPUT_FILE" >> "$RESULTS_FILE"
            echo "⚠️  $RESPONSE"
            echo
            continue
          fi
        ;;
        *)
          echo "⏭️  Skipping $MODEL"
          STATUS="skipped"
          DURATION="0"
          RESPONSE="Model $MODEL was skipped by user"
          MODEL_TAG=$(echo "$MODEL" | tr '/:' '_')
          OUTPUT_FILE="$OUTPUT_DIR/${MODEL_TAG}_${PROMPT_KEY}.txt"
          echo "$RESPONSE" > "$OUTPUT_FILE"
          TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
          echo "$TIMESTAMP,$MODEL,$PROMPT_KEY,$STATUS,$DURATION,$OUTPUT_FILE" >> "$RESULTS_FILE"
          echo "⚠️  $RESPONSE"
          echo
          continue
        ;;
      esac
    else
      # Non-interactive mode: skip missing models
      echo "⏭️  Skipping $MODEL (non-interactive mode)"
      STATUS="skipped"
      DURATION="0"
      RESPONSE="Model $MODEL was skipped (non-interactive mode)"
      MODEL_TAG=$(echo "$MODEL" | tr '/:' '_')
      OUTPUT_FILE="$OUTPUT_DIR/${MODEL_TAG}_${PROMPT_KEY}.txt"
      echo "$RESPONSE" > "$OUTPUT_FILE"
      TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
      echo "$TIMESTAMP,$MODEL,$PROMPT_KEY,$STATUS,$DURATION,$OUTPUT_FILE" >> "$RESULTS_FILE"
      echo "⚠️  $RESPONSE"
      echo
      continue
    fi
  fi

  START_TIME=$(date +%s.%N)
  if RESPONSE=$(echo "$SELECTED_PROMPT" | ollama run "$MODEL" 2>&1); then
    STATUS="success"
  else
    STATUS="failure"
  fi
  END_TIME=$(date +%s.%N)
  DURATION=$(echo "$END_TIME - $START_TIME" | bc)

  # Sanitize output: strip ANSI CSI sequences, remove control chars except tab/newline, remove CR
  ESC=$'\033'
  CLEANED=$(printf "%s" "$RESPONSE" \
    | sed -E "s/${ESC}\\[[0-9;?]*[ -\/]*[@-~]//g" \
    | tr -d '\r' \
    | tr -d '\000-\010\013\014\016-\037' )
  echo "$CLEANED" > "$OUTPUT_FILE"

  TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  echo "$TIMESTAMP,$MODEL,$PROMPT_KEY,$STATUS,$DURATION,$OUTPUT_FILE" >> "$RESULTS_FILE"

  echo "✅ Output saved to $OUTPUT_FILE"
  echo "⏱️  Duration: ${DURATION}s | Status: ${STATUS}"
  echo
done

echo "📊 Benchmark complete. Results logged in $RESULTS_FILE"
