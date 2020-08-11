FROM openjdk:12-jdk-alpine
VOLUME /tmp
COPY build/libs/* app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app.jar"]