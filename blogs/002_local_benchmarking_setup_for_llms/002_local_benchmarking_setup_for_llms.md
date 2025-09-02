# Benchmarking Local LLMs with Ollama and a Simple Bash Script

### Intro
In my [last post](https://medium.com/@walterdeane/setting-up-a-local-llm-for-code-assistance), I walked through setting up a local LLM for code assistance. Once I had it running, the next obvious question was:  

**How practical is it, really, on my hardware?**  

It’s one thing to run a model. It’s another to know if it responds quickly enough for coding, writing, or other tasks. If it is too slow im not really going to use it for day to day programming. The setup is still helpful for when im learning to use ai itself like setting up a local vector database and using RAG or prototyping an MCP, but it wouldn't be something I would suffer through. I hat staring at builds so i'm not going to want to suffer through that everytime I want a an autocomplete. To find out if this would work for me and what LLM works best, I wrote a small Bash script that benchmarks Ollama models. 

I’ve put both the script and my benchmark data in a GitHub repo so you can run the tests yourself:  
📂 [GitHub repo](https://github.com/walterdeane/blog-drafts/tree/main/blogs/002_local_benchmarking_setup_for_llms)

---
The data isn't completed I'm still playing with models but I hoped it could be useful to someone else. The sample output and csv are more for demostration sake. You can download the script tweak it to whatever LLMs you want and give it a go.

### Why Benchmark?
- Different models (Mistral, Codestral, Llama 2, DeepSeek, Qwen) perform very differently.  
- Quantization (q4 vs q5, etc.) changes both memory footprint and speed.  
- Benchmarks make the trade-offs visible: sometimes a smaller, “weaker” model is more usable because it streams tokens faster.  

---

### The Script
Here’s the script I use. It times execution, saves output, and logs results to a CSV file. I did a bit of poking around on what were good LLMs for a local machine. and I wanted to test a few things. The LLMs I chose have variations in parameters, quantization, whether they were instruct versions, and some are MoE models.  I don't really break them down in this blog. I might do another blog on what all of that means. This blog is just for sharing the script so that you can get started comparing on your own system.

```bash
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
```



📂 [Script on GitHub](https://github.com/walterdeane/blog-drafts/blob/main/blogs/002_local_benchmarking_setup_for_llms/benchmark-llms.sh)



### Script Features
This isn’t just a barebones timer — the script has a few features that make it hopefully more useful for real benchmarking:

- **Built-in model list (customizable):** You can run it against common local models out-of-the-box, or pass your own model names on the command line.  
- **Auto-downloads models if missing:** If Ollama doesn’t find a model locally, it will pull it for you the first time you run it. That way, you don’t need to pre-install every model you want to test. It also warms up the model with a simple call before benchmarking so the output is closer to how it will work when you are using it. 
- **CSV logging:** Each run writes a row into `benchmark-results.csv`, including model name, duration, status, and the path to the raw output. Perfect for comparing runs later or importing into a spreadsheet.  
- **Saved outputs:** Each model’s raw response is written to a text file. This lets you evaluate not just *how fast* a model runs, but also *what kind of output* it produces.  I might add a code quality checker in in a later version.
- **Flexible prompts:** The script uses a default coding-related prompt, but you can easily swap it out with something closer to your real workload (translation, summarization, Q&A, etc.).  

Together, these features let you evaluate not only raw speed but also whether a model feels “good enough” for your actual use case on your hardware.


---

### Running the Benchmark
Make the script executable and run it against one or more models:

```bash
chmod +x benchmark-llms.sh
./benchmark-llms.sh mistral:7b-instruct codellama:13b qwen2.5-coder:7b-instruct-q5_K_M
```

This will log results to the terminal and append them to `benchmark-results.csv`. There are several options in the script. You could just call it without a list and it will prompt you instead. LLMs get updated and superceded very quickly so you will probably want to edit the list in the script anyway.

---

### Example Results
Here’s one run from my machine. **Don’t expect your numbers to match mine** — results vary widely depending on CPU/GPU, RAM, and which models/quantizations you load. The whole point of this script is to have a way to test on your local system how well it works for you. A lot of AI blogs are engagement bait with wild claims either way about how evil or good AI is. Hopefully, by running this locally you will have a way to make your own opinion.

📊 [Full CSV of my results](https://raw.githubusercontent.com/walterdeane/blog-drafts/refs/heads/main/blogs/002_local_benchmarking_setup_for_llms/benchmark-results.csv)

A snippet in ASCII table form:

```
+--------------------------------------------+-----------------+------+
| Model                                      | Mean Duration s | Runs |
+--------------------------------------------+-----------------+------+
| qwen2.5-coder:7b-instruct-q5_K_M           | 110.91          | 1    |
| deepseek-coder-v2:16b-lite-instruct-q4_K_M | 120.70          | 1    |
| qwen2.5-coder:7b-instruct-q4_K_M           | 147.39          | 1    |
| mistral:7b-instruct                        | 209.98 (avg)    | 2    |
| codellama:13b-instruct-q5_K_M              | 229.38          | 1    |
| qwen2.5-coder:14b-instruct-q4_K_M          | 295.48          | 1    |
| codestral:22b-v0.1-q4_K_M                  | 385.22          | 1    |
| gpt-oss:20b                                | 598.51          | 1    |
+--------------------------------------------+-----------------+------+
```

---

### Takeaways
- **Speed matters as much as smarts.** A smaller model that streams quickly often feels better to use.  
- **Quantization is key.** Switching between q4 and q5 changes usability and memory footprint.  
- **Measure, don’t guess.** Your hardware will give very different results than mine.  

---

### Extending the Script
- Add multiple prompts and average results.  
- Compare different quantization levels directly.  
- Append results from multiple runs into a master CSV to track changes over time.  

---

### Final Thoughts
Running LLMs locally still feels like the 90s — closing apps, watching memory, hoping nothing crashes. But with a small Bash script, you can **measure instead of guess**.  

The numbers I’ve shared are just an example. The whole point is to run the script yourself and see what works best on *your* system. That’s the only benchmark that really matters.  
