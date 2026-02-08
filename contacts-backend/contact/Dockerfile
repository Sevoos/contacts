# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY local-repo local-repo
COPY shared shared

COPY contact/build.gradle.kts contact/build.gradle.kts
COPY contact/src contact/src


COPY icon-handling/build.gradle.kts icon-handling/build.gradle.kts

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :contact:bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /build/contact/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
