import base from '@playwright/test'
import { AccessibilityScan } from './accessibility-scan'
import { AuthUtils } from './auth.utils'
import { SokPage } from './sok.page'

type Fixtures = {
   auth: AuthUtils
   accessibilityScan: AccessibilityScan
   sok: SokPage
}

export const test = base.extend<Fixtures>({
   page: async ({ page }, use) => {
      page.on('console', (m) => {
         if (m.type() === 'error') {
            const errorText = m.text()
            // Filter ut kjente development/HMR relaterte feil
            if (errorText.includes('Failed to fetch') && errorText.includes('manifest')) {
               console.warn('Ignoring development error:', errorText)
               return
            }
            throw Error(errorText)
         }
      })
      page.on('pageerror', (error) => {
         throw error
      })
      await use(page)
   },

   auth: async ({ page }, use) => {
      const auth = new AuthUtils(page)
      await use(auth)
   },

   accessibilityScan: async ({ page }, use) => {
      const accessibilityScan = new AccessibilityScan(page)
      await use(accessibilityScan)
   },

   sok: async ({ page }, use) => {
      const sok = new SokPage(page)
      await use(sok)
   },
})
