FROM openjdk:17
EXPOSE 8080
ADD target/spring-boot-docker.jar spring-boot-docker.jar
LABEL authors="raczk"

ENTRYPOINT ["java", "-jar","/spring-boot-docker.jar"]
