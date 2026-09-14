#!/usr/bin/env bash
set -e

if [ ! -f .env ]; then
    echo "No se encontró el archivo .env. Copiá .env.example a .env y completá las credenciales:"
    echo "  cp .env.example .env"
    exit 1
fi

export $(grep -v '^#' .env | xargs)
mvn process-classes exec:java
