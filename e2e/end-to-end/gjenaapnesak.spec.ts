import { faker } from "@faker-js/faker";
import { expect } from "@playwright/test";
import { attesterSakGodkjenn, behandleSakTilAttestring } from "./sak.utils";
import { test } from "./test.fixtures";

test.describe("Gjenåpne sak", () => {
    test.describe.configure({ mode: "serial" });

    const brukerFnr = `5${faker.string.numeric({ length: 10 })}`;

    test.beforeAll(async () => {
        console.debug(`Generert fødselsnummer for "gjenåpne sak" testene: ${brukerFnr}`);
    });

    test.beforeEach(async ({ page }) => {
        await page.goto("/");
    });

    test("Behandle sak som Sara Saksbehandler", async ({ auth, sak, journalforing }) => {
        await behandleSakTilAttestring({ auth, sak, journalforing }, brukerFnr);
    });

    test("Attester sak som Atle Attestant", async ({ page, auth, sok, sak }) => {
        await attesterSakGodkjenn({ page, auth, sok, sak }, brukerFnr);
    });

    test("Gjenåpne sak igjen som Sara", async ({ page, auth, sok, sak }) => {
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
            await row.getByRole("button", { name: "Åpne sak" }).click();
        });

        await test.step("Gjenåpne sak", async () => {
            await sak.selectBehandlingsmenyItem("Gjenåpne sak");
            const aarsakInput = page.getByRole("textbox", { name: "Årsak" });
            await expect(aarsakInput).toBeVisible();
            await aarsakInput.click();
            await aarsakInput.fill("Fordi fordi fordi");
            await page.getByRole("button", { name: "Gjenåpne" }).click();
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
