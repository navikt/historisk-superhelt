#!/usr/bin/env bash
#
# Tester for pr-endringsoversikt.sh. Kjøres uten argumenter:
#   .github/scripts/test-pr-endringsoversikt.sh
set -euo pipefail

SKRIPT_KATALOG="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=pr-endringsoversikt.sh
source "$SKRIPT_KATALOG/pr-endringsoversikt.sh"

ANTALL_FEIL=0

paastaa_lik() {
  local forventet="$1" faktisk="$2" beskrivelse="$3"
  if [[ "$forventet" == "$faktisk" ]]; then
    printf 'OK   %s\n' "$beskrivelse"
  else
    printf 'FEIL %s\n     forventet: %s\n     faktisk:   %s\n' \
      "$beskrivelse" "$forventet" "$faktisk"
    ANTALL_FEIL=$((ANTALL_FEIL + 1))
  fi
}

paastaa_inneholder() {
  local naal="$1" hoystakk="$2" beskrivelse="$3"
  if [[ "$hoystakk" == *"$naal"* ]]; then
    printf 'OK   %s\n' "$beskrivelse"
  else
    printf 'FEIL %s\n     fant ikke: %s\n     i:         %s\n' \
      "$beskrivelse" "$naal" "$hoystakk"
    ANTALL_FEIL=$((ANTALL_FEIL + 1))
  fi
}

# Bygger et minimalt repo som speiler strukturen i bidrag-frontend.
lag_fikstur() {
  local rot
  rot="$(mktemp -d)"
  mkdir -p "$rot/apps/web/app"
  mkdir -p "$rot/apps/behandling/src"
  mkdir -p "$rot/packages/api/src"
  mkdir -p "$rot/.nais/web" "$rot/nais" "$rot/.github/workflows"
  touch "$rot/package.json"
  touch "$rot/apps/web/package.json"
  touch "$rot/apps/behandling/package.json"
  touch "$rot/packages/api/package.json"
  printf '%s\n' "$rot"
}

ROT="$(lag_fikstur)"
trap 'rm -rf "$ROT"' EXIT

paastaa_lik "apps/web" \
  "$(finn_modul "apps/web/app/root.tsx" "$ROT")" \
  "finn_modul finner nærmeste app-modul"

paastaa_lik "apps/behandling" \
  "$(finn_modul "apps/behandling/src/App.tsx" "$ROT")" \
  "finn_modul finner behandling-appen"

paastaa_lik "packages/api" \
  "$(finn_modul "packages/api/package.json" "$ROT")" \
  "finn_modul plasserer modulens egen package.json i modulen"

paastaa_lik "packages/api" \
  "$(finn_modul "packages/api/src/index.ts" "$ROT")" \
  "finn_modul håndterer packages"

paastaa_lik "$KATEGORI_NAIS" \
  "$(finn_modul ".nais/web/nais.yaml" "$ROT")" \
  "finn_modul kategoriserer .nais"

paastaa_lik "$KATEGORI_NAIS" \
  "$(finn_modul "nais/felles.yaml" "$ROT")" \
  "finn_modul kategoriserer nais"

paastaa_lik "$KATEGORI_WORKFLOW" \
  "$(finn_modul ".github/workflows/deploy.yaml" "$ROT")" \
  "finn_modul kategoriserer workflows"

paastaa_lik "$KATEGORI_ROT" \
  "$(finn_modul "package.json" "$ROT")" \
  "finn_modul kategoriserer rot-package.json"

paastaa_lik "$KATEGORI_ANNET" \
  "$(finn_modul "README.md" "$ROT")" \
  "finn_modul kategoriserer ukjente filer på rotnivå"

# bygg_tabell mot et ekte git-repo med to commits.
GIT_ROT="$(mktemp -d)"
trap 'rm -rf "$ROT" "$GIT_ROT"' EXIT
(
  cd "$GIT_ROT"
  git init --quiet --initial-branch=main
  git config user.email "test@example.com"
  git config user.name "Test"
  git config commit.gpgsign false
  git config gpg.format openpgp
  mkdir -p apps/web/src .nais/web
  touch package.json apps/web/package.json
  printf 'linje1\n' > apps/web/src/App.tsx
  git add -A && git commit --quiet -m "start"

  printf 'linje1\nlinje2\nlinje3\n' > apps/web/src/App.tsx
  printf 'image: test\n' > .nais/web/nais.yaml
  printf 'export const samvær = { id: 1 }\n' > apps/web/src/Samvær.tsx
  git add -A && git commit --quiet -m "endring"
)

TABELL="$(cd "$GIT_ROT" && bygg_tabell HEAD~1 HEAD .)"
paastaa_inneholder "| \`apps/web\` | 2 | +3 / -0 |" "$TABELL" \
  "bygg_tabell teller filer og linjer per modul"
paastaa_inneholder "| \`$KATEGORI_NAIS\` | 1 | +1 / -0 |" "$TABELL" \
  "bygg_tabell tar med nais-endringer"
if [[ "$TABELL" == *"$KATEGORI_ANNET"* ]]; then
  printf 'FEIL filnavn med æ/ø/å havner i "%s" (mangler core.quotepath=false)\n' \
    "$KATEGORI_ANNET"
  ANTALL_FEIL=$((ANTALL_FEIL + 1))
else
  printf 'OK   filnavn med æ/ø/å knyttes til riktig modul\n'
fi

TOM_TABELL="$(cd "$GIT_ROT" && bygg_tabell HEAD HEAD .)"
paastaa_lik "Ingen relevante filendringer." "$TOM_TABELL" \
  "bygg_tabell håndterer diff uten endringer"

# bygg_diff skal avkorte når diffen er større enn grensen.
DIFF="$(cd "$GIT_ROT" && env MAKS_DIFF_TEGN=50 bash -c 'source "$1"; bygg_diff HEAD~1 HEAD' bash "$SKRIPT_KATALOG/pr-endringsoversikt.sh")"
paastaa_inneholder "AVKORTET" "$DIFF" "bygg_diff avkorter store differ"

if (( ANTALL_FEIL > 0 )); then
  printf '\n%s test(er) feilet\n' "$ANTALL_FEIL"
  exit 1
fi
printf '\nAlle tester passerte\n'
