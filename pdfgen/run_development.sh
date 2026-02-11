#!/bin/bash

CURRENT_PATH="$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"

CONTAINER_NAME="ghcr.io/navikt/pdfgen:2.0.98"

docker pull $CONTAINER_NAME
docker run \
        -v $CURRENT_PATH/templates:/app/templates \
        -v $CURRENT_PATH/fonts:/app/fonts \
        -v $CURRENT_PATH/data:/app/data \
        -v $CURRENT_PATH/resources:/app/resources \
        -p 8086:8080 \
        -e DISABLE_PDF_GET=false \
        -e ENABLE_HTML_ENDPOINT=true \
        -e JDK_JAVA_OPTIONS \
        -it \
        --rm \
        $CONTAINER_NAME
