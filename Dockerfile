FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache curl tar

RUN curl -sSL https://github.com/jwilder/dockerize/releases/download/v0.6.1/dockerize-linux-amd64-v0.6.1.tar.gz -o /tmp/dockerize.tar.gz \
    && tar -xzvf /tmp/dockerize.tar.gz -C /usr/local/bin \
    && rm /tmp/dockerize.tar.gz

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080


ENTRYPOINT ["sh", "-c", "dockerize -wait tcp://db:5432 -timeout 30s java -jar app.jar"]
