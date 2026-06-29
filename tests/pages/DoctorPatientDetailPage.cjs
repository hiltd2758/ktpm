// tests/pages/DoctorPatientDetailPage.cjs
'use strict';

const { I } = inject();

module.exports = {

  // ───────── Locators ─────────

  editBtn:       { xpath: "//button[normalize-space()='Chỉnh sửa']" },
  saveBtn:       { xpath: "//button[normalize-space()='Lưu thay đổi']" },
  cancelBtn:     { xpath: "//button[normalize-space()='Huỷ']" },
  backBtn:       { xpath: "//button[normalize-space()='Quay lại']" },

  saveSuccessMsg: 'div.text-green-700',
  saveErrorMsg:   'div.text-red-700',

  patientName:  { xpath: "//h2[contains(@class,'font-bold')]" },
  patientEmail: { xpath: "//p[contains(@class,'text-gray')]" },

  // ───────── Open Page ─────────

  /**
   * Navigate to the patient detail page for the given patientId.
   * @param {number|string} patientId
   */
  open(patientId) {
    I.amOnPage(`/doctor/patient/${patientId}`);
    I.wait(2);
  },

  // ───────── Assertions ─────────

  /**
   * Assert that at least the patient name heading is visible,
   * confirming the page has loaded its data.
   */
  seePatientLoaded() {
    I.waitForElement(this.patientName, 10);
    I.seeElement(this.patientName);
  },

  // ───────── Actions ─────────

  /** Click the "Chỉnh sửa" (Edit) button. */
  clickEdit() {
    I.waitForElement(this.editBtn, 10);
    I.click(this.editBtn);
  },

  /** Click the "Huỷ" (Cancel) button to exit edit mode. */
  clickCancel() {
    I.waitForElement(this.cancelBtn, 10);
    I.click(this.cancelBtn);
  },

  /** Click the "Lưu thay đổi" (Save) button. */
  clickSave() {
    I.waitForElement(this.saveBtn, 10);
    I.click(this.saveBtn);
  },

  /** Click the "Quay lại" (Back) button. */
  clickBack() {
    I.waitForElement(this.backBtn, 10);
    I.click(this.backBtn);
  },

  /**
   * Fill a clinical-info field identified by its visible label text.
   * Uses a dynamic XPath that finds the sibling <input> of the matching <label>.
   *
   * @param {string} label - The exact visible label text (e.g. "Nhóm máu").
   * @param {string} value - The value to type into the field.
   */
  fillClinicalField(label, value) {
    const fieldLocator = {
      xpath: `//label[normalize-space()="${label}"]/following-sibling::input`,
    };
    I.waitForElement(fieldLocator, 10);
    I.clearField(fieldLocator);
    I.fillField(fieldLocator, value);
  },

  // ───────── Success Message ─────────

  /** Assert that the green success banner is visible after saving. */
  seeSaveSuccess() {
    I.waitForElement(this.saveSuccessMsg, 10);
    I.seeElement(this.saveSuccessMsg);
  },

};
