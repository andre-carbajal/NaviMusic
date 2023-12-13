FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:resolve

COPY src ./src

RUN mvn compile

COPY target/*.jar ./NaviMusic.jar

ENV DISCORD_TOKEN = ""

ENTRYPOINT ["java", "-Xmx512m", "-jar", "/app/NaviMusic.jar", "nogui"]