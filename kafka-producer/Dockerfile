FROM python:3.9-slim

WORKDIR /app

COPY ./kafka-producer/requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY ./kafka-producer/collector.py .

CMD ["python", "collector.py"]