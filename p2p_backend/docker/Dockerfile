FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    postgresql-client \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Aplicar migraciones (agrega columnas que los scripts SQL de arranque no tienen,
# p.ej. kyc_status/terms_*/accepted_at/confirmed_at), sembrar datos y arrancar gunicorn.
CMD flask db upgrade; python migrations/sql/00_seed.seed; gunicorn --bind 0.0.0.0:5000 --workers 2 --timeout 60 --max-requests 500 --max-requests-jitter 100 wsgi:app
