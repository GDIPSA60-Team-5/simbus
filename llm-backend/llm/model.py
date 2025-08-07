from gpt4all import GPT4All
import joblib
import numpy as np
from llm.state import CONFIDENCE_THRESHOLD
from typing import Optional


# Load model once
# Switch to "cuda" to enable gpu
model = GPT4All("Meta-Llama-3.1-8B-Instruct-128k-Q4_0.gguf", device="cpu")

# Load embedder and classifier as a tuple
embedder, intent_classifier = joblib.load("intent-classifier/intent_classifier_embedding.joblib")

# Load the TF-IDF model
# vectorizer, intent_classifier = joblib.load("intent-classifier/intent_classifier_tfidf.joblib")

def get_model():
    return model


def predict_intent(user_input: str) -> Optional[str]:
    input_vector = None
    input_vector = embedder.encode([user_input])
    # input_vector = vectorizer.transform([user_input])
    probs = intent_classifier.predict_proba(input_vector)[0]
    print(f"Prediction probs: {probs}")
    max_index = np.argmax(probs)
    max_prob = probs[max_index]
    return (
        intent_classifier.classes_[max_index]
        if max_prob >= CONFIDENCE_THRESHOLD
        else None
    )