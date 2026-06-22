'use strict';

const { I } = inject();

module.exports = {

  // ───────── Locators ─────────

  doctorSelect: '#doctorId',
  dateTimeInput: '#scheduleTime',

  submitBtn: 'button[type="submit"]',

  successMsg: '.alert-success',
  errorMsg: '.alert-danger',

  // ───────── Open Page ─────────

  open() {
    I.amOnPage(
      '/patient/appointment/book'
    );

    I.waitForElement(
      this.doctorSelect,
      10
    );
  },

  // ───────── Select Doctor ─────────

  async selectDoctor(name = null) {

    I.waitForElement(
      this.doctorSelect,
      10
    );

    // chọn theo tên
    if (name) {

      I.selectOption(
        this.doctorSelect,
        name
      );

      return;

    }

    // tự lấy doctor cuối
    const doctors =
      await I.executeScript(() => {

        const select =
          document.querySelector(
            '#doctorId'
          );

        if (!select) {
          return [];
        }

        return Array
          .from(select.options)
          .slice(1)
          .map(x => ({
            value: x.value,
            text: x.text
          }));

      });

    if (!doctors.length) {

      throw new Error(
        'Không có bác sĩ'
      );

    }

    const doctor =
      doctors[
        doctors.length - 1
      ];

    console.log(
      'Using doctor:',
      doctor
    );

    I.selectOption(
      this.doctorSelect,
      doctor.value
    );

  },

  // ───────── Fill Datetime ─────────

  fillDateTime(value) {

    I.waitForElement(
      this.dateTimeInput,
      10
    );

    // datetime-local thường lỗi với fillField
    I.executeScript((v) => {

      const input =
        document.querySelector(
          '#scheduleTime'
        );

      if (!input) {
        return;
      }

      input.value = v;

      input.dispatchEvent(
        new Event(
          'input',
          {
            bubbles: true
          }
        )
      );

      input.dispatchEvent(
        new Event(
          'change',
          {
            bubbles: true
          }
        )
      );

    }, value);

  },

  // ───────── Submit ─────────

  clickSubmit() {

    I.waitForElement(
      this.submitBtn,
      10
    );

    I.click(
      this.submitBtn
    );

  },

  // ───────── Assertions ─────────

  seeSuccess() {

    I.wait(5);

    I.seeInCurrentUrl(
      '/patient/appointment/list'
    );

  },

  seeError() {

    I.waitForVisible(
      this.errorMsg,
      5
    );

    I.seeElement(
      this.errorMsg
    );

  }

};