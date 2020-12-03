FROM openjdk:15-alpine
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY .env[1] .env
CMD ["java", "-jar", "app.jar"]