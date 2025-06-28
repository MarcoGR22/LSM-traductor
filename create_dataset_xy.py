
import os
import cv2
import pickle
import mediapipe as mp

DATASET_PATH = "Dataset"
if not os.path.exists(DATASET_PATH):
    os.makedirs(DATASET_PATH)

mp_hands = mp.solutions.hands
hands = mp_hands.Hands(static_image_mode=True, max_num_hands=1)

data = {"data": [], "labels": []}
labels = sorted(os.listdir(DATASET_PATH))

for label in labels:
    folder_path = os.path.join(DATASET_PATH, label)
    if not os.path.isdir(folder_path):
        continue
    for file in os.listdir(folder_path):
        if file.lower().endswith(('.jpg', '.jpeg', '.png')):
            img_path = os.path.join(folder_path, file)
            image = cv2.imread(img_path)
            image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            result = hands.process(image_rgb)
            if result.multi_hand_landmarks:
                lm = result.multi_hand_landmarks[0].landmark
                base = lm[0]
                coords = [(p.x - base.x, p.y - base.y) for p in lm]
                flat = [coord for pair in coords for coord in pair]
                if len(flat) == 42:
                    data["data"].append(flat)
                    data["labels"].append(label)

with open("data_xy.pickle", "wb") as f:
    pickle.dump(data, f)

print("✅ Dataset creado con coordenadas x, y (42 valores)")
