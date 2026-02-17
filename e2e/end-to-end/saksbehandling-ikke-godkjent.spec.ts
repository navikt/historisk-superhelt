import { test } from "./test.fixtures";
import { faker } from "@faker-js/faker";
import { expect } from "@playwright/test";

test.describe("Saksbehandling og attestering ikke godkjent", () => {
  test.describe.configure({ mode: "serial" });

  const brukerFnr = `5${faker.string.numeric({ length: 10 })}`;

  test.beforeAll(async () => {
    console.debug(
      `Generert fødselsnummer for "Saksbehandling og attestering ikke godkjent" testene: ${brukerFnr}`,
    );
  });

  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test("Behandle sak som Sara Saksbehandler", async ({
    page,
    auth,
    sak,
    journalforing,
  }) => {
    await test.step("Logg in Sara", async () => {
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

  test("Avvis attestering som Atle Attestant", async ({ page, auth, sok, sak }) => {
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
      await row.getByRole("button", { name: "Åpne sak" }).click();
    });

    await test.step("Velg oppsummering ", async () => {
      await sak.selectMenuItem("Oppsummering");
    });

    await test.step("Attester og avvis", async () => {
      await test.step("Attester og tilbakefør", async () => {
        await expect(
          page.getByRole("heading", { name: "Godkjenne sak" }),
        ).toBeVisible();

        await page
          .getByRole("radio", { name: "Underkjenn og send tilbake" })
          .check();

        await page
          .getByRole("textbox", { name: "Årsak til avslag" })
          .fill("Dette må bli bedre");
        await page.getByRole("button", { name: "Attester sak" }).click();
      });

      await test.step("Sjekk at sak er under behandling", async () => {
        await expect(page.getByText("Sak returnert til saksbehandler")).toBeVisible();
      });
    });
  });
});
