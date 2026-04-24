import type { Page } from "@playwright/test";
import { expect } from "@playwright/test";
import type { AuthUtils } from "./auth.utils";
import type { JournalforingPage } from "./journalforing.page";
import type { SakPage } from "./sak.page";
import type { SokPage } from "./sok.page";
import { test } from "./test.fixtures";

interface SaraFixtures {
    auth: AuthUtils;
    sak: SakPage;
    journalforing: JournalforingPage;
}

interface AtleFixtures {
    page: Page;
    auth: AuthUtils;
    sok: SokPage;
    sak: SakPage;
}

export async function behandleSakTilAttestring({ auth, sak, journalforing }: SaraFixtures, brukerFnr: string) {
    await test.step("Logg inn Sara", async () => {
        await auth.loginSara();
    });

    await test.step("Journalfør ny sak", async () => {
        await journalforing.journalforNySak(brukerFnr, "SARAH", "REISEUTGIFTER");
    });

    await test.step("Fyll inn opplysninger", async () => {
        await sak.fyllInnOpplysninger({
            beskrivelse: "Søknad om superkrefter",
            belop: "2345",
            begrunnelse: "Bruker har dokumentert behov for superkrefter.",
        });
    });

    await test.step("Skriv brev", async () => {
        await sak.skrivBrev();
    });

    await test.step("Send til attestering", async () => {
        await sak.sendTilAttering();
    });
}

export async function attesterSakGodkjenn({ page, auth, sok, sak }: AtleFixtures, brukerFnr: string) {
    await test.step("Logg inn Atle", async () => {
        await auth.loginAtle();
    });

    await test.step("Søk opp bruker", async () => {
        await sok.fnr(brukerFnr);
    });

    await test.step("Velg sak til attestering", async () => {
        await page.getByRole("button", { name: "Attester" }).click();
    });

    await test.step("Velg Godkjenning", async () => {
        await sak.selectMenuItem("Godkjenning");
    });

    await test.step("Attester og ferdigstill", async () => {
        await expect(page.getByRole("heading", { name: "Godkjenne sak" })).toBeVisible();
        await page.getByRole("radio", { name: "Godkjenn vedtak" }).check();
        await page.getByRole("button", { name: "Attester sak" }).click();
        await expect(page.getByRole("heading", { name: "Saken er innvilget og ferdigstilt" })).toBeVisible({
            timeout: 20_000,
        });
    });
}
