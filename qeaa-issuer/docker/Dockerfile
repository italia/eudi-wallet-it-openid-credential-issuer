FROM --platform=linux/amd64 eclipse-temurin:17-jdk
 
RUN addgroup --gid 1000 java
RUN adduser -G java -u 1000 -S java -h /app java
RUN echo "java:x:1000:1000::/app:/bin/sh" >> /etc/passwd
USER java:java

WORKDIR /app
 
COPY --chown=1000:1000 target/*.jar app.jar

ADD --chown=1000:1000 edc-ws edc-ws
COPY --chown=1000:1000 docker/start.sh start.sh

ENV TZ=Europe/Rome
 
USER java
 
CMD ["sh", "./start.sh"]
