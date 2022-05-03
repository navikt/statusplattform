FROM maven:3.6.3-openjdk-17 as maven

COPY pom.xml pom.xml

COPY . .

RUN mvn clean install

RUN mvn dependency:go-offline -B

RUN mvn package

FROM openjdk:17

#RUN dir #Added

WORKDIR /adevguide

EXPOSE 3005


#RUN dir #Added


COPY --from=maven portal-server/target/portal-server-0.1-SNAPSHOT.jar ./portal-server.jar

CMD ["java", "-jar", "./portal-server.jar"]