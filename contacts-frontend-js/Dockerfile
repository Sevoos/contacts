FROM eclipse-temurin:21-jdk AS gradle_builder
WORKDIR /build

COPY gradlew .
RUN chmod +x gradlew
RUN sed -i 's/\r$//' gradlew
COPY gradle gradle

COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY generation-annotations/build.gradle.kts generation-annotations/
COPY ksp-processor/build.gradle.kts ksp-processor/

COPY contacts-frontend/build.gradle.kts contacts-frontend/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :contacts-frontend:dependencies --no-daemon --parallel --configure-on-demand

COPY generation-annotations/src generation-annotations/src
COPY ksp-processor/src ksp-processor/src

COPY contacts-frontend/src contacts-frontend/src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :contacts-frontend:exportJavascriptProduction --no-daemon --configure-on-demand --parallel

FROM node:20 AS frontend-build
WORKDIR /app

COPY --from=gradle_builder /build/contacts-frontend-js/kotlin-lib ./kotlin-lib/

ARG CONTACT_API
ARG ICON_HANDLING_API
ARG SERVER_HOST_EXTERNAL
ARG API_PORT

ENV VITE_CONTACT_API=$CONTACT_API
ENV VITE_ICON_HANDLING_API=$ICON_HANDLING_API
ENV VITE_SERVER_HOST_EXTERNAL=$SERVER_HOST_EXTERNAL
ENV VITE_API_PORT=$API_PORT

COPY contacts-frontend-js/package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci

# Copy project files
COPY contacts-frontend-js .

# Build production bundle
RUN npm run build

FROM nginx:alpine

# Remove default nginx content
# RUN rm -rf /usr/share/nginx/html/*

# Copy built frontend
COPY --from=frontend-build /app/dist /usr/share/nginx/html

# Optional: SPA routing support
COPY contacts-frontend-js/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
