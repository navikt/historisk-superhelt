import {expect, type Page} from "@playwright/test";

export interface Opplysninger {
    beskrivelse?: string;
    belop?: string;
    begrunnelse?: string;
}

export class SakPage {
    constructor(public readonly page: Page) {
    }

    async selectMenuItem(
        item: "Oppsummering" | "Opplysninger" | "Brev til bruker",
    ) {
        const menuItem = this.page.getByRole("link", {name: item});
        await expect(menuItem).toBeEnabled();
        await menuItem.click();
        await expect(menuItem).toHaveClass(/active/);
    }

    async fyllInnOpplysninger(opplysninger: Opplysninger) {
        if (opplysninger.beskrivelse) {
            await this.page
                .getByRole("textbox", {name: "Kort beskrivelse av stønad"})
                .fill(opplysninger.beskrivelse);
        }

        await this.page
            .getByRole("radio", {name: "Innvilget", exact: true})
            .check();
        await this.page.getByRole("radio", {name: "Direkte til bruker"}).check();

        if (opplysninger.belop) {
            await this.page
                .getByRole("textbox", {name: "Beløp som skal utbetales"})
                .fill(opplysninger.belop);
        }

        await this.page.waitForLoadState("networkidle");

        if (opplysninger.begrunnelse) {
            await this.page
                .getByRole("textbox", {name: "Begrunnelse for vedtak"})
                .fill(opplysninger.begrunnelse);
        }

        await this.page.getByRole("button", {name: "Lagre og gå videre"}).click();
    }

    async skrivBrev() {
        await expect(
            this.page.getByText("Dokumentbeskrivelse i arkivet"),
        ).toBeVisible();
        await this.page.getByRole("button", {name: "Lagre og gå videre"}).click();
    }

    async selectBehandlingsmenyItem(item: "Feilregistrer sak"| "Henlegg sak") {
        const benhandlingsmeny= this.page.getByRole('button', { name: 'Behandlingsmeny' })
        await expect(benhandlingsmeny).toBeVisible();
        await benhandlingsmeny.click();
        await this.page.getByRole('menuitem', { name: item }).click();
    }   

    async sendTilAttering() {
        await expect(
            this.page.getByRole("heading", {name: "Til attestering"}),
        ).toBeVisible();
        await this.page
            .getByRole("button", {name: "Send til attestering"})
            .click();
        await this.page.waitForLoadState("networkidle");
        await expect(
            this.page.getByRole("heading", {name: "Godkjenne sak"}),
        ).toBeVisible();
    }

}
