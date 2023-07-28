FROM --platform=linux/amd64 eclipse-temurin:11-jdk
RUN adduser --system --group spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /home/spring/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-Dspring.pidfile=/home/spring/application.pid","-jar","/home/spring/app.jar"]
