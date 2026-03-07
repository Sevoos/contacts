FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

ARG MODULE_NAME

COPY gradlew .
COPY gradle gradle

COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY contacts-backend/build.gradle.kts contacts-backend/

COPY contacts-backend/${MODULE_NAME}/build.gradle.kts contacts-backend/${MODULE_NAME}/
COPY contacts-backend/shared/build.gradle.kts contacts-backend/shared/
COPY contacts-frontend/build.gradle.kts contacts-frontend/
COPY generation-annotations/build.gradle.kts generation-annotations/
COPY ksp-processor/build.gradle.kts ksp-processor/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :contacts-backend:${MODULE_NAME}:dependencies --no-daemon

COPY contacts-backend/${MODULE_NAME}/src contacts-backend/${MODULE_NAME}/src
COPY contacts-backend/shared/src contacts-backend/shared/src
    COPY contacts-frontend/src contacts-frontend/src
    COPY generation-annotations/src generation-annotations/src
    COPY ksp-processor/src ksp-processor/src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :contacts-backend:${MODULE_NAME}:bootJar --no-daemon #--no-build-cache

RUN cp /build/contacts-backend/${MODULE_NAME}/build/libs/*.jar /build/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /build/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
