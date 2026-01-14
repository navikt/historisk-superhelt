import {test} from './test.fixtures'
import {expect} from '@playwright/test'
import {faker} from '@faker-js/faker'


test.describe('Superhelt', () => {

    test.describe.configure({mode: 'serial'})

    const brukerFnr = `5${faker.string.numeric({length: 10})}`


    test.beforeAll(() => {
        console.debug(`Generert fødselsnummer for "Happy path saksbehandling" testene: ${brukerFnr}`)
    })

    test.beforeEach(async ({page}) => {
        await page.goto('/')
    })

    test('Ny sak', async ({page, auth, sok}) => {

        await test.step('Logg in Sara', async () => {
            await auth.loginSara()
        })

        await test.step('Søk opp bruker', async () => {
            await sok.fnr(brukerFnr)
        });

        await test.step('Opprett sak', async () => {
            await page.getByRole('button', {name: 'Opprett ny sak'}).click();
            await expect(page.getByRole('button', { name: 'Opplysninger' })).toBeVisible();
        });

        // await test.step('Fyll inn opplysninger', async () => {
        //     await page.getByRole('textbox', {name: 'Kort beskrivelse av stønad'}).fill('Søknad om superkrefter');
        //     await page.getByRole('radio', {name: 'Innvilget', exact: true} ).check();
        //     await page.getByRole('radio', {name: 'Direkte til bruker'}).check();
        //     await page.getByRole('textbox', {name: 'Beløp som skal utbetales (kr)'}).fill('2345');
        //     await page.getByRole('textbox', {name: 'Begrunnelse for vedtak'}).fill('Bruker har dokumentert behov for superkrefter.');
        //     await page.getByRole('button', {name: 'Lagre og gå videre'}).click();
        // });

        // await test.step('Skriv brev', async () => {
        //     await page.getByRole('button', {name: 'Lagre og gå videre'}).click();
        // });
        // await test.step('Send til attestering', async () => {
        //     await page.getByRole('button', {name: 'Send til attestering'}).click();
        // });

    });



});