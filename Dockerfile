FROM maven:3.6.3-openjdk-11 as maven

# Create a base layer with linkerd-await from a recent release.
FROM docker.io/curlimages/curl:latest as linkerd
ARG LINKERD_AWAIT_VERSION=v0.2.4
RUN curl -sSLo /tmp/linkerd-await https://github.com/linkerd/linkerd-await/releases/download/release%2F${LINKERD_AWAIT_VERSION}/linkerd-await-${LINKERD_AWAIT_VERSION}-amd64 && \
    chmod 755 /tmp/linkerd-await

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
# Package the application wrapped by linkerd-await. Note that the binary is
# static so it can be used in `scratch` images:
FROM scratch
COPY --from=linkerd /tmp/linkerd-await /linkerd-await


COPY --from=maven portal-server/target/portal-server-0.1-SNAPSHOT.jar ./portal-server.jar

# In this case, we configure the proxy to be shutdown after `myapp` completes
# running. This is only really needed for jobs where the application is
# expected to complete on its own (namely, `Jobs` and `Cronjobs`)
ENTRYPOINT ["/linkerd-await", "--shutdown", "--"]

CMD ["java", "-jar", "./portal-server.jar"]