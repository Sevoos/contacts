frontend-build:
	./gradlew :contacts-frontend:exportJavascriptProduction
	docker compose build frontend

contact-build:
	docker compose build contact

icon-handling-build:
	mkdir -p contacts-backend/icons
	docker compose build icon-handling

backend-build: contact-build icon-handling-build
#	docker compose build db nginx

build: frontend-build backend-build

up:
	docker compose up