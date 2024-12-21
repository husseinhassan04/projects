
# Multimodal AI Interface

This project provides a multimodal AI interface integrating real-time object detection, speech recognition, and automatic translation of recognized speech into a selectable target language. It offers a web-based interface with dark mode support and a customizable user experience.

## Features

1. **Live Object Detection**:  
   - Uses a webcam feed to detect objects in real-time.  
   - Allows selection of specific object categories to filter detections (e.g., person, car, bottle, cell phone).  
   - Displays bounding boxes and confidence scores.

2. **Speech Recognition**:  
   - Captures voice commands through a microphone.  
   - Uses [Google Speech Recognition](https://pypi.org/project/SpeechRecognition/) (via the `speech_recognition` Python package) for transcription.

3. **Translation**:  
   - Integrates a T5 model (from the `transformers` library) to translate recognized English speech into a user-selected target language (French, Spanish, German, etc.).

4. **Web Interface**:  
   - Responsive front-end built with HTML, CSS, and JavaScript.  
   - Dark mode support toggleable by the user.  
   - Dropdown menus for selecting objects to detect and target translation language.  
   - Real-time display of recognized speech and translated text.  
   - Live video streaming from the Flask server.

## Project Structure

- **`main.py`**:  
  The Flask application that:  
  - Serves `index.html` at `/`.  
  - Streams webcam frames at `/video_feed`.  
  - Exposes a `/detect` endpoint for object detection.  
  - Exposes a `/speech` endpoint for speech recognition and translation.  
  - Loads the T5 model for translation.  
  - Manages resources like the webcam.

- **`templates/index.html`**:  
  The front-end page that:  
  - Displays the live video feed.  
  - Provides controls for selecting objects to detect and target translation languages.  
  - Initiates speech recognition and displays recognized speech along with its translation.  
  - Offers a dark mode toggle.

- **`app/detection.py`**:  
  A module that contains functions for performing object detection on frames captured from the webcam.

- **`requirements.txt` (optional)**:  
  Contains Python dependencies and their versions if you want to freeze the environment.

## Dependencies

- **Flask**: For serving the web interface.
- **OpenCV (cv2)**: For accessing the webcam and processing video frames.
- **SpeechRecognition**: For capturing and recognizing speech.
- **PyAudio**: Required by `speech_recognition` for microphone input.
- **transformers & torch**: For loading and using the T5 model for translation.
- **numpy**: For numerical operations related to image processing and object detection.

*Note:* Additional object detection models or configuration files may be required depending on your chosen detection method.

## Installation

1. **Set up the environment**:
   ```bash
   python3 -m venv venv
   source venv/bin/activate   # On Windows: venv\Scripts\activate
   ```

2. **Install the required packages**:
   ```bash
   pip install flask opencv-python-headless speechrecognition pyaudio transformers torch numpy
   ```

   **Notes**:
   - `pyaudio` may require additional steps depending on your OS.
   - For GPU acceleration with PyTorch, consider installing a CUDA-enabled version of `torch`.

3. **Download model weights**:  
   The code uses `"google-t5/t5-base"` from Hugging Face. The first run will automatically download the model weights if you have an internet connection.

4. **Set up microphone and webcam**:
   - Ensure your webcam is connected and accessible.
   - Ensure a working microphone is available for speech recognition.

## Running the Application

1. **Start the Flask app**:
   ```bash
   python main.py
   ```

2. **Open the web interface**:  
   By default, Flask runs on `http://127.0.0.1:5000`.  
   Visit `http://127.0.0.1:5000` in your web browser.

## Usage

- **Object Detection**:  
  When you visit the page, you will see a live webcam feed.  
  Select the object type you want to detect from the dropdown and click "Update Detection" to filter.

- **Speech Recognition & Translation**:  
  Select a target language from the dropdown menu.  
  Click the microphone button to start speech recognition.  
  Speak a command, and after processing, the original command and its translation will be displayed on the page.

- **Dark Mode**:  
  Toggle the switch at the top of the interface to switch between light and dark modes.

## Model Customization

The provided T5 model `"google-t5/t5-base"` is a general-purpose T5 model. For improved translation results, consider using a model specifically fine-tuned for translation, such as `"Helsinki-NLP/opus-mt-en-fr"` for English-to-French translation. Update the prompt and code in `main.py` accordingly.

## Speech Recognition Limitations

The quality of speech recognition depends on factors like microphone quality, ambient noise, and pronunciation. Consider using `recognizer.adjust_for_ambient_noise()` to improve performance in noisy environments.

## Error Handling

This code includes basic exception handling. For production environments, more robust error handling, logging, and security measures should be implemented.
