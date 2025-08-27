FROM gcr.io/distroless/java21-debian12:nonroot

ENV TZ="Europe/Oslo"
WORKDIR /app

COPY target/*.jar app.jar

CMD ["/app/app.jar"]
EXPOSE 8080