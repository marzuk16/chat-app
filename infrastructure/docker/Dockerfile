# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --chown=appuser:appgroup ${JAR_FILE} app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
