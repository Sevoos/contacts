# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY local-repo local-repo
COPY shared shared

COPY icon-handling/build.gradle.kts icon-handling/build.gradle.kts
COPY icon-handling/src icon-handling/src

COPY contact/build.gradle.kts contact/build.gradle.kts

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :icon-handling:bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /build/icon-handling/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
