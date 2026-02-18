import { expect, type Page } from "@playwright/test";
import { opprettJFR, tildelOppgave } from "./oppgave.utils";

export class JournalforingPage {
  constructor(public readonly page: Page) {}

  async journalfor(fnr: string, saksbehandler: string, soknadType= "REISEUTGIFTER" ) {
    const jfrId = await opprettJFR(fnr);
    await tildelOppgave(jfrId, saksbehandler);
    await this.velgOppgave(fnr);
    await this.journalforOgStartBehandling(soknadType);
  }

    private async velgOppgave(fnr: string) {
    const row = this.page.locator("tr", {
      has: this.page.locator(`text=${fnr}`),
    });
    await this.page.goto("/");

    await expect(this.page.getByRole('heading', { name: 'Dine oppgaver fra Gosys' })).toBeVisible();
    await row.getByRole("button", { name: "Journalfør" }).click();
    await expect(
      this.page.getByRole("heading", { name: "Journalfør oppgave" }),
    ).toBeVisible();
  }

  private async journalforOgStartBehandling(soknadType: string) {
    await this.page.getByLabel("Velg type søknad").selectOption(soknadType);
    await this.page
      .getByRole("button", { name: "Journalfør og start behandling" })
      .click();
  }
}
