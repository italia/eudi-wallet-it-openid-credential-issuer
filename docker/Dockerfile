FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ~/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=docker", "-Dspring.config.location=${CONF_FILE}", "-Dlogging.file.name=/home/spring/log/app.log", "-Dspring.pidfile=/home/spring/pid/application.pid","-jar","/home/spring/app.jar"]
