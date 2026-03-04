import cv2
import numpy as np
import os
import pandas as pd
from sklearn.preprocessing import MinMaxScaler

# Cartelle generate precedentemente
directory = "."
dataset_dir = directory + "/Dataset"

classes = ["circles", "squares"]

data = []

# ======================================
# Estrazione delle feature
# ======================================
for label, cls in enumerate(classes):
    folder = os.path.join(dataset_dir, cls)

    for filename in os.listdir(folder):
        img_path = os.path.join(folder, filename)
        img = cv2.imread(img_path, cv2.IMREAD_GRAYSCALE)

        if img is None:
            continue

        _, thresh = cv2.threshold(img, 127, 255, cv2.THRESH_BINARY_INV)

        # Trova contorni
        contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        if len(contours) == 0:
            continue

        cnt = contours[0]

        # Feature
        area = cv2.contourArea(cnt)
        perimeter = cv2.arcLength(cnt, True)
        x, y, w, h = cv2.boundingRect(cnt)

        aspect_ratio = float(w) / h
        extent = float(area) / (w * h)

        data.append([area, perimeter, aspect_ratio, extent, label])

# =============================
# Salvataggio CSV dataset
# =============================
df = pd.DataFrame(data, columns=["area", "perimeter", "aspect_ratio", "extent", "label"])

#Opzionale
# df.to_csv("features_dataset.csv", index=False)
# print("Features salvate in features_dataset.csv")

# =============================
# Normalizzazione Min-Max
# =============================
scaler = MinMaxScaler()
df_scaled = df.copy()
df_scaled[["area", "perimeter", "aspect_ratio", "extent"]] = scaler.fit_transform(
    df_scaled[["area", "perimeter", "aspect_ratio", "extent"]]
)

# =============================
# Creazione del file problem.dat
# =============================
with open(os.path.join(directory, "problem.dat"), "w") as f:
    
    n_vars = 4
    randomnumber = 10  
    minrandom = -2
    maxrandom = 2
    n_cases = len(df_scaled)

    # Prima riga (nuovo formato TinyGP aggiornato)
    f.write(f"{n_vars} {randomnumber} {minrandom} {maxrandom} {n_cases}\n")

    # Aggiunta dei casi (feature + label)
    for _, row in df_scaled.iterrows():
        line = " ".join(f"{v:.2f}" for v in row.values)
        f.write(line + "\n")

print("File problem.dat creato con successo!")
