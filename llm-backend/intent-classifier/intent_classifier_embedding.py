import json
import pandas as pd
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import cross_val_score
import joblib

# ----- Load training data from JSON -----
with open("intent_training_data.json", "r", encoding="utf-8") as f:
    raw_data = json.load(f)

df = pd.DataFrame(raw_data)

assert "text" in df.columns and "intent" in df.columns, (
    "Missing 'text' or 'intent' in training data."
)

# ----- Load pretrained sentence transformer model -----
embedder = SentenceTransformer("all-MiniLM-L6-v2")  # Small and fast model

# ----- Create embeddings for all text data -----
X_embeddings = embedder.encode(df["text"].tolist(), show_progress_bar=True)

# ----- Train classifier with cross-validation -----
clf = LogisticRegression(max_iter=200)

scores = cross_val_score(clf, X_embeddings, df["intent"], cv=5)
print(f"Cross-validation scores: {scores}")
print(f"Mean accuracy: {np.mean(scores):.2f}")

# ----- Fit classifier on full data -----
clf.fit(X_embeddings, df["intent"])

# ----- Save model and embedder -----
joblib.dump((embedder, clf), "intent_classifier_embedding.joblib")
