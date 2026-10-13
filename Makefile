.DEFAULT_GOAL := help
.PHONY: help all build test up down test-all typecheck biome biome-write frontend-start frontend-build e2e-test e2e-test-ui

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
	pnpm test

up: ## Starter alle avhengigheter
	docker compose -f compose.yml up --wait -d

down: ## Stopper og fjerner alle avhengigheter
	docker compose -f compose.yml down

test-all: ## Kjører tester i alle workspaces
	pnpm test

typecheck: ## Kjører TypeScript typecheck i alle workspaces
	pnpm typecheck

biome: ## Kjører Biome lint- og formatsjekk
	pnpm biome

biome-write: ## Kjører Biome og retter feil
	pnpm biome:write

frontend-start: ## Starter frontend dev-server
	pnpm frontend:start

frontend-build: ## Bygger frontend
	pnpm frontend:build

e2e-test: ## Kjører E2E-tester (headless)
	pnpm e2e:test

e2e-test-ui: ## Kjører E2E-tester med UI
	pnpm e2e:test:ui

