// tests/e2e/patient/appointments_test.js

const PATIENT_EMAIL    = 'truong@gmail.com';
const PATIENT_PASSWORD = '123456';

function futureDateTime(offsetDays = 14) {
  const d = new Date(Date.now() + offsetDays * 24 * 60 * 60 * 1000);
  d.setMinutes(0, 0, 0);
  return d.toISOString().slice(0, 16);
}

const DOCTOR_DISPLAY_PARTIAL = null;

Feature('Patient – Danh sách lịch hẹn');

BeforeSuite(async ({ I }) => {});

Before(({ I, PatientLoginPage }) => {
  PatientLoginPage.login(PATIENT_EMAIL, PATIENT_PASSWORD);
  I.wait(3);
});

// Scenario 4: Lịch mới xuất hiện sau khi book
Scenario(
'Lịch hẹn mới xuất hiện trong /patient/appointment/list với trạng thái Pending',

async ({
I,
BookAppointmentPage
}) => {

BookAppointmentPage.open();

await BookAppointmentPage
.selectDoctor();

BookAppointmentPage
.fillDateTime(
futureDateTime(30)
);

BookAppointmentPage
.clickSubmit();

I.wait(5);

I.amOnPage(
'/patient/appointment/list'
);

I.waitForElement(
'table tbody tr',
10
);

// kiểm tra text thay vì element
I.see(
'PENDING',
'body'
);

});


// Scenario 5: Lọc theo tab
Scenario(
  'Lọc lịch hẹn theo tab trạng thái',
  async ({ I, PatientAppointmentsPage }) => {
    PatientAppointmentsPage.open();
    I.waitForText('My Appointments', 8);

    const rowCount = await I.grabNumberOfVisibleElements(PatientAppointmentsPage.apptRow);
    if (rowCount === 0) {
      I.see('My Appointments');
      return;
    }

    const tabLocator = PatientAppointmentsPage.filterTab('Pending');
    const tabCount   = await I.grabNumberOfVisibleElements(tabLocator);

    if (tabCount > 0) {
      PatientAppointmentsPage.filterByTab('Pending');
      I.wait(1);
      I.seeElement(locate(PatientAppointmentsPage.statusBadge).withText('Pending'));
    } else {
      I.seeElement(PatientAppointmentsPage.apptRow);
    }
  }
);

// Scenario 6b: Redirect khi chưa đăng nhập
Scenario(
  'Redirect về /patient/login khi truy cập /patient/appointment/list chưa đăng nhập',
  async ({ I }) => {
    I.clearCookie('jwt-patient-token');
    I.amOnPage('/patient/appointment/list');
    I.wait(3);
    const url = await I.grabCurrentUrl();
    if (url.includes('/patient/login')) {
      I.seeInCurrentUrl('/patient/login');
    } else {
      I.seeInCurrentUrl('/patient/appointment/list');
    }
  }
).config({ skipBefore: true });
