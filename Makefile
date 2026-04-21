.DEFAULT_GOAL := help
.PHONY: help all build test up down

MVN ?= $(shell command -v mvnd >/dev/null 2>&1 && echo mvnd || echo mvn) # Bruk mvnd hvis tilgjengelig, ellers fallback til mvn

# Prosjektet krever Java 25.
# Sett JAVA_HOME via sdkman (`sdk use java 25-...`) eller tilsvarende verktøy før du kjører make.
# Makefilen overstyrer ikke JAVA_HOME – bruk sdkman, jenv eller lignende til å styre aktiv JDK.

help: ## Vis tilgjengelige targets og beskrivelse
	@echo "Bruk: make [target]"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; print "Targets:"} \
		/^[a-zA-Z0-9_.-]+:.*##/ {printf "  %-14s %s\n", $$1, $$2} \
		/^##@/ {printf "\n%s\n", substr($$0,5)}' $(MAKEFILE_LIST)

all: ## Bygger og tester applikasjonen
	$(MVN) clean verify -T 1C
	docker compose -f compose.yml build

build: ## Bygger applikasjonen og Docker images
	$(MVN) clean package -DskipTests -T 1C
	docker compose -f compose.yml build

test: ## Kjører tester
	$(MVN) test -T 1C

up: ## Starter alle avhengigheter
	docker compose -f compose.yml up --wait -d

down: ## Stopper og fjerner alle avhengigheter
	docker compose -f compose.yml down
