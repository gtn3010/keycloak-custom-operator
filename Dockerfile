ARG BUILDERIMAGE="quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3-java11"
ARG BASEIMAGE="cgr.dev/chainguard/graalvm-native:latest"

FROM $BUILDERIMAGE as builder
WORKDIR /app
ADD . .

RUN 
RUN mvn clean package -Pnative

FROM $BASEIMAGE
WORKDIR /app
COPY --from=builder /app/target/keycloak-operator.*jar .
CMD ["-jar", "/app/"]