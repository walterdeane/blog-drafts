Option 1: Use ngrok or Cloudflared to expose Ollama externally

This method creates a secure tunnel to your local Ollama server so Cursor can reach it over the network.

We'll walk through using ngrok here as an example. It creates a secure tunnel from your local machine to the internet, allowing remote access to services running on your machine—like your Ollama LLM API.

When installing ngrok, you'll need to create a free account to get an authentication token. Once that's set up, you'll be able to expose your local Ollama server securely. This is especially useful if you're running the LLM on a desktop and want to connect from a laptop, or even use your LLM while you're away from home.

For many people, this allows a desktop to act as a dedicated local AI server—often with more power and better thermal capacity than a laptop, at a lower cost.

There are tradeoffs: you get flexibility and access, but you are exposing a local port to the outside world (albeit securely). If you don’t need external access, the /etc/hosts approach is simpler and safer. Install and run ngrok:

brew install ngrok

# Authenticate (only required once)
ngrok config add-authtoken <your-token>

# Start tunnel
ngrok http 11434
``` you will get an 
# authentication error for setting up and account and getting a token. 
ngrok http 11434



When installing ngrok, you'll need to create a free account to get an authentication token. Once that's set up, you'll be able to create secure tunnels from your local machine to the internet. This is especially useful if you're running the LLM on a desktop and want to connect from a laptop or another device on your local network. 

For many people, setting this up on a dedicated desktop or home server gives you more power (and often costs less) than trying to do everything from a laptop.

ngrok will give you a public forwarding URL like https://abc123.ngrok.io. In Cursor settings, set your LLM endpoint to that URL.