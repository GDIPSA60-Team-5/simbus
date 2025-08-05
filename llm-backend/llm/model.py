from gpt4all import GPT4All

# Load model once
# Switch to "cuda" to enable gpu
model = GPT4All("Meta-Llama-3.1-8B-Instruct-128k-Q4_0.gguf", device="cpu")

def get_model():
    return model