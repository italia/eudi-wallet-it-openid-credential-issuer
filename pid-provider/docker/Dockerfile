FROM --platform=linux/amd64 eclipse-temurin:17-jdk
 
RUN addgroup --gid 1000 java
RUN adduser -G java -u 1000 -S java -h /app java
RUN echo "java:x:1000:1000::/app:/bin/sh" >> /etc/passwd
USER java:java

WORKDIR /app
 
COPY --chown=1000:1000 target/*.jar app.jar
 
ENV TZ=Europe/Rome
 
USER java
 
CMD ["java", "-Dspring.config.location=/appconfig/application.yml", "-Dspring.pidfile=/app/application.pid", "-jar", "/app/app.jar"]