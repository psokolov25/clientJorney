# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY client-journey-*/pom.xml ./
COPY client-journey-app/src ./client-journey-app/src
COPY client-journey-core/src ./client-journey-core/src
COPY client-journey-domain/src ./client-journey-domain/src
COPY client-journey-storage-spi/src ./client-journey-storage-spi/src
COPY client-journey-storage-file/src ./client-journey-storage-file/src
COPY client-journey-storage-h2/src ./client-journey-storage-h2/src
COPY client-journey-storage-postgres/src ./client-journey-storage-postgres/src
COPY client-journey-visit-spi/src ./client-journey-visit-spi/src
COPY client-journey-visit-mock/src ./client-journey-visit-mock/src
COPY client-journey-channel-spi/src ./client-journey-channel-spi/src

RUN mvn -q -pl client-journey-app -am package -DskipTests \
 && mvn -q -pl client-journey-app dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=/build/deps

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /build/client-journey-app/target/client-journey-app-*.jar /app/app.jar
COPY --from=builder /build/deps /app/lib

EXPOSE 8080
ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/lib/*", "com.clientjourney.app.Application"]
