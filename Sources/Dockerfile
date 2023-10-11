FROM maven:3-openjdk-11 AS build
COPY . /home/maven/src
WORKDIR /home/maven/src
RUN mvn clean package

FROM openjdk:11
EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/maven/src/target/*-with-dependencies.jar /app/ktor-docker-sample.jar
ENTRYPOINT ["java", "-jar", "/app/ktor-docker-sample.jar"]
