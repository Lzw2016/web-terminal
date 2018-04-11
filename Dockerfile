FROM java:8u111-jre-alpine

ADD target/web-terminal-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "-server.port=9066"]
EXPOSE 9066