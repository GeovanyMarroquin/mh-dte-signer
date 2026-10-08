FROM eclipse-temurin:25-jdk AS build

WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-cryptography \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system signer \
    && useradd --system --gid signer --home-dir /app --no-create-home signer

WORKDIR /app
COPY --from=build /app/build/libs/mh-dte-signer-0.0.1-SNAPSHOT.jar /app/app.jar
COPY scripts/ /app/scripts/
RUN mkdir -p /app/uploads/certs \
    && chown -R signer:signer /app

USER signer

EXPOSE 8113
CMD ["java", "-jar", "/app/app.jar"]

