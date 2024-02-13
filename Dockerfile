FROM openjdk:21

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} matchservice.jar

ENTRYPOINT ["java", "-jar", "/matchservice.jar"]

EXPOSE 8083