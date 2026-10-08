#!/usr/bin/env bash
#
# Bygger en deterministisk oversikt over hva en pull request endrer.
#
# Produserer to filer i utkatalogen:
#   tabell.md  - markdown-tabell med berørte maven-moduler og linjeendringer
#   diff.txt   - avkortet git diff som kan sendes til en språkmodell
#
# Bruk: pr-endringsoversikt.sh <base-ref> <head-ref> <utkatalog>
set -euo pipefail

readonly KATEGORI_NAIS="Nais-konfigurasjon"
readonly KATEGORI_WORKFLOW="GitHub Actions"
readonly KATEGORI_ROT="Rot-konfigurasjon"
readonly KATEGORI_ANNET="Annet"

readonly MAKS_DIFF_TEGN="${MAKS_DIFF_TEGN:-60000}"
readonly TABELL_FILNAVN="tabell.md"
readonly DIFF_FILNAVN="diff.txt"

# Filer som ikke gir modellen nyttig kontekst, men spiser mye plass.
readonly EKSKLUDERTE_STIER=(
  ':(exclude)**/scripts/**'
  ':(exclude)pnpm-lock.yaml'
  ':(exclude)**/*.png'
  ':(exclude)**/*.jpg'
  ':(exclude)**/*.svg'
  ':(exclude)**/*.pdf'
  ':(exclude)**/*.woff2'
)

# kategoriser <filsti>
# Klassifiserer filer som ikke tilhører noen maven-modul.
kategoriser() {
  local fil="$1"
  case "$fil" in
    .nais/* | nais/*) printf '%s\n' "$KATEGORI_NAIS" ;;
    .github/*) printf '%s\n' "$KATEGORI_WORKFLOW" ;;
    package.json | pnpm-workspace.yaml | pnpm-lock.yaml | tsconfig*.json | biome.json | Dockerfile | compose.yml | CODEOWNERS | .gitignore | .npmrc | .editorconfig)
      printf '%s\n' "$KATEGORI_ROT" ;;
    *) printf '%s\n' "$KATEGORI_ANNET" ;;
  esac
}

# finn_modul <filsti> [repo-rot]
# Går oppover i katalogtreet fra filen og returnerer nærmeste katalog som
# inneholder en package.json. Håndterer dermed både apps/<app> og packages/<pakke>
# i pnpm-workspacet. Rot-package.json regnes ikke som modul, slik at
# rot-filer kategoriseres. Filer uten modul kategoriseres.
finn_modul() {
  local fil="$1"
  local rot="${2:-.}"
  local katalog
  katalog="$(dirname "$fil")"

  while [[ "$katalog" != "." && "$katalog" != "/" ]]; do
    if [[ -f "$rot/$katalog/package.json" ]]; then
      printf '%s\n' "$katalog"
      return 0
    fi
    katalog="$(dirname "$katalog")"
  done

  kategoriser "$fil"
}

# git_diff <argumenter...>
# Kjører git diff med core.quotepath=false slik at filnavn med æ/ø/å ikke blir
# escapet og pakket i hermetegn. Repoet har mange slike filnavn.
git_diff() {
  git -c core.quotepath=false diff "$@"
}

# bygg_tabell <base-ref> <head-ref> [repo-rot]
# Skriver en markdown-tabell med én rad per berørt modul/kategori.
# Unngår assosiative arrays for å være kompatibel med bash 3.2 (macOS).
bygg_tabell() {
  local base="$1"
  local head="$2"
  local rot="${3:-.}"

  local numstat
  numstat="$(git_diff --numstat "$base" "$head" -- "${EKSKLUDERTE_STIER[@]}")"

  if [[ -z "$numstat" ]]; then
    printf 'Ingen relevante filendringer.\n'
    return 0
  fi

  local lagt_til fjernet fil modul
  local per_fil=""

  while IFS=$'\t' read -r lagt_til fjernet fil; do
    [[ -z "$fil" ]] && continue
    # Binære filer rapporteres med "-" av git.
    [[ "$lagt_til" == "-" ]] && lagt_til=0
    [[ "$fjernet" == "-" ]] && fjernet=0
    modul="$(finn_modul "$fil" "$rot")"
    per_fil+="$modul	$lagt_til	$fjernet"$'\n'
  done <<< "$numstat"

  printf '| Modul | Filer | Linjer |\n'
  printf '| --- | ---: | --- |\n'
  printf '%s' "$per_fil" | awk -F'\t' '
    {
      if (!($1 in filer)) rekkefolge[++antall] = $1
      filer[$1]++
      lagt_til[$1] += $2
      fjernet[$1] += $3
    }
    END {
      n = asorti_stabil(rekkefolge, antall)
      for (i = 1; i <= antall; i++) {
        modul = rekkefolge[i]
        printf "| `%s` | %d | +%d / -%d |\n", modul, filer[modul], lagt_til[modul], fjernet[modul]
      }
    }
    function asorti_stabil(arr, n,   i, j, tmp) {
      for (i = 2; i <= n; i++) {
        tmp = arr[i]
        for (j = i - 1; j >= 1 && arr[j] > tmp; j--) arr[j + 1] = arr[j]
        arr[j + 1] = tmp
      }
      return n
    }
  '
}

# bygg_diff <base-ref> <head-ref>
# Skriver diffen, avkortet til MAKS_DIFF_TEGN med tydelig markering.
bygg_diff() {
  local base="$1"
  local head="$2"

  local diff
  diff="$(git_diff "$base" "$head" -- "${EKSKLUDERTE_STIER[@]}")"

  if (( ${#diff} > MAKS_DIFF_TEGN )); then
    printf '%s\n' "${diff:0:MAKS_DIFF_TEGN}"
    printf '\n[AVKORTET: diffen var %s tegn, kun de første %s er tatt med]\n' \
      "${#diff}" "$MAKS_DIFF_TEGN"
  else
    printf '%s\n' "$diff"
  fi
}

main() {
  if (( $# != 3 )); then
    echo "Bruk: $0 <base-ref> <head-ref> <utkatalog>" >&2
    return 2
  fi

  local base="$1"
  local head="$2"
  local utkatalog="$3"

  mkdir -p "$utkatalog"
  bygg_tabell "$base" "$head" > "$utkatalog/$TABELL_FILNAVN"
  bygg_diff "$base" "$head" > "$utkatalog/$DIFF_FILNAVN"
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  main "$@"
fi
