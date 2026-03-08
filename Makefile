frontend-build:
	./gradlew :contacts-frontend:exportJavascriptProduction
	docker compose build frontend

backend-build:
	docker compose build db contact icon-handling nginx

build: frontend-build backend-build

up:
	docker compose up