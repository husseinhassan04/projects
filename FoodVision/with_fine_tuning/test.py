import sys
import tensorflow as tf
from tensorflow.keras.models import load_model
from PyQt5.QtWidgets import (
    QApplication, QWidget, QLabel, QPushButton, QFileDialog, QVBoxLayout, QSpacerItem, QSizePolicy
)
from PyQt5.QtGui import QPixmap, QFont
from PyQt5.QtCore import Qt

# Class names
class_names = [
    'pizza', 'samosa', 'falafel', 'donuts', 'macaroni_and_cheese',
    'caprese_salad', 'bibimbap', 'ceviche', 'bruschetta', 'beef_carpaccio',
    'gnocchi', 'club_sandwich', 'grilled_cheese_sandwich', 'pad_thai', 'takoyaki'
]

# Load model once
model = load_model("C:\\Users\\Lenovo\\Desktop\\CCE\\Ai\\FoodVision\\with_fine_tuning\\tuned_food_vision_model.h5")

def predict_image(img_path):
    img = tf.io.read_file(img_path)
    img = tf.image.decode_image(img, channels=3)
    img = tf.image.resize(img, [224, 224])
    img = img / 255.0
    img = tf.expand_dims(img, axis=0)
    predictions = model.predict(img)
    predicted_index = tf.argmax(predictions[0]).numpy()
    predicted_class = class_names[predicted_index]
    confidence = predictions[0][predicted_index]
    return predicted_class, confidence

class FoodClassifierApp(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("🍽️ Food Vision Classifier")
        self.setFixedSize(420, 550)
        self.setStyleSheet("background-color: #f9f9f9;")

        layout = QVBoxLayout()
        layout.setContentsMargins(20, 20, 20, 20)
        layout.setSpacing(20)

        # Title label
        title_label = QLabel("🍕 Food Vision Classifier")
        title_label.setFont(QFont("Arial", 18, QFont.Bold))
        title_label.setAlignment(Qt.AlignCenter)
        layout.addWidget(title_label)

        # Image display
        self.image_label = QLabel("No image selected")
        self.image_label.setAlignment(Qt.AlignCenter)
        self.image_label.setFixedSize(360, 300)
        self.image_label.setStyleSheet("border: 1px dashed #aaa; background-color: #fff;")
        layout.addWidget(self.image_label)

        # Result label
        self.result_label = QLabel("")
        self.result_label.setAlignment(Qt.AlignCenter)
        self.result_label.setStyleSheet("font-size: 16px; color: #333;")
        layout.addWidget(self.result_label)

        # Spacer
        layout.addSpacerItem(QSpacerItem(20, 20, QSizePolicy.Minimum, QSizePolicy.Expanding))

        # Choose image button
        self.button = QPushButton("📷 Choose Image")
        self.button.setFixedHeight(40)
        self.button.setStyleSheet("""
            QPushButton {
                background-color: #4CAF50;
                color: white;
                border: none;
                border-radius: 8px;
                font-size: 16px;
            }
            QPushButton:hover {
                background-color: #45a049;
            }
        """)
        self.button.clicked.connect(self.open_image)
        layout.addWidget(self.button)

        self.setLayout(layout)

    def open_image(self):
        file_path, _ = QFileDialog.getOpenFileName(self, "Select Image", "", "Image Files (*.png *.jpg *.jpeg)")
        if file_path:
            # Show image
            pixmap = QPixmap(file_path).scaled(
                self.image_label.width(), self.image_label.height(), Qt.KeepAspectRatio, Qt.SmoothTransformation
            )
            self.image_label.setPixmap(pixmap)

            # Predict
            try:
                predicted_class, confidence = predict_image(file_path)
                self.result_label.setText(f"{predicted_class.replace('_', ' ').title()} ({confidence:.2%})")
            except Exception:
                self.result_label.setText("⚠️ Error during prediction")

# Run the app
if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = FoodClassifierApp()
    window.show()
    sys.exit(app.exec_())
