import speech_recognition as sr
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

# Load the T5 model and tokenizer
tokenizer = AutoTokenizer.from_pretrained("google-t5/t5-base")
model = AutoModelForSeq2SeqLM.from_pretrained("google-t5/t5-base")


def translate_text(input_text: str, source_lang: str, target_lang: str) -> str:

    prompt = f"translate {source_lang} to {target_lang}: {input_text}"
    input_ids = tokenizer(prompt, return_tensors="pt").input_ids
    output_ids = model.generate(input_ids, max_length=512, num_beams=4, early_stopping=True)
    translated_text = tokenizer.decode(output_ids[0], skip_special_tokens=True)
    return translated_text


def process_command(source_lang: str, target_lang: str):

    recognizer = sr.Recognizer()
    with sr.Microphone(device_index=0) as source:
        print("Adjusting for ambient noise...")
        recognizer.adjust_for_ambient_noise(source, duration=1)
        print("Listening for commands...")

        try:
            audio = recognizer.listen(source, timeout=5, phrase_time_limit=10)
            command = recognizer.recognize_google(audio).lower()
            print(f"Recognized command: {command}")

            # Translate the recognized command
            translated = translate_text(command, source_lang, target_lang)
            print(f"Translated command ({source_lang} -> {target_lang}): {translated}")

            return command

        except sr.UnknownValueError:
            print("Could not understand the audio. Please try again.")
            return "Unknown command"

        except sr.WaitTimeoutError:
            print("Listening timeout. No command detected.")
            return "Timeout"

        except Exception as e:
            print(f"An error occurred: {e}")
            return "Error"