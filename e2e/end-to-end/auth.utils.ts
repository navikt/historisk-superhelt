import { expect, type Locator, type Page } from '@playwright/test'

export class AuthUtils {
   saraSakbehandler: {
      navn: string
      locator: Locator
   }
   atleAssistant: {
      navn: string
      locator: Locator
   }
   fagkoordinatorFagesen: {
      navn: string
      locator: Locator
   }

   constructor(public readonly page: Page) {
      this.saraSakbehandler = { navn: 'Sarah', locator: page.getByText('Sarah Saksbehandler Saksbehandler') }
      this.atleAssistant = { navn: 'Atle', locator: page.getByText('Atle Attestant Saksbehandler') }
      this.fagkoordinatorFagesen = {
         navn: 'Fagkoordinator',
         locator: page.getByText('Fagkoordinator Fagesen Fagkoordinator'),
      }
   }

   async login(bruker: { navn: string; locator: Locator }) {
      await expect(async () => {
         await bruker.locator.locator('.btn').click()
         await expect(this.page).toHaveTitle('Superhelt');
         await expect(this.page.getByRole('button', { name: bruker.navn })).toBeVisible();
      }).toPass()
   }

   async loginSara() {
      await this.login(this.saraSakbehandler)
   }

   async loginAtle() {
      await this.login(this.atleAssistant)
   }

   async loginFagesen() {
      await this.login(this.fagkoordinatorFagesen)
   }

   async logout() {
      await expect(async () => {
         await this.page.waitForLoadState('domcontentloaded')
         await this.page.getByRole('button', { name: 'Brukermeny for' }).click()
         await expect(this.page.getByRole('link', { name: 'Logg ut' })).toBeVisible()
      }).toPass()
      await this.page.getByRole('link', { name: 'Logg ut' }).click()
      await expect(this.saraSakbehandler.locator).toBeVisible()
      await expect(this.atleAssistant.locator).toBeVisible()
   }
}
