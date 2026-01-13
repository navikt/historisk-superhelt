import AxeBuilder from '@axe-core/playwright'
import type { Page } from '@playwright/test'

export class AccessibilityScan {
   constructor(public readonly page: Page) {}

   async violations() {
      const accessibilityScanResults = await new AxeBuilder({ page: this.page }).analyze()
      return (
         accessibilityScanResults.violations
            // TODO - Skipper over color-contrast problemer da det er feil i default knapper i aksel
            .filter((violation) => violation.id !== 'color-contrast')
      )
   }
}
