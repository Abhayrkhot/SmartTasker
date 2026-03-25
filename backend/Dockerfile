# Production-oriented Django image (AWS EC2 / Docker compatible)
FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    DJANGO_SETTINGS_MODULE=backend.settings

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq-dev gcc \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --upgrade pip && pip install -r requirements.txt

COPY . .

EXPOSE 8000

# Optional: container health for orchestrators (compose ignores this unless extended)
HEALTHCHECK --interval=30s --timeout=5s --start-period=50s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8000/api/health/', timeout=5)" || exit 1

# Default CMD (docker-compose overrides with migrate + collectstatic + gunicorn)
CMD ["gunicorn", "backend.wsgi:application", "--bind", "0.0.0.0:8000"]
