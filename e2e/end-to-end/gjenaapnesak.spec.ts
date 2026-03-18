import {faker} from "@faker-js/faker";
import {expect} from "@playwright/test";
import {test} from "./test.fixtures";

test.describe("Gjenåpne sak", () => {
    test.describe.configure({mode: "serial"});

    const brukerFnr = `5${faker.string.numeric({length: 10})}`;

    test.beforeAll(async () => {
        console.debug(`Generert fødselsnummer for "gjenåpne sak" testene: ${brukerFnr}`);
    });

    test.beforeEach(async ({page}) => {
        await page.goto("/");
    });

    test("Behandle sak som Sara Saksbehandler", async ({auth, sak, journalforing}) => {
        await test.step("Logg inn Sara", async () => {
            await auth.loginSara();
        });

        await test.step("Journalfør", async () => {
            await journalforing.journalfor(brukerFnr, "SARAH", "REISEUTGIFTER");
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
            await page.getByRole("button", {name: "Attester"}).click();
        });

        await test.step("Velg oppsummering ", async () => {
            await sak.selectMenuItem("Oppsummering");
        });

        await test.step("Attester og ferdigstill", async () => {
            await expect(page.getByRole("heading", {name: "Godkjenne sak"})).toBeVisible();
            await page.getByRole("radio", {name: "Godkjenn vedtak"}).check();
            await page.getByRole("button", {name: "Attester sak"}).click();
            await expect(page.getByRole("heading", {name: "ferdigstilt"})).toBeVisible({timeout: 20_000});
        });
    });

    test("Gjenåpne sak igjen som Sara", async ({page, auth, sok, sak}) => {
        await test.step("Logg in Sara", async () => {
            await auth.loginSara();
        });

        await test.step("Søk opp bruker", async () => {
            await sok.fnr(brukerFnr);
        });

        await test.step("Velg sak til attestering", async () => {
            const row = page.locator("tr", {
                has: page.locator(`text=Søknad om superkrefter`),
            });
            await row.getByRole("button", {name: "Åpne sak"}).click();
        });

        await test.step("Gjenåpne sak", async () => {
            await sak.selectBehandlingsmenyItem("Gjenåpne sak");
            const aarsakInput = page.getByRole("textbox", {name: "Årsak"});
            await expect(aarsakInput).toBeVisible();
            await aarsakInput.click();
            await aarsakInput.fill("Fordi fordi fordi");
            await page.getByRole("button", {name: "Gjenåpne"}).click();
        });


        await test.step("Verifier at sak er gjenåpnet", async () => {
            await expect(page.getByText("Sak er gjenåpnet")).toBeVisible();
            await sak.selectMenuItem("Opplysninger");
            await sak.fyllInnOpplysninger({
                beskrivelse: "Søknad om superkrefter v2",
                belop: "11",
                begrunnelse: "Bruker har dokumentert behov for superkrefter på nytt.",
            });

        });

    });
});
