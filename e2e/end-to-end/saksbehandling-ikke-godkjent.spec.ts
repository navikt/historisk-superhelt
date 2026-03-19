import { test } from "./test.fixtures";
import { faker } from "@faker-js/faker";
import { expect } from "@playwright/test";
import { behandleSakTilAttestring } from "./sak.utils";

test.describe("Saksbehandling og attestering ikke godkjent", () => {
    test.describe.configure({mode: "serial"});

    const brukerFnr = `5${faker.string.numeric({length: 10})}`;

    test.beforeAll(async () => {
        console.debug(
            `Generert fødselsnummer for "Saksbehandling og attestering ikke godkjent" testene: ${brukerFnr}`,
        );
    });

    test.beforeEach(async ({page}) => {
        await page.goto("/");
    });

    test("Behandle sak som Sara Saksbehandler", async ({auth, sak, journalforing}) => {
        await behandleSakTilAttestring({auth, sak, journalforing}, brukerFnr);
    });

    test("Avvis attestering som Atle Attestant", async ({page, auth, sok, sak}) => {
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

        await test.step("Attester og avvis", async () => {
            await test.step("Attester og tilbakefør", async () => {
                await expect(
                    page.getByRole("heading", {name: "Godkjenne sak"}),
                ).toBeVisible();

                await page
                    .getByRole("radio", {name: "Underkjenn og send tilbake"})
                    .check();

                await page
                    .getByRole("textbox", {name: "Årsak til avslag"})
                    .fill("Dette må bli bedre");
                await page.getByRole("button", {name: "Attester sak"}).click();
            });

            await test.step("Sjekk at sak blir returnert til saksbehandler", async () => {
              await expect(page.getByRole('heading', { name: 'Saken sendes tilbake' })).toBeVisible();
              await page.getByRole('button', { name: 'Gå til din oppgavebenk' }).click();
              await expect(page.getByRole('heading', { name: 'Dine oppgaver' })).toBeVisible();
            });
        });
    });
});
