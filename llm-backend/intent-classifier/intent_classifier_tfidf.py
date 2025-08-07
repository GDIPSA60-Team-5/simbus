import json
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import cross_val_score
import joblib

# ----- Load training data from JSON -----
with open("intent_training_data.json", "r", encoding="utf-8") as f:
    raw_data = json.load(f)

df = pd.DataFrame(raw_data)
assert "text" in df.columns and "intent" in df.columns, "Missing 'text' or 'intent' in training data."

# ----- TF-IDF vectorization -----
vectorizer = TfidfVectorizer(ngram_range=(1, 2), max_features=5000)
X_tfidf = vectorizer.fit_transform(df["text"])
y = df["intent"]

# ----- Train classifier with cross-validation -----
clf = LogisticRegression(max_iter=300)
scores = cross_val_score(clf, X_tfidf, y, cv=5)
print(f"Cross-validation scores: {scores}")
print(f"Mean accuracy: {np.mean(scores):.2f}")

# ----- Fit on full data -----
clf.fit(X_tfidf, y)

# ----- Save model and vectorizer -----
joblib.dump((vectorizer, clf), "intent_classifier_tfidf.joblib")
