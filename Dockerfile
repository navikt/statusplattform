FROM library/maven:3-amazoncorretto-21 AS maven

COPY pom.xml pom.xml
COPY . .

RUN mvn clean dependency:go-offline package -B


FROM eclipse-temurin:21-jre

WORKDIR /adevguide
EXPOSE 3005

COPY rapporter/ rapporter/
COPY --from=maven server/target/server-*jar ./statusplattform-server.jar

CMD ["java", "-jar", "./statusplattform-server.jar"]
