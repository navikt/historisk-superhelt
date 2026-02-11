FROM ghcr.io/navikt/pdfgen:2.0.108
COPY templates /app/templates
COPY fonts /app/fonts
COPY resources /app/resources
ENV ENABLE_HTML_ENDPOINT=true
ENV DISABLE_PDF_GET=false