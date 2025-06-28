import pickle
import numpy as np
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from keras.models import Sequential
from keras.layers import Dense

# Load the dataset
with open("data_xy.pickle", "rb") as f:
    dataset = pickle.load(f)

X = np.array(dataset["data"])
y = np.array(dataset["labels"])

# Preprocess the data
scaler = StandardScaler()
X = scaler.fit_transform(X)

label_encoder = LabelEncoder()
y_encoded = label_encoder.fit_transform(y)

# Split the data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y_encoded, test_size=0.2, shuffle=True, stratify=y_encoded)

# Define the Keras model (Multilayer Perceptron)
model = Sequential()

# Input layer (X.shape[1] is the number of features)
model.add(Dense(128, input_dim=X.shape[1], activation='relu'))  # First hidden layer
model.add(Dense(64, activation='relu'))  # Second hidden layer
model.add(Dense(len(label_encoder.classes_), activation='softmax'))  # Output layer with softmax (for multi-class classification)

# Compile the model
model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# Train the model
model.fit(X_train, y_train, validation_data=(X_test, y_test), epochs=60, batch_size=64)

# Save the trained model
model.save("modelo_xy.keras")

# Convert the model to TFLite format
interpreter = tf.lite.TFLiteConverter.from_keras_model(model).convert()
with open("modelo_xy.tflite", "wb") as f:
    f.write(interpreter)

# Save the labels
with open("etiquetas_xy.txt", "w") as f:
    for lbl in label_encoder.classes_:
        f.write(lbl + "\n")

print("✅ Modelo .tflite y etiquetas exportadas")
