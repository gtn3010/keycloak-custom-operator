ARG BUILDERIMAGE="quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3-java11"
ARG BASEIMAGE="cgr.dev/chainguard/graalvm-native:latest"

FROM $BUILDERIMAGE as builder
WORKDIR /app
ADD . .
ENV MAVEN_VERSION="3.9.1"
ENV MAVEN_HOME="/app/apache-maven-${MAVEN_VERSION}"
ENV PATH="$PATH:$MAVEN_HOME/bin"
RUN curl -o maven.tar.gz https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && tar -xf maven.tar.gz
RUN --mount=type=cache,target=/home/quarkus/.m2,id=maven-build,uid=1001,gid=1001 \
       mvn clean package -Pnative -Doperator -Dquarkus.native.native-image-xmx=4g

FROM $BASEIMAGE
WORKDIR /app
COPY --from=builder /app/target/*-runner /app/keycloak-operator
ENTRYPOINT ["/app/keycloak-operator"]