frontend-build:
	./gradlew :contacts-frontend:exportJavascriptProduction
	docker compose build frontend

icon-handling-build:
	mkdir -p contacts-backend/icons
	docker compose build icon-handling

backend-build: icon-handling-build
	docker compose build db contact nginx

build: frontend-build backend-build

up:
	docker compose up