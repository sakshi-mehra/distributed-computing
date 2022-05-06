FROM openjdk:11
WORKDIR /var/lib/sqlite
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ./app.jar
ENTRYPOINT ["java","-jar","./app.jar"]
EXPOSE 5555
EXPOSE 5556