FROM --platform=linux/amd64 eclipse-temurin:17-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /home/spring/app.jar

#add external jar for EDC service
ADD edc-ws edc-ws

COPY docker/start.sh start.sh

ENTRYPOINT ["sh","/start.sh"]