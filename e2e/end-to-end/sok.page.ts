import {expect, type Page} from '@playwright/test'

export class SokPage {
    constructor(public readonly page: Page) {
    }

    async goto() {
        await this.page.goto('/')
    }

    async fnr(fnr: string) {
        const searchbox = this.page.getByRole('searchbox', {name: 'Søk'});
        await searchbox.fill(fnr)
        await searchbox.press('Enter')

        // await this.page.getByRole('button', { name: 'Søk' }).click()
        await expect(this.page.getByRole('heading', {name: 'Personside'})).toBeVisible()
        await expect(this.page.getByText(fnr)).toBeVisible()
    }
}
