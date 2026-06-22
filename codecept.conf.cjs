// codecept.conf.cjs
'use strict';

/** @type {CodeceptJS.MainConfig} */
exports.config = {
  tests: './tests/e2e/**/*_test.js',
  output: './tests/output',
  helpers: {
    Playwright: {
      browser: 'chromium',
      url: 'http://localhost:8080',
      show: false,           // true = headed mode (để debug)
      waitForNavigation: 'networkidle',
      waitForAction: 500,
    },
  },
  include: {
    I: './steps_file.cjs',

    // ── Existing page objects ───────────────────────────────────────────────
    PatientLoginPage: './tests/pages/PatientLoginPage.cjs',

    // ── New page objects (EHC-31) ───────────────────────────────────────────
    BookAppointmentPage:      './tests/pages/BookAppointmentPage.cjs',
    PatientAppointmentsPage:  './tests/pages/PatientAppointmentsPage.cjs',
  },
  name: 'ktpm-e2e',
};
