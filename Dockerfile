FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:resolve

COPY src ./src
RUN mvn package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /app/NaviMusic.jar

ENTRYPOINT ["java", "-Xmx512m", "-jar", "/app/NaviMusic.jar", "nogui"]