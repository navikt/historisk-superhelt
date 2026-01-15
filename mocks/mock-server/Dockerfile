FROM gcr.io/distroless/java21-debian12:debug

ENV TZ="Europe/Oslo"
WORKDIR /app

COPY target/*.jar app.jar

CMD ["/app/app.jar"]
EXPOSE 9080