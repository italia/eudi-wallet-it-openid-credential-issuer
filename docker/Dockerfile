FROM --platform=linux/amd64 eclipse-temurin:17-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ~/app.jar

ENTRYPOINT ["java", "-Dspring.config.location=${CONF_FILE}", "-Dspring.pidfile=/home/spring/pid/application.pid", "-jar", "/home/spring/app.jar"]
