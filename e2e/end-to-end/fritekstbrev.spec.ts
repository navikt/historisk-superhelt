import { faker } from "@faker-js/faker";
import { expect } from "@playwright/test";
import { test } from "./test.fixtures";

test.describe("Fritekstbrev", () => {
    test.describe.configure({ mode: "serial" });

    const brukerFnr = `5${faker.string.numeric({ length: 10 })}`;

    test.beforeAll(async () => {
        console.debug(`Generert fødselsnummer for "Fritekstbrev" testene: ${brukerFnr}`);
    });

    test.beforeEach(async ({ page }) => {
        await page.goto("/");
    });

    test("Opprett sak og lag fritekstbrev", async ({ page, auth, sak, journalforing }) => {
        await test.step("Logg in Sara", async () => {
            await auth.loginSara();
        });

        await test.step("Journalfør", async () => {
            await journalforing.journalforNySak(brukerFnr, "SARAH", "FOTSENG");
        });

        await test.step("Skriv fritekstbrev", async () => {
            await sak.selectBehandlingsmenyItem("Fritekstbrev til bruker");
            await page.getByRole("textbox", { name: "Dokumentbeskrivelse i arkivet" }).fill("Brev 1");

            const editorWrapper = page.getByTestId("tiptapeditor");
            const editor = editorWrapper.getByRole("textbox");
            await editor.click();
            await editor.pressSequentially("Header\n");
            await editor.pressSequentially(
                "Her er vanlig tekst i ett brev som er ganske lang kanskje så lang at den ikke får plass i en enkelt linje\n",
            );
            await editor.pressSequentially("Her er en liste:\n- Punkt 1\n- Punkt 2\n- Punkt 3\n\n");
            await editor.pressSequentially("Her er en lenke: https://www.nav.no\n");
            await editor.pressSequentially("**Bold** *italic* ==highlight==\n");

            await expect(editorWrapper).toMatchAriaSnapshot();

            await editorWrapper.blur();

            await page.getByRole("button", { name: "Standardtekster og signatur" }).click();
            await page.getByRole("button", { name: "Standardtekster og signatur" }).click();

            await page.getByRole("button", { name: "Send brev" }).click();
        });

        await test.step("Sjekk brevet", async () => {
            await page.getByLabel("Dokumenter i saken").selectOption("Brev 1", { timeout: 15000 });

            // TODO sjekk innhold i pdf
        });
    });
});
