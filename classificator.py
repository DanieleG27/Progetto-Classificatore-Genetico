import cv2
import numpy as np
import os
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
import re





# ======================================================
# Funzione per caricare il programma GP dal file
# ======================================================
def load_gp_formula(file_path="best_program.txt"):
    with open(file_path, "r") as f:
        text = f.read().strip()

    # Estrazione formula (prima riga)
    formula = text.splitlines()[0].strip()

    # Rimuovi eventuali spazi doppi
    formula = re.sub(r"\s+", " ", formula)

    # Sostituisci le virgole decimali con punti
    formula = formula.replace(",", ".")
    
    return formula

# ====================================================================================
# Funzione per caricare il programma GP dal file, caluta la formula calcolata dal GP
# ====================================================================================

def classify(X1, X2, X3, X4, gp_formula=None):
    if gp_formula is None:
        raise ValueError("Nessuna formula GP caricata!")

    try:
        # Valuta la formula GP come espressione Python
        result = eval(gp_formula, {"X1": X1, "X2": X2, "X3": X3, "X4": X4})
    except Exception as e:
        print("Errore durante l'evaluazione GP:", e)
        result = 0.0 #la sigmoide vale 0.5

    # Sigmoide per ottenere probabilità
    with np.errstate(over='ignore'):
        output = 1 / (1 + np.exp(-result))

    return output


# ================================================================================================================
# Funzione per classificare tutte le immagini del test set (estrae features -> normalizza -> classifica)
# ================================================================================================================
def classify_images(directory, gp_formula):
    data = []
    filenames = []

    # Estrazione feature
    for filename in os.listdir(directory):
        
        img_path = os.path.join(directory, filename)
        img = cv2.imread(img_path, cv2.IMREAD_GRAYSCALE)
        
        if img is None:
            continue
        _, thresh = cv2.threshold(img, 127, 255, cv2.THRESH_BINARY_INV)
        contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        if len(contours) == 0:
            continue

        cnt = contours[0]
        area = cv2.contourArea(cnt)
        perimeter = cv2.arcLength(cnt, True)
        x, y, w, h = cv2.boundingRect(cnt)
        aspect_ratio = float(w) / h
        extent = float(area) / (w * h)

        data.append([area, perimeter, aspect_ratio, extent])
        filenames.append(filename)

    # Normalizzazione e creazione DataFrame
    df = pd.DataFrame(data, columns=["area", "perimeter", "aspect_ratio", "extent"])
    scaler = MinMaxScaler()
    df_scaled = df.copy()
    df_scaled[["area", "perimeter", "aspect_ratio", "extent"]] = scaler.fit_transform(
        df_scaled[["area", "perimeter", "aspect_ratio", "extent"]])

    # Classificazione
    results = []
    for idx, row in df_scaled.iterrows():
        X1, X2, X3, X4 = row
        epsilon = 1e-6
        X1, X2, X3, X4 = max(X1, epsilon), max(X2, epsilon), max(X3, epsilon), max(X4, epsilon)

        try:
            prob = classify(X1, X2, X3, X4, gp_formula)
            if np.isnan(prob) or np.isinf(prob):
                prob = 0.5
        except Exception as e:
            print(f"Errore con {filenames[idx]}:", e)
            prob = 0.5

        label = "quadrato" if prob >= 0.5 else "cerchio"
        #print(f"{filenames[idx]}, Label={label}")
        results.append([
        filenames[idx],
        round(X1, 2),
        round(X2, 2),
        round(X3, 2),
        round(X4, 2),
        round(prob, 2),
        label
    ])
    

    df_results = pd.DataFrame(results, columns=["filename", "area", "perimeter", "aspect_ratio", "extent", "prob", "label_predicted"])
    return df_results



# ======================================================
# Esempio d'uso completo
# ======================================================
if __name__ == "__main__":
    gp_formula = load_gp_formula("best_program.txt")
    print("="*100)
    print("Formula GP caricata:")
    print(gp_formula)
    print("="*100)

    test_dir = "Testing"
    df_results = classify_images(test_dir, gp_formula)

    df_results.to_csv("classification_results.csv", index=False)
    print("\nRisultati salvati in classification_results.csv\n")

    
    # Calcolo dell'accuratezza
    # Aggiunge la colonna con la label reale basata sul nome file
    df_results["true_label"] = df_results["filename"].apply(
        lambda x: "quadrato" if "square" in x else "cerchio"
    )

    # Calcolo dell'accuracy
    correct = np.sum(df_results["true_label"] == df_results["label_predicted"])
    total = len(df_results)
    accuracy = correct / total * 100

    print("="*50)
    print(f"Accuratezza complessiva: {accuracy:.2f}% ({correct}/{total})")
    print("="*50)

    # Salvataggio del file con le label reali
    df_results.to_csv("classification_results_with_truth.csv", index=False)


