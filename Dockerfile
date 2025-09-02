##BUILD
FROM maven:3.9.8-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
# CACHE
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests clean package

## RUNTIME
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# DELAY
RUN apk add --no-cache curl tar \
 && curl -sSL https://github.com/jwilder/dockerize/releases/download/v0.6.1/dockerize-linux-amd64-v0.6.1.tar.gz -o /tmp/dockerize.tar.gz \
 && tar -xzvf /tmp/dockerize.tar.gz -C /usr/local/bin \
 && rm /tmp/dockerize.tar.gz

COPY --from=build /workspace/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["sh","-c","dockerize -wait tcp://db:5432 -wait tcp://redis:6379 -timeout 60s && exec java -XX:MaxRAMPercentage=75 -jar app.jar"]
