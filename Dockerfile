FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:resolve

COPY src ./src
RUN mvn compile

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /app/NaviMusic.jar

ENV DISCORD_TOKEN=""
ENV SPOTIFY_CLIENT_ID=""
ENV SPOTIFY_SECRET=""

ENTRYPOINT ["java", "-Xmx512m", "-jar", "/app/NaviMusic.jar", "nogui"]