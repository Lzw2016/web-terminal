FROM java:8u111-jre

ADD target/web-terminal-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "-server.port=8080"]
EXPOSE 1314