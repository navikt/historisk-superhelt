.DEFAULT_GOAL := help
.PHONY: help all build test up down

help: ## Vis tilgjengelige targets og beskrivelse
	@echo "Bruk: make [target]"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; print "Targets:"} \
		/^[a-zA-Z0-9_.-]+:.*##/ {printf "  %-14s %s\n", $$1, $$2} \
		/^##@/ {printf "\n%s\n", substr($$0,5)}' $(MAKEFILE_LIST)

all: ## Bygger og tester applikasjonen
	mvn clean verify -T 1C
	docker compose -f compose.yml build

build: ## Bygger applikasjonen og Docker images
	mvn clean package -DskipTests -T 1C
	docker compose -f compose.yml build

test: ## Kjører tester
	mvn test -T 1C

up: ## Starter alle avhengigheter
	docker compose -f compose.yml up --wait -d

down: ## Stopper og fjerner alle avhengigheter
	docker compose -f compose.yml down
