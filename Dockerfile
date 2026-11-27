FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jdk:openjdk-21-dev AS maven

# /home/build is the image's default workdir and is owned by the nonroot user;
# building elsewhere (e.g. /build) fails because that path is root-owned.
# --chown makes the copied sources writable by the nonroot user so Maven can
# create target/ directories during the build.
WORKDIR /home/build

COPY --chown=java:java . .

RUN ./mvnw clean package -B -DskipTests


FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

WORKDIR /adevguide
EXPOSE 3005

COPY rapporter/ rapporter/
COPY --from=maven /home/build/server/target/server-*jar ./statusplattform-server.jar

# Chainguard jre sets ENTRYPOINT to /usr/bin/java, so override it here rather
# than using CMD (which would be passed as args to /usr/bin/java).
ENTRYPOINT ["java", "-jar", "./statusplattform-server.jar"]
