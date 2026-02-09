import {expect} from "@playwright/test";
import {test} from "./test.fixtures";
import {faker} from "@faker-js/faker";

test.describe("Superhelt", () => {
    test.describe.configure({mode: "serial"});

    const brukerFnr = `5${faker.string.numeric({length: 10})}`;

    test.beforeAll(async ({oppgave}) => {
        console.debug(
            `Generert fødselsnummer for "Happy path saksbehandling" testene: ${brukerFnr}`,
        );
        const jfrId = await oppgave.opprettJFR(brukerFnr);
        await oppgave.tildelOppgave(jfrId, "SARAH");
    });

    test.beforeEach(async ({page}) => {
        await page.goto("/");
    });

    test("Behandle sak som Sara Saksbehandler", async ({page, auth}) => {
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
            await expect(page.getByRole("link", {name: "Opplysninger"})).toHaveClass(
                /active/,
            );
            await page
                .getByRole("textbox", {name: "Kort beskrivelse av stønad"})
                .fill("Søknad om superkrefter");

            await page.getByRole("radio", {name: "Innvilget", exact: true}).check();
            await page.getByRole("radio", {name: "Direkte til bruker"}).check();
            await page
                .getByRole("textbox", {name: "Beløp som skal utbetales"})
                .fill("2345");
            // Vent på at beløp er lagret ferdig
            await page.waitForLoadState("networkidle");

            await page
                .getByRole("textbox", {name: "Begrunnelse for vedtak"})
                .fill("Bruker har dokumentert behov for superkrefter.");
            await page.getByRole("button", {name: "Lagre og gå videre"}).click();
        });

        await test.step("Skriv brev", async () => {
            await expect(page.getByRole("link", {name: "Brev til bruker"})).toHaveClass(/active/);
            await expect(page.getByText("Dokumentbeskrivelse i arkivet")).toBeVisible();
            await page.getByRole("button", {name: "Lagre og gå videre"}).click();
            await page.waitForLoadState("networkidle");
        });
        await test.step("Send til attestering", async () => {
            await expect(page.getByRole("link", {name: "Oppsummering"})).toHaveClass(/active/);
            await expect(page.getByRole("heading", {name: "Til attestering"})).toBeVisible();
            await page.getByRole("button", {name: "Send til attestering"}).click();
            await page.waitForLoadState("networkidle");
            await expect(page.getByRole("heading", {name: "Godkjenne sak"})).toBeVisible();
        });
    });

    test("Attester sak som Atle Attestant", async ({page, auth, sok}) => {
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
            await page.getByRole("button", {name: "Oppsummering"}).click();
            //   expect(page.getByRole("link", { name: "Oppsummering" })).toHaveClass(
            //     /active/,
            //   );
        });
        await test.step("Attester ", async () => {
            await page.getByRole('button', {name: 'Attester sak'}).click();
            await expect(
                page.getByRole("heading", {name: "Godkjenne sak"}),
            ).toBeVisible();
            await page.getByRole('radio', {name: 'Godkjenn vedtak'}).check();
            await page.getByRole('button', {name: 'Attester sak'}).click();
            await page.waitForLoadState("networkidle");
            await expect(page.getByRole("heading", {name: "ferdigstilt"})).toBeVisible();
        });
    });
});
