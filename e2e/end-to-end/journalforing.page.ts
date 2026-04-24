import { expect, type Page } from "@playwright/test";
import { opprettJFR, tildelOppgave } from "./oppgave.utils";

export class JournalforingPage {
    constructor(public readonly page: Page) {}

    async journalforNySak(fnr: string, saksbehandler: string, soknadType = "REISEUTGIFTER", dokumentTittel?: string) {
        const jfrId = await opprettJFR(fnr);
        await tildelOppgave(jfrId, saksbehandler);
        await this.velgOppgave(fnr);
        await this.settTittel(dokumentTittel);
        await this.journalforOgStartBehandling(soknadType);
    }
    async journalforEksisterendeSak(
        fnr: string,
        saksbehandler: string,
        sakBeskrivelse = "PARYKK",
        dokumentTittel?: string,
    ) {
        const jfrId = await opprettJFR(fnr);
        await tildelOppgave(jfrId, saksbehandler);
        await this.velgOppgave(fnr);
        await this.settTittel(dokumentTittel);
        await this.journalforEksisterende(sakBeskrivelse);
    }

    private async velgOppgave(fnr: string) {
        const row = this.page.locator("tr", {
            has: this.page.locator(`text=${fnr}`),
        });
        await this.page.goto("/");

        await expect(this.page.getByRole("heading", { name: "Dine oppgaver fra Gosys" })).toBeVisible();
        await row.getByRole("button", { name: "Journalfør" }).click();
        await expect(this.page.getByRole("heading", { name: "Journalfør oppgave" })).toBeVisible();
    }

    private async settTittel(dokumentTittel: string | undefined) {
        if (dokumentTittel) {
            await this.page.getByRole("textbox", { name: "Dokumenttittel", exact: true }).fill(dokumentTittel);
        }
    }

    private async journalforOgStartBehandling(soknadType: string) {
        await this.page.getByRole("radio", { name: "Ny sak" }).check();
        await this.page.getByLabel("Velg type stønad").selectOption(soknadType);
        await this.page.getByRole("button", { name: "Journalfør og start behandling" }).click();
    }

    private async journalforEksisterende(sakbeskrivelse: string) {
        const eksisterendeSakRadio = this.page.getByLabel("Eksisterende sak", { exact: true });

        await this.page.getByText("Eksisterende sak", { exact: true }).click();
        await expect(eksisterendeSakRadio).toBeChecked();
        await this.page.getByRole("cell", { name: sakbeskrivelse }).click();
        await this.page.getByRole("button", { name: "Journalfør på eksisterende sak" }).click();
    }
}
