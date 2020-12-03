FROM openjdk:15-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
CMD ["java", "-jar", "app.jar"]