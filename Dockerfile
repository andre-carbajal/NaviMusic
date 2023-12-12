FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y maven

COPY pom.xml ./

RUN mvn dependency:resolve

COPY src ./src

RUN mvn compile

COPY target/*.jar ./NaviMusic.jar

ENV DISCORD_TOKEN = ""

ENTRYPOINT ["java", "-Xmx512m", "-jar", "/app/NaviMusic.jar", "nogui"]