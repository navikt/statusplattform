FROM maven:3.9-amazoncorretto-21 as maven

COPY pom.xml pom.xml
COPY . .

RUN mvn clean dependency:go-offline -B
RUN mvn package


FROM openjdk:21-jdk-slim

WORKDIR /adevguide
EXPOSE 3005

COPY rapporter/ rapporter/
COPY --from=maven server/target/server-*jar ./statusplattform-server.jar

CMD ["java", "-jar", "./statusplattform-server.jar"]
