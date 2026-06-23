// tests/e2e/doctor/patient_detail_test.js

// ─── Credentials ────────────────────────────────────────────────────────────
const DOCTOR_EMAIL    = 'doctor01@example.com';
const DOCTOR_PASSWORD = '123456';

// ─── Feature ─────────────────────────────────────────────────────────────────
Feature('Doctor - Patient Detail');

// ─── Suite-level variables ───────────────────────────────────────────────────
let VALID_PATIENT_ID;

BeforeSuite(() => {
  // Patient ID=1 (patient01@example.com) is guaranteed to have clinical info
  // in the seed data, so all "valid patient" scenarios use this ID.
  VALID_PATIENT_ID = 1;
});

// ─── Per-test setup: login as doctor ────────────────────────────────────────
Before(({ I, DoctorLoginPage }) => {
  DoctorLoginPage.login(DOCTOR_EMAIL, DOCTOR_PASSWORD);
  // Allow Zustand (auth-storage) to persist the JWT into localStorage.
  I.wait(3);
});

// ─── Scenarios ───────────────────────────────────────────────────────────────

/**
 * SC-01: Error message when patientId does not exist.
 */
Scenario(
  'Hiển thị lỗi khi patientId không tồn tại',
  ({ I, DoctorPatientDetailPage }) => {

    DoctorPatientDetailPage.open(99999);

    I.waitForText('Không thể tải thông tin bệnh nhân.', 10);
    I.see('Không thể tải thông tin bệnh nhân.');

  }
);

/**
 * SC-02: Back button is visible and navigates to /doctor/dashboard.
 */
Scenario(
  'Hiển thị nút Quay lại và điều hướng đúng',
  ({ I, DoctorPatientDetailPage }) => {

    DoctorPatientDetailPage.open(VALID_PATIENT_ID);
    DoctorPatientDetailPage.seePatientLoaded();

    DoctorPatientDetailPage.clickBack();

    I.wait(2);
    I.seeInCurrentUrl('/doctor/dashboard');

  }
);

/**
 * SC-03: Clicking Edit reveals the Save and Cancel buttons.
 */
Scenario(
  'Click Chỉnh sửa hiển thị form nhập liệu',
  ({ I, DoctorPatientDetailPage }) => {

    DoctorPatientDetailPage.open(VALID_PATIENT_ID);
    DoctorPatientDetailPage.seePatientLoaded();

    DoctorPatientDetailPage.clickEdit();

    I.waitForText('Lưu thay đổi', 5);
    I.see('Lưu thay đổi');
    I.see('Huỷ');

  }
);

/**
 * SC-04: Clicking Cancel exits edit mode and brings back the Edit button.
 */
Scenario(
  'Click Huỷ thoát khỏi chế độ chỉnh sửa',
  ({ I, DoctorPatientDetailPage }) => {

    DoctorPatientDetailPage.open(VALID_PATIENT_ID);
    DoctorPatientDetailPage.seePatientLoaded();

    DoctorPatientDetailPage.clickEdit();
    I.waitForText('Huỷ', 5);

    DoctorPatientDetailPage.clickCancel();

    // After cancel the Edit button must be visible again.
    I.waitForElement(DoctorPatientDetailPage.editBtn, 5);
    I.seeElement(DoctorPatientDetailPage.editBtn);

  }
);

/**
 * SC-05: Successfully update a clinical info field (blood type).
 */
Scenario(
  'Cập nhật thông tin lâm sàng thành công',
  ({ I, DoctorPatientDetailPage }) => {

    DoctorPatientDetailPage.open(VALID_PATIENT_ID);
    DoctorPatientDetailPage.seePatientLoaded();

    DoctorPatientDetailPage.clickEdit();

    // Frontend reads data from res.data.clinicalInfo; label text must match
    // exactly what the template renders.
    DoctorPatientDetailPage.fillClinicalField('Nhóm máu', 'A+');

    DoctorPatientDetailPage.clickSave();

    // A green banner (div.text-green-700) appears on success.
    DoctorPatientDetailPage.seeSaveSuccess();

  }
);

/**
 * SC-06: Unauthenticated access is redirected to /login.
 *        Uses `.config({ skipBefore: true })` so the Before hook (login)
 *        is NOT executed for this scenario.
 */
Scenario(
  'Trang chi tiết không truy cập được khi chưa đăng nhập',
  async ({ I }) => {

    // Clear all credentials so the app treats the user as a guest.
    I.clearCookie();
    I.executeScript(() => localStorage.clear());
    I.wait(1);

    I.amOnPage('/doctor/patient/1');
    I.wait(3);

    const url = await I.grabCurrentUrl();
    I.say(`Redirected to: ${url}`);

    I.seeInCurrentUrl('/login');

  }
).config({ skipBefore: true });
