import base from '@playwright/test'
import {AccessibilityScan} from './accessibility-scan'
import {AuthUtils} from './auth.utils'
import {SokPage} from './sok.page'
import {SakPage} from './sak.page'
import {JournalforingPage} from './journalforing.page'

type Fixtures = {
    auth: AuthUtils
    accessibilityScan: AccessibilityScan
    sok: SokPage
    sak: SakPage
    journalforing: JournalforingPage
}

export const test = base.extend<Fixtures>({
    page: async ({page}, use) => {
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

    auth: async ({page}, use) => {
        const auth = new AuthUtils(page)
        await use(auth)
    },

    accessibilityScan: async ({page}, use) => {
        const accessibilityScan = new AccessibilityScan(page)
        await use(accessibilityScan)
    },

    sok: async ({page}, use) => {
        const sok = new SokPage(page)
        await use(sok)
    },

    sak: async ({page}, use) => {
        const sak = new SakPage(page)
        await use(sak)
    },

    journalforing: async ({page}, use) => {
        const journalforing = new JournalforingPage(page)
        await use(journalforing)
    },
})
