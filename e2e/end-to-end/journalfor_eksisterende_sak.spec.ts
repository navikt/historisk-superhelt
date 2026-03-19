import {faker} from "@faker-js/faker";
import {expect} from "@playwright/test";
import {test} from "./test.fixtures";

test.describe("Journalfør på eksisterende åpen sak", () => {
    test.describe.configure({mode: "serial"});

    const brukerFnr = `5${faker.string.numeric({length: 10})}`;

    test.beforeAll(async () => {
        console.debug(`Generert fødselsnummer for "journalfør på eksisternde sak" testene: ${brukerFnr}`);
    });

    test.beforeEach(async ({page}) => {
        await page.goto("/");
    });

    test("Opprett sak", async ({auth, sak, journalforing}) => {
        await test.step("Logg in Sara", async () => {
            await auth.loginSara();
        });

        await test.step("Journalfør", async () => {
            await journalforing.journalforNySak(brukerFnr, "SARAH", "PARYKK", "Søknaden min");
        });

        await test.step("Fyll inn opplysninger", async () => {
            await sak.fyllInnOpplysninger({
                beskrivelse: "Søknad som får ettersendt noe",
                belop: "1",
                begrunnelse: "Bruker har dokumentert behov for superkrefter.",
            });
        });
    });
    test("Journalfør klage og knytt til sak", async ({page, auth, journalforing}) => {
        await test.step("Logg in Sara", async () => {
            await auth.loginSara();
        });

        await test.step("Journalfør", async () => {
            await journalforing.journalforEksisterendeSak(
                brukerFnr,
                "SARAH",
                "Søknad som får ettersendt noe",
                "Vedlegget mitt",
            );
        });
        await test.step("Verifiser at dokumentet er lagt til", async () => {
            await expect(page.getByRole("combobox", {name: "Dokumenter i saken"})).toContainText("Vedlegget mitt");
            await expect(page.getByRole("combobox", {name: "Dokumenter i saken"})).toContainText("Søknaden min");
        });
    });
});
