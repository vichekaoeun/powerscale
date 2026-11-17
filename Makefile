.PHONY: help up down logs infra infra-down deps voting voting2 gateway ranking frontend test build loadtest

help:
	@echo "make up          Build and run the full stack in Docker"
	@echo "make down        Stop the Docker stack"
	@echo "make logs        Follow Docker logs"
	@echo "make infra       Start only PostgreSQL, Kafka and Redis"
	@echo "make deps        Install the shared event module"
	@echo "make voting      Run the Voting Service on :8081"
	@echo "make voting2     Run a second Voting Service on :8083"
	@echo "make gateway     Run the API Gateway on :8080"
	@echo "make ranking     Run the Ranking Service on :8082"
	@echo "make frontend    Run the React dev server on :5173"
	@echo "make test        Run backend tests"
	@echo "make build       Build backend and frontend"
	@echo "make loadtest    Run the k6 voting load test"

up:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f --tail=100

infra:
	docker compose up -d postgres kafka redis
	sh scripts/init-databases.sh

infra-down:
	docker compose down

deps:
	mvn -q -pl libs/events -am install -DskipTests

voting: deps
	mvn -pl services/voting-service spring-boot:run

voting2: deps
	SERVER_PORT=8083 mvn -pl services/voting-service spring-boot:run

gateway:
	mvn -pl services/api-gateway spring-boot:run

ranking: deps
	mvn -pl services/ranking-service spring-boot:run

frontend:
	npm --prefix frontend run dev

test:
	mvn test

build:
	mvn verify
	npm --prefix frontend run build

loadtest:
	k6 run loadtest/voting.js
