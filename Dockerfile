FROM eclipse-temurin:17-jre

ARG JAR_FILE

ENV TZ=Asia/Shanghai

WORKDIR /app

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
