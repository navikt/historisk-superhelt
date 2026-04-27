import { faker } from "@faker-js/faker";
import { expect } from "@playwright/test";
import { test } from "./test.fixtures";

test.describe("Henlegg sak", () => {
    test.describe.configure({ mode: "serial" });

    const brukerFnr = `5${faker.string.numeric({ length: 10 })}`;

    test.beforeAll(async () => {
        console.debug(`Generert fødselsnummer for "Henlegg sak" testene: ${brukerFnr}`);
    });

    test.beforeEach(async ({ page }) => {
        await page.goto("/");
    });

    test("Opprett sak og henlegg", async ({ page, auth, sak, journalforing }) => {
        await test.step("Logg in Sara", async () => {
            await auth.loginSara();
        });

        await test.step("Journalfør", async () => {
            await journalforing.journalforNySak(brukerFnr, "SARAH", "REISEUTGIFTER");
        });

        await test.step("Henlegg sak", async () => {
            await sak.selectBehandlingsmenyItem("Henlegg sak");
            const aarsakInput = page.getByRole("textbox", { name: "Årsak" });
            await expect(aarsakInput).toBeVisible();
            await aarsakInput.click();
            await aarsakInput.fill("Fordi fordi fordi");

            await page.getByRole("button", { name: "Henlegg" }).click();
        });

        await test.step("Sjekk at sak er henlagt", async () => {
            await sak.selectMenuItem("Godkjenning");
            await expect(page.getByRole("heading", { name: "Saken er henlagt" })).toBeVisible();
        });
    });
});
