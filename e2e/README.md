<!-- TOC -->
* [Playwright Ende-til-Ende Tester for Historisk HelT 🚀](#playwright-ende-til-ende-tester-for-historisk-helt-)
  * [Kom i Gang](#kom-i-gang)
  * [Testing og Vedlikehold](#testing-og-vedlikehold)
    * [Oppdatere Snapshots](#oppdatere-snapshots)
    * [HTML-rapport](#html-rapport)
      * [Lokalt](#lokalt)
      * [Fra GitHub Actions](#fra-github-actions)
  * [Verktøy for Utvikling](#verktøy-for-utvikling)
    * [Visual Studio Code (VS Code)](#visual-studio-code-vs-code)
    * [Spille inn nye tester (Codegen)](#spille-inn-nye-tester-codegen)
    * [Playwright Inspector](#playwright-inspector)
  * [Kodingstil og Linting (Biome)](#kodingstil-og-linting-biome)
<!-- TOC -->
# Playwright Ende-til-Ende Tester for Historisk HelT 🚀

Velkommen til ende-til-ende testene for **Historisk HelT**-applikasjonen!

Denne mappen (`/tests`) inneholder Playwright-tester som validerer de viktigste brukerflytene i applikasjonen.

---

## Kom i Gang

Før du kan kjøre testene, må applikasjonen være i gang og alle avhengigheter installert.

1.  **Start applikasjonen:** Først må du bygge og starte frontend og backend. Se [README](../README.md) i prosjektets rotmappe for 
fullstendige instruksjoner. Deretter bygg å kjør docker imager:
    ```bash
    docker compose up --build --wait
    ```
    Alternativt må du starte tjenestene lokalt på annen måte.

2.  **Installer prosjektets avhengigheter:** Sørg for at du har installert alle prosjektets avhengigheter ved å kjøre `npm install` fra prosjektets **rotmappe**.
3.  **Installer nettlesere:** Første gang du kjører Playwright, må du installere nettleserne som skal brukes til testing. Kjør denne kommandoen fra mappen `/tests`:
    ```bash
    npx playwright install
    ```
4.  **Kjør alle tester:** Nå kan du starte testene. Kjør følgende kommando fra denne mappen (`/tests`). Testene kjører da i "headless" modus (uten synlig nettleservindu).
    ```bash
    npx playwright test
    ```
5.  **Kjør tester med UI:** For å se testene kjøre i en nettleser med Playwrights UI-modus, bruk følgende kommando. Dette er nyttig for feilsøking.
    ```bash
    npx playwright test --ui
    ```

---

## Testing og Vedlikehold

### Oppdatere Snapshots

Playwright bruker snapshots for å oppdage visuelle eller tekstbaserte regresjoner. Dette kan være **bilder av UI-komponenter** eller bare **tekstbasert innhold**. Dersom det er en tilsiktet endring i brukergrensesnittet eller innholdet, må snapshots oppdateres:
```bash
npx playwright test -u
```

**Viktig:** Verifiser alltid at endringene i snapshots er korrekte før du committer dem.

### HTML-rapport

Etter at testene er kjørt, genereres en detaljert HTML-rapport.

#### Lokalt

Fra denne mappen (`/tests`), kjør:
```bash
npx playwright show-report
```

#### Fra GitHub Actions

I seksjonen **Artifacts**, klikk på `playwright-report` for å laste ned rapporten som en zip-fil. Etter å ha pakket den ut, kan du åpne den med følgende kommando:
```bash
npx playwright show-report <navn-på-min-utpakkede-playwright-rapport>
```

For mer informasjon om CI, se Playwrights offisielle dokumentasjon: Setting up GitHub Actions.

---

## Verktøy for Utvikling

Selv om Playwright tilbyr kraftige verktøy for testing via kommandolinjen, **anbefales det å bruke Playwright-utvidelsen for Visual Studio Code** for en mer integrert og effektiv arbeidsflyt.

### Visual Studio Code (VS Code)

Installer utvidelsen **Playwright Test for VSCode** fra Marketplace. Denne utvidelsen gir en rekke fordeler:

* Enkel testkjøring og feilsøking direkte fra editoren.
* "Show Browser" for å se testene kjøre.
* Intellisense og autofullføring for Playwright-spesifikke metoder.
* Playwright Inspector og Codegen er sømløst integrert.
* "Record at cursor": Dette er en kraftig funksjon som lar deg lage nye tester eller legge til trinn i en eksisterende
test. Plasser markøren der du vil at opptaket skal starte, høyreklikk og velg "Record at cursor". Playwright vil 
starte et opptak av dine handlinger i nettleseren og generere kode rett i filen din.

De andre verktøyene som er nevnt under er gode alternativer for de som ikke bruker VS Code, men utvidelsen gjør utviklingen av tester mer intuitiv.

### Spille inn nye tester (Codegen)

Playwright har et verktøy som kan ta opp brukerinteraksjoner og generere testkode automatisk. Dette er en super måte å komme i gang på!

* **Spill inn mot et lokalt miljø (f.eks. `http://localhost:4000`):**
    ```bash
    npx playwright codegen http://localhost:4000
    ```
* **Spill inn mot en annen URL:**
    ```bash
    npx playwright codegen [https://www.eksempel.no](https://www.eksempel.no)
    ```

### Playwright Inspector

For avansert feilsøking kan du bruke Playwright Inspector, som lar deg trinnvis utføre tester og inspisere selektorer.
```bash
PWDEBUG=1 npx playwright test
```

---

## Kodingstil og Linting (Biome)

Prosjektet bruker **Biome** for formattering og linting i `/tests`.

- Kjør lint-sjekk:
    ```bash
    npm run lint
    ```
- Forsøk automatisk fiksing av lint-regler:
    ```bash
    npm run lint:fix
    ```
- Formater filer uten å skrive endringer:
    ```bash
    npm run format
    ```
- Formater og skriv endringer til disk:
    ```bash
    npm run format:write
    ```

Konfigurasjonen finnes i `biome.json`. Scriptene er definert i `package.json` i denne mappen.
