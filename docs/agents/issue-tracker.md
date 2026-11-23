# Issue tracker: GitHub

Issues og spesifikasjoner ligger i GitHub Issues. Bruk `gh` fra repoets mappe.

## Konvensjoner

- Kontroller målrepoet med `gh repo view --json nameWithOwner` før første opprettelse i en sesjon. Målet er `navikt/historisk-superhelt`.
- Opprett: `gh issue create --title "..." --body "..."`. Bruk heredoc for tekst over flere linjer.
- Les: `gh issue view <number> --json number,title,body,labels,comments`.
- List: `gh issue list --state open --json number,title,body,labels,comments`. Bruk relevante filtre.
- Kommenter: `gh issue comment <number> --body "..."`.
- Endre etiketter: `gh issue edit <number> --add-label "..."` eller `--remove-label "..."`.
- Lukk: publiser forklaringen som kommentar først, og kjør så `gh issue close <number>`. Kommenter separat også når saken allerede er lukket.

## Underordnede issues og avhengigheter

Bruk GitHubs native sub-issues og issue dependencies.

Med `gh` 2.94+:
- Knytt til forelder: `gh issue create --parent <parent> ...` eller `gh issue edit <parent> --add-sub-issue <child>`.
- Registrer blokkering: `gh issue create --blocked-by <n>,<n> ...` eller `gh issue edit <child> --add-blocked-by <blocker>`.

Med eldre `gh`, bruk API-et:
- Sub-issue: `gh api --method POST repos/{owner}/{repo}/issues/<parent>/sub_issues -F sub_issue_id=<child-db-id>`.
- Blokkering: `gh api --method POST repos/{owner}/{repo}/issues/<child>/dependencies/blocked_by -F issue_id=<blocker-db-id>`.
- Finn database-ID: `gh api repos/{owner}/{repo}/issues/<n> --jq .id`. Bruk database-ID, ikke issue-nummer eller node-ID.

Hvis funksjonene ikke er tilgjengelige, skriv `Part of #<parent>` eller `Blocked by: #<n>, #<n>` øverst i teksten. Forelderen får en oppgaveliste med barna.

Et issue er klart når alle blokkere er lukket. Tell åpne blokkere med `gh api repos/{owner}/{repo}/issues/<n> --jq .issue_dependencies_summary.blocked_by`.

## Pull requests as a triage surface

**PRs as a request surface: no.**

## Når en skill ber om publisering eller lesing

«Publish to the issue tracker» betyr å opprette et GitHub-issue.
«Fetch the relevant ticket» betyr å lese det aktuelle GitHub-issuet.

## Wayfinder

Kartet er ett issue med etiketten `wayfinder:map`. Teksten inneholder Notes, Decisions-so-far og Fog.

Oppgaver knyttes til kartet som sub-issues og får `wayfinder:<type>`, der type er `research`, `prototype`, `grilling` eller `task`.

Velg første åpne oppgave i kartets rekkefølge som ikke har åpne blokkere eller en assignee. Ta oppgaven med `gh issue edit <n> --add-assignee @me` før andre endringer.

Publiser svaret som kommentar, lukk oppgaven og legg en kontekstpeker i kartets Decisions-so-far.
