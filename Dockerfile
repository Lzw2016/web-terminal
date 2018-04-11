FROM java:8u111-jre-alpine

ADD target/web-terminal-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "-server.port=9066"]
EXPOSE 9066