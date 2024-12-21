from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

tokenizer = AutoTokenizer.from_pretrained("google-t5/t5-base")
model = AutoModelForSeq2SeqLM.from_pretrained("google-t5/t5-base")


def translate_text(input_text: str, source_lang: str, target_lang: str) -> str:

    prompt = f"translate {source_lang} to {target_lang}: {input_text}"

    input_ids = tokenizer(prompt, return_tensors="pt").input_ids

    output_ids = model.generate(input_ids, max_length=512, num_beams=4, early_stopping=True)

    translated_text = tokenizer.decode(output_ids[0], skip_special_tokens=True)

    return translated_text

# translated = translate_text("Hello, how are you?", "English", "French")
# print(translated)
