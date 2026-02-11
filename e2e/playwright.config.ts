import {defineConfig, devices} from '@playwright/test'

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
import dotenv from 'dotenv'

dotenv.config({ path: '.env.test' })

const baseURL = process.env.BASE_URL ? process.env.BASE_URL : `http://localhost:4000`

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
   testDir: './end-to-end',
   /* Run tests in files in parallel */
   fullyParallel: true,
   /* Fail the build on CI if you accidentally left test.only in the source code. */
   forbidOnly: !!process.env.CI,
   /* Retry on CI only */
   retries: process.env.CI ? 2 : 0,
   /* Opt out of parallel tests on CI. */
   workers: process.env.CI ? 1 : undefined, //undefined, TODO-filer når tester kjører i parallel lokalt (kan være noe innloggings problmer)
   /* Reporter to use. See https://playwright.dev/docs/test-reporters */
   reporter: [['list'], ['html', { open: 'never' }]],
   /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
   use: {
      baseURL: baseURL,
      actionTimeout: 5000,

      /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
      trace: 'retain-on-failure',
   },

   /* Configure projects for major browsers */
   projects: [
      {
         name: 'chromium',
         use: { ...devices['Desktop Chrome'] },
      },


   ],
})
