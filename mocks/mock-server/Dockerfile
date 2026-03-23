FROM gcr.io/distroless/java25-debian13:debug

ENV TZ="Europe/Oslo"
WORKDIR /app

COPY target/*.jar app.jar

CMD ["/app/app.jar"]
EXPOSE 9080