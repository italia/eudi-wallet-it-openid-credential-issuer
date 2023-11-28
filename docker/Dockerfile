FROM openjdk:17-alpine
 
RUN addgroup -g 1000 java && adduser -G java -u 1000 -S java -h /app
 
WORKDIR /app
 
COPY --chown=1000:1000 target/*.jar app.jar
 
ENV TZ=Europe/Rome
 
EXPOSE 8080
 
USER java
 
CMD ["java", "-Dspring.config.location=/appconfig/application.yml", "-Dspring.pidfile=/app/application.pid", "-jar", "/app/app.jar", "-Xms256m", "-Xmx512m"]