FROM --platform=linux/amd64 openjdk:17-alpine
 
RUN addgroup -g 1000 java && adduser -G java -u 1000 -S java -h /app
 
WORKDIR /app
 
COPY --chown=1000:1000 target/*.jar app.jar

ADD --chown=1000:1000 edc-ws edc-ws
COPY --chown=1000:100 docker/start.sh start.sh

ENV TZ=Europe/Rome
 
EXPOSE 8080
 
USER java
 
CMD ["sh", "./start.sh"]
