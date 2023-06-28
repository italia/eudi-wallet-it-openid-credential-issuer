FROM eclipse-temurin:11-jdk
RUN adduser --system --group spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /home/spring/app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-Dlogging.file.name=/home/spring/app.log", "-Dspring.pidfile=/home/spring/application.pid","-jar","/home/spring/app.jar"]
