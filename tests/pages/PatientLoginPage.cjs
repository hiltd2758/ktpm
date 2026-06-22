'use strict';

const { I } = inject();

module.exports = {

  // Locators
  emailInput: '#email',
  passwordInput: '#password',
  submitBtn: 'button[type="submit"]',
  errorMsg: '.error-message',

  open() {
    I.amOnPage('/patient/login');
  },

  async login(email, password) {

    this.open();

    I.waitForElement(this.emailInput, 10);

    I.fillField(this.emailInput, email);
    I.fillField(this.passwordInput, password);

    I.click(this.submitBtn);

    // đợi redirect thật sự
    I.wait(5);

    const url =
      await I.grabCurrentUrl();

    console.log(
      'LOGIN URL:',
      url
    );

    const html =
      await I.grabSource();

    // login fail
    if (
      url.includes('/patient/login')
    ) {

      console.log(
        'LOGIN HTML:',
        html
      );

      throw new Error(
        'Đăng nhập thất bại'
      );

    }

    // xác nhận đã có menu user
    I.waitForText(
      'TRƯỜNG',
      10
    );

  },

  seeLoginError() {
    I.waitForVisible(
      this.errorMsg,
      5
    );

    I.seeElement(
      this.errorMsg
    );
  }

};