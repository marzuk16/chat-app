FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG JAR_FILE
COPY target/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
