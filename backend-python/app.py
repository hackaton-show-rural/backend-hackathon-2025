import os
from flask import Flask, request, jsonify
from minio import Minio
import pdfplumber
import docx
import magic
import io
import requests

app = Flask(__name__)

minio_client = Minio(
    os.environ.get("MINIO_URL").replace("http://", ""),
    access_key=os.environ.get("MINIO_ACCESS_KEY"),
    secret_key=os.environ.get("MINIO_SECRET_KEY"),
    secure=False
)

OLLAMA_URL = "http://host.docker.internal:11434/api/generate"

def extract_text_from_pdf(file_bytes):
    with pdfplumber.open(io.BytesIO(file_bytes)) as pdf:
        text = ""
        for page in pdf.pages:
            text += page.extract_text() or ""
    return text

def extract_text_from_docx(file_bytes):
    doc = docx.Document(io.BytesIO(file_bytes))
    text = ""
    for paragraph in doc.paragraphs:
        text += paragraph.text + "\n"
    return text

def process_document(file_bytes):
    file_type = magic.from_buffer(file_bytes, mime=True)
    
    if file_type == "application/pdf":
        text = extract_text_from_pdf(file_bytes)
    elif file_type == "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
        text = extract_text_from_docx(file_bytes)
    else:
        raise ValueError(f"Tipo de arquivo não suportado: {file_type}")

    prompt = f"""Extraia as seguintes informações do documento:
            1. Tipo de documento (licença ambiental, autorização, outorga, etc)
            2. CNPJ/CPF relacionado
            3. Data de emissão
            4. Data de vencimento
            5. Órgão emissor
            6. Número do documento

            Documento:
            {text}

            Responda no formato JSON.
            """

    response = requests.post(OLLAMA_URL, json={
        "model": "llama2",
        "prompt": prompt,
        "stream": False,
        "options": {
            "temperature": 0.1,
            "num_predict": 1000
        }
    })
    
    if response.status_code == 200:
        return response.json()["response"]
    else:
        raise Exception(f"Erro ao processar com Ollama: {response.text}")

@app.route("/process", methods=["POST"])
def process_file():
    try:
        bucket_name = request.json.get("documents")
        file_name = request.json.get("file_name")
        
        data = minio_client.get_object(bucket_name, file_name)
        file_bytes = data.read()
        
        result = process_document(file_bytes)
        
        return jsonify({"success": True, "result": result})
    
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)