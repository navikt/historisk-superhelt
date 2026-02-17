import {expect} from "@playwright/test";
import {test} from "./test.fixtures";
import {faker} from "@faker-js/faker";
import {opprettJFR, tildelOppgave} from "./oppgave.utils";

test.describe("Superhelt", () => {
    test.describe.configure({mode: "serial"});

    const brukerFnr = `5${faker.string.numeric({length: 10})}`;

    test.beforeAll(async () => {
        console.debug(
            `Generert fødselsnummer for "Happy path saksbehandling" testene: ${brukerFnr}`,
        );
        const jfrId = await opprettJFR(brukerFnr);
        await tildelOppgave(jfrId, "SARAH");
    });

    test.beforeEach(async ({page}) => {
        await page.goto("/");
    });

    test("Behandle sak som Sara Saksbehandler", async ({page, auth, sak}) => {
        await test.step("Logg in Sara", async () => {
            await auth.loginSara();
        });

        await test.step("Velg jfr oppgave", async () => {
            const row = page.locator("tr", {
                has: page.locator(`text=${brukerFnr}`),
            });
            await row.getByRole("button", {name: "Journalfør"}).click();
            await expect(
                page.getByRole("heading", {name: "Journalfør oppgave"}),
            ).toBeVisible();
        });

        await test.step("Journalfør", async () => {
            await page.getByLabel("Velg type søknad").selectOption("REISEUTGIFTER");
            await page
                .getByRole("button", {name: "Journalfør og start behandling"})
                .click();
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
    });

    test("Attester sak som Atle Attestant", async ({page, auth, sok, sak}) => {
        await test.step("Logg in Atle", async () => {
            await auth.loginAtle();
        });

        await test.step("Søk opp bruker", async () => {
            await sok.fnr(brukerFnr);
        });

        await test.step("Velg sak til attestering", async () => {
            const row = page.locator("tr", {
                has: page.locator(`text=Til attestering`),
            });
            await row.getByRole("button", {name: "Åpne sak"}).click();
            await expect(page.getByRole("heading", {name: "Sak"})).toBeVisible();
        });

        await test.step("Velg oppsummering ", async () => {
            await sak.selectMenuItem("Oppsummering");
        });

        await test.step("Attester ", async () => {
            await sak.attesterSak();
        });
    });
});
