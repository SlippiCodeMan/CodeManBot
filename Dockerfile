FROM openjdk:15-alpine
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
CMD ["java", "-jar", "app.jar"]