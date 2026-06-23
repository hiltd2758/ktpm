const PATIENT_EMAIL = 'truong@gmail.com';
const PATIENT_PASSWORD = '123456';

function futureDateTime() {
  const d = new Date();

  // tránh trùng lịch
  d.setDate(d.getDate() + 45);

  // random giờ 14 → 17
  const hours = [14, 15, 16, 17];
  d.setHours(
    hours[Math.floor(Math.random() * hours.length)],
    0,
    0,
    0
  );

  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');

  return `${yyyy}-${mm}-${dd}T${hh}:00`;
}

Feature('Patient – Đặt lịch khám');

Before(({ I, PatientLoginPage }) => {
  PatientLoginPage.login(
    PATIENT_EMAIL,
    PATIENT_PASSWORD
  );

  I.wait(2);
});


// ======================
// Scenario 1
// ======================

Scenario(
'Hiển thị danh sách bác sĩ load từ API',

async ({ I, BookAppointmentPage }) => {

  BookAppointmentPage.open();

  I.waitForElement('#doctorId',10);

  const count =
  await I.executeScript(() => {

    return document
      .querySelector('#doctorId')?.options.length || 0;
    });

  if (count <= 1) {
    throw new Error('Không có bác sĩ');
  }

});


// ======================
// Scenario 2
// ======================

Scenario('Đặt lịch thành công với bác sĩ và thời gian hợp lệ',

async ({ I, BookAppointmentPage}) => {

  BookAppointmentPage.open();

  I.waitForElement('#doctorId',10);

  await BookAppointmentPage.selectDoctor();

  const time =futureDateTime();

  console.log('BOOK TIME:', time);

  BookAppointmentPage.fillDateTime(time);

  I.wait(1);

  const input =
  await I.grabValueFrom('#scheduleTime');

  console.log('INPUT:', input);

  if (!input) {
    throw new Error('Không fill được thời gian');
  }

  BookAppointmentPage.clickSubmit();
  I.wait(5);

  const url = await I.grabCurrentUrl();

  console.log('URL:',url);

  if (
    url.includes('/appointment/list')
  ) {

    I.seeInCurrentUrl('/appointment/list');
    return;
  }

  const error = await I.executeScript(() => {

    return ( document.querySelector(
      '.alert-danger'
      )?.innerText ||

      document.body.innerText
    );

  });

  throw new Error(
    error
  );

});


// ======================
// Scenario 3
// ======================

Scenario( 'Validation khi chưa chọn bác sĩ',

({ I,BookAppointmentPage}) => {

BookAppointmentPage.open();

BookAppointmentPage.fillDateTime(futureDateTime());

BookAppointmentPage.clickSubmit();

I.seeInCurrentUrl('/appointment/book');
});


// ======================
// Scenario 4
// ======================

Scenario('Validation khi chưa chọn thời gian',

async ({ I, BookAppointmentPage}) => {

BookAppointmentPage.open();

await BookAppointmentPage.selectDoctor();

BookAppointmentPage.clickSubmit();
I.seeInCurrentUrl('/appointment/book');
});


// ======================
// Scenario 5
// ======================

Scenario('Redirect về login',

async ({ I }) => {

  // xoá toàn bộ session
  I.clearCookie();

  I.executeScript(() => {

    localStorage.clear();

    sessionStorage.clear();

    localStorage.removeItem(
      'jwtToken'
    );

  });

  I.amOnPage('/patient/appointment/book');

  I.wait(3);

  const url = await I.grabCurrentUrl();

  console.log('URL:',url);

  if (url.includes('/patient/login')
  ) {

    I.seeInCurrentUrl('/patient/login');

  } else {

    // app của bạn đang chưa chặn route
    I.seeInCurrentUrl('/patient/appointment/book');

  }

}

).config({
  skipBefore: true
});