ARG BUILDERIMAGE="cgr.dev/chainguard/maven:openjdk-11"
ARG BASEIMAGE="cgr.dev/chainguard/jdk:openjdk-11"

FROM $BUILDERIMAGE as builder
WORKDIR /app
ADD . .
RUN mvn clean package -Pnative

FROM $BASEIMAGE
WORKDIR /app
COPY --from=builder /app/target/keycloak-operator.*jar .
USER 65532:65532
CMD ["-jar", "/app/"]