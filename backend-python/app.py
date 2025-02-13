import os
import logging
import traceback
from flask import Flask, request, jsonify
from minio import Minio
import pdfplumber
import docx
import magic
import io
import requests

# Configure logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

try:
    minio_client = Minio(
        "localhost:9000",
        "minio",
        "minio123",
        secure=False
    )
    logger.info(f"MinIO client initialized with URL: {os.environ.get('MINIO_URL')}")
except Exception as e:
    logger.error(f"Failed to initialize MinIO client: {str(e)}")
    raise

OLLAMA_HOST = os.environ.get("OLLAMA_HOST", "localhost")
OLLAMA_PORT = os.environ.get("OLLAMA_PORT", "11434")
OLLAMA_URL = f"http://{OLLAMA_HOST}:{OLLAMA_PORT}/api/generate"

logger.info(f"Configured Ollama URL: {OLLAMA_URL}")

def extract_text_from_pdf(file_bytes):
    try:
        with pdfplumber.open(io.BytesIO(file_bytes)) as pdf:
            text = ""
            for page_num, page in enumerate(pdf.pages, 1):
                logger.debug(f"Processing PDF page {page_num}")
                page_text = page.extract_text() or ""
                text += page_text
                logger.debug(f"Extracted {len(page_text)} characters from page {page_num}")
            return text
    except Exception as e:
        logger.error(f"Error extracting text from PDF: {str(e)}")
        logger.error(traceback.format_exc())
        raise

def extract_text_from_docx(file_bytes):
    try:
        doc = docx.Document(io.BytesIO(file_bytes))
        text = ""
        for para_num, paragraph in enumerate(doc.paragraphs, 1):
            logger.debug(f"Processing paragraph {para_num}")
            text += paragraph.text + "\n"
        logger.debug(f"Extracted total of {len(text)} characters from DOCX")
        return text
    except Exception as e:
        logger.error(f"Error extracting text from DOCX: {str(e)}")
        logger.error(traceback.format_exc())
        raise

def process_document(file_bytes):
    try:
        file_type = magic.from_buffer(file_bytes, mime=True)
        logger.info(f"Detected file type: {file_type}")
        
        if file_type == "application/pdf":
            text = extract_text_from_pdf(file_bytes)
        elif file_type == "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            text = extract_text_from_docx(file_bytes)
        else:
            logger.error(f"Unsupported file type: {file_type}")
            raise ValueError(f"Unsupported file type: {file_type}")

        logger.debug(f"Extracted text length: {len(text)}")
        logger.debug(f"First 200 characters of text: {text[:200]}")

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

        logger.info("Sending request to Ollama")
        response = requests.post(OLLAMA_URL, json={
            "model": "llama3",
            "prompt": prompt,
            "stream": False,
            "options": {
                "temperature": 0.1,
                "num_predict": 1000
            }
        })
        
        logger.debug(f"Ollama response status: {response.status_code}")
        logger.debug(f"Ollama response headers: {response.headers}")
        
        if response.status_code == 200:
            result = response.json()["response"]
            logger.info("Successfully processed document with Ollama")
            return result
        else:
            logger.error(f"Ollama error response: {response.text}")
            raise Exception(f"Error processing with Ollama: {response.text}")
            
    except Exception as e:
        logger.error(f"Error in process_document: {str(e)}")
        logger.error(traceback.format_exc())
        raise

@app.route("/process", methods=["POST"])
def process_file():
    try:
        logger.info("Received process request")
        request_data = request.get_json()
        logger.debug(f"Request data: {request_data}")
        
        bucket_name = request_data.get("documents")
        file_name = request_data.get("file_name")
        
        logger.info(f"Processing file {file_name} from bucket {bucket_name}")
        
        try:
            data = minio_client.get_object(bucket_name, file_name)
            file_bytes = data.read()
            logger.info(f"Successfully read file from MinIO, size: {len(file_bytes)} bytes")
        except Exception as e:
            logger.error(f"Error reading from MinIO: {str(e)}")
            logger.error(traceback.format_exc())
            return jsonify({"success": False, "error": f"Failed to read file from storage: {str(e)}"}), 500
        
        try:
            result = process_document(file_bytes)
            logger.info("Successfully processed document")
            return jsonify({"success": True, "result": result})
        except Exception as e:
            logger.error(f"Error processing document: {str(e)}")
            logger.error(traceback.format_exc())
            return jsonify({"success": False, "error": f"Failed to process document: {str(e)}"}), 500
    
    except Exception as e:
        logger.error(f"Unexpected error in process_file endpoint: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({"success": False, "error": str(e)}), 500

if __name__ == "__main__":
    logger.info("Starting Flask application")
    app.run(host="0.0.0.0", port=5000, debug=True)