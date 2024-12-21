import time
import logging
import atexit
import pyttsx3
from flask import Flask, render_template, Response, jsonify, request
import cv2
import speech_recognition as sr
from app.detection import detect_objects
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

app = Flask(__name__)
engine = pyttsx3.init()

is_listening = False

def speak_text(text):
    engine.say(text)
    engine.runAndWait()

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

camera = cv2.VideoCapture(0)
if not camera.isOpened():
    logging.error("Error: Could not open the webcam.")
    raise RuntimeError("Error: Could not open the webcam.")

recognizer = sr.Recognizer()

tokenizer = AutoTokenizer.from_pretrained("google-t5/t5-base")
model = AutoModelForSeq2SeqLM.from_pretrained("google-t5/t5-base")

def translate_text(input_text: str, source_lang: str, target_lang: str) -> str:
    prompt = f"translate {source_lang} to {target_lang}: {input_text}"
    input_ids = tokenizer(prompt, return_tensors="pt").input_ids
    output_ids = model.generate(input_ids, max_length=512, num_beams=4, early_stopping=True)
    translated_text = tokenizer.decode(output_ids[0], skip_special_tokens=True)
    return translated_text

def cleanup():
    if camera.isOpened():
        logging.info("Releasing camera resource.")
        camera.release()

atexit.register(cleanup)

def generate_frames():
    retry_attempts = 3
    while True:
        success, frame = False, None
        for _ in range(retry_attempts):
            success, frame = camera.read()
            if success:
                break
        if not success:
            logging.warning("Failed to grab frame from the webcam after retries.")
            break

        annotated_frame, _ = detect_objects(frame)

        ret, buffer = cv2.imencode(".jpg", annotated_frame)
        if not ret:
            logging.error("Failed to encode frame.")
            break

        frame = buffer.tobytes()
        yield (b"--frame\r\nContent-Type: image/jpeg\r\n\r\n" + frame + b"\r\n")
        time.sleep(0.1)

@app.route("/")
def index():
    return render_template("index.html")

@app.route("/video_feed")
def video_feed():
    return Response(generate_frames(), mimetype="multipart/x-mixed-replace; boundary=frame")

@app.route("/detect", methods=["POST"])
def detect():
    try:
        data = request.get_json()
        object_class = data.get("object", "all")

        success, frame = camera.read()
        if not success:
            logging.error("Failed to read frame from the webcam.")
            return jsonify({"error": "Failed to read frame"}), 500

        annotated_frame, detections = detect_objects(frame,
                                                     class_names=[object_class] if object_class != "all" else None)

        return jsonify({
            "detections": detections
        })

    except Exception as e:
        logging.exception("Error in detection endpoint")
        return jsonify({"error": "Detection failed"}), 500

@app.route("/speech", methods=["GET"])
def speech():
    global is_listening
    SOURCE_LANGUAGE = "English"
    target_lang = request.args.get("target_lang", "French")

    try:
        with sr.Microphone() as source:
            logging.info("Listening for speech...")
            is_listening = True
            recognizer.adjust_for_ambient_noise(source, duration=1)
            audio = recognizer.listen(source, timeout=5, phrase_time_limit=10)
            command = recognizer.recognize_google(audio).lower()
            logging.info(f"Recognized command: {command}")

            translated_command = translate_text(command, SOURCE_LANGUAGE, target_lang)
            logging.info(f"Translated command ({SOURCE_LANGUAGE} -> {target_lang}): {translated_command}")

            if "compare" in command:
                response = voice_compare(command)
                return jsonify(response)
            else:
                return jsonify({"command": command, "translated_command": translated_command})

    except sr.UnknownValueError:
        logging.warning("Could not understand the audio.")
        return jsonify({"command": "Unknown command"})
    except sr.WaitTimeoutError:
        logging.warning("Listening timeout.")
        return jsonify({"command": "Timeout"})
    except Exception as e:
        logging.exception("Error in speech recognition")
        return jsonify({"command": "Error"})
    finally:
        is_listening = False

@app.route("/voice_compare/<command>", methods=["GET"])
def voice_compare(command):
    if "compare" in command:
        success, frame = camera.read()
        if not success:
            logging.error("Failed to read frame from the webcam.")
            return jsonify({"error": "Failed to read frame"}), 500

        object_class = None
        if "bottle" in command:
            object_class = "bottle"
        elif "person" in command or "people" in command:
            object_class = "person"

        if object_class:
            annotated_frame, detections = detect_objects(frame)

            # Filter detections to only include the desired object class
            filtered = [d for d in detections if d["class_name"] == object_class]

            comparison_result = None
            if object_class == "bottle":
                if len(filtered) == 2:
                    # Get width and height for both bottles
                    w1 = filtered[0]["x2"] - filtered[0]["x1"]
                    h1 = filtered[0]["y2"] - filtered[0]["y1"]
                    area_1 = w1 * h1

                    w2 = filtered[1]["x2"] - filtered[1]["x1"]
                    h2 = filtered[1]["y2"] - filtered[1]["y1"]
                    area_2 = w2 * h2

                    # Compare the areas of the two bottles
                    if area_1 > area_2:
                        comparison_result = f"The bottle on the left appears bigger."
                    elif area_2 > area_1:
                        comparison_result = f"The bottle on the right appears bigger."
                    else:
                        comparison_result = f"Both bottles appear to be the same size."
                else:
                    comparison_result = f"Less than two '{object_class}' objects detected or more than two detected. Please show exactly two."
            elif object_class == "person":
                if len(filtered) == 2:
                    # Get width and height for both persons
                    w1 = filtered[0]["x2"] - filtered[0]["x1"]
                    h1 = filtered[0]["y2"] - filtered[0]["y1"]
                    area_1 = w1 * h1

                    w2 = filtered[1]["x2"] - filtered[1]["x1"]
                    h2 = filtered[1]["y2"] - filtered[1]["y1"]
                    area_2 = w2 * h2

                    # Compare the areas of the two persons
                    if area_1 > area_2:
                        comparison_result = f"The person on the left appears bigger."
                    elif area_2 > area_1:
                        comparison_result = f"The person on the right appears bigger."
                    else:
                        comparison_result = f"Both persons appear to be the same size."
                else:
                    comparison_result = f"Less than two '{object_class}' objects detected or more than two detected. Please show exactly two."

            if comparison_result:
                logging.info(comparison_result)
                speak_text(comparison_result)
                return jsonify({
                    "detections": detections,
                    "comparison": comparison_result
                })
            else:
                logging.warning(f"Unexpected error: No comparison result.")
                speak_text("Error: Unable to perform comparison.")
                return jsonify({"message": "Error: Unable to perform comparison."}), 500
        else:
            speak_text("No bottle or person detected for comparison.")
            return jsonify({"message": "No 'bottle' or 'person' detected for comparison."}), 200
    else:
        speak_text("No 'compare' command detected.")
        return jsonify({"message": "No 'compare' command detected"}), 200

@app.route("/listening-status", methods=["GET"])
def listening_status():
    return jsonify({"is_listening": is_listening})

@app.route("/speech/stop", methods=["POST"])
def stop_speech_recognition():
    global is_listening
    if is_listening:
        is_listening = False
        logging.info("Speech recognition stopped.")
        return jsonify({"message": "Speech recognition stopped."}), 200
    else:
        logging.warning("No active speech recognition session to stop.")
        return jsonify({"message": "No recognition session active."}), 400


if __name__ == "__main__":
    app.run(debug=True)
