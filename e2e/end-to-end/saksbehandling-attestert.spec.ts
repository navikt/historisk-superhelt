import { test } from "./test.fixtures";
import { faker } from "@faker-js/faker";
import { behandleSakTilAttestring, attesterSakGodkjenn } from "./sak.utils";

test.describe("Saksbehandling og attestering ok", () => {
    test.describe.configure({mode: "serial"});

    const brukerFnr = `5${faker.string.numeric({length: 10})}`;

    test.beforeAll(async () => {
        console.debug(
            `Generert fødselsnummer for "Saksbehandling og attestering ok" testene: ${brukerFnr}`,
        );
    });

    test.beforeEach(async ({page}) => {
        await page.goto("/");
    });

    test("Behandle sak som Sara Saksbehandler", async ({auth, sak, journalforing}) => {
        await behandleSakTilAttestring({auth, sak, journalforing}, brukerFnr);
    });

    test("Attester sak som Atle Attestant", async ({page, auth, sok, sak}) => {
        await attesterSakGodkjenn({page, auth, sok, sak}, brukerFnr);
    });
});
