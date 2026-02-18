import { expect } from "@playwright/test";
import { test } from "./test.fixtures";
import { faker } from "@faker-js/faker";

test.describe("Feilregistrer sak", () => {
  test.describe.configure({ mode: "serial" });

  const brukerFnr = `5${faker.string.numeric({ length: 10 })}`;

  test.beforeAll(async () => {
    console.debug(
      `Generert fødselsnummer for "Feilregister sak" testene: ${brukerFnr}`,
    );
  });

  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test("Opprett sak og feilregisterer", async ({
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

    await test.step("Feilregisterer sak", async () => {
      await sak.selectBehandlingsmenyItem("Feilregistrer sak");
      const aarsakInput = page.getByRole("textbox", { name: "Årsak" });
      await expect(aarsakInput).toBeVisible();
      await aarsakInput.click();
      await aarsakInput.fill("Fordi fordi fordi");
      await page.getByRole("button", { name: "Feilregister" }).click();
    });

    await test.step("Sjekk at sak er feilregistrert", async () => {
      await expect(page.getByText("Sak feilregistrert")).toBeVisible();
    });
  });

});
