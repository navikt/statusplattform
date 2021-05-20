FROM maven:3.6.3-openjdk-11 as maven

COPY pom.xml pom.xml

COPY . .

RUN mvn clean install

RUN mvn dependency:go-offline -B

RUN mvn package

FROM openjdk:11

#RUN dir #Added

WORKDIR /adevguide

EXPOSE 3001


#RUN dir #Added


COPY --from=maven portal-server/target/portal-server-0.1-SNAPSHOT.jar ./portal-server.jar

CMD ["java", "-jar", "./portal-server.jar"]