FROM maven:3.9.8-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml /app/pom.xml

RUN mvn dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=build /app/target/*.jar /app/NaviMusic.jar

RUN chown -R appuser:appgroup /app
USER appuser

ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/NaviMusic.jar nogui $@", "--"]