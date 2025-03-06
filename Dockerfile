FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml /app/pom.xml

RUN --mount=type=bind,source=pom.xml,target=/app/pom.xml,readonly \
    --mount=type=cache,target=/root/.m2 \
    mvn dependency:resolve

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar /app/NaviMusic.jar

ENTRYPOINT ["java", "-jar", "/app/NaviMusic.jar", "nogui"]
