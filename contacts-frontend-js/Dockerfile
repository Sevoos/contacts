FROM eclipse-temurin:21-jdk AS gradle-build
WORKDIR /build

COPY gradlew .
COPY gradle gradle

COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY contacts-frontend/build.gradle.kts contacts-frontend/
COPY generation-annotations/build.gradle.kts generation-annotations/
COPY ksp-processor/build.gradle.kts ksp-processor/

COPY contacts-frontend/src contacts-frontend/src
COPY generation-annotations/src generation-annotations/src
COPY ksp-processor/src ksp-processor/src

COPY contacts-frontend-js contacts-frontend-js

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :contacts-frontend:exportJavascriptProduction --no-daemon

FROM node:20-alpine AS frontend-build

WORKDIR /app

ARG CONTACT_API
ARG ICON_HANDLING_API
ARG SERVER_HOST_EXTERNAL
ARG API_PORT
ENV VITE_CONTACT_API=$CONTACT_API
ENV VITE_ICON_HANDLING_API=$ICON_HANDLING_API
ENV VITE_SERVER_HOST_EXTERNAL=$SERVER_HOST_EXTERNAL
ENV VITE_API_PORT=$API_PORT

# Install dependencies
COPY contacts-frontend-js/package*.json ./
COPY contacts-frontend-js/src/lib/package.json ./src/lib/package.json
RUN npm install

# Copy project files
COPY contacts-frontend-js/ .

COPY --from=gradle-build /build/contacts-frontend-js/src/lib ./src/lib

# Build production bundle
RUN npm run build

FROM nginx:alpine

# Remove default nginx content
RUN rm -rf /usr/share/nginx/html/*

# Copy built frontend
COPY --from=frontend-build /app/dist /usr/share/nginx/html

# Optional: SPA routing support
COPY contacts-frontend-js/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]