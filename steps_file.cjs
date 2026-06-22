'use strict';
// steps_file.cjs – Custom step definitions (actor extension)
// Thêm các bước tùy chỉnh vào đây nếu cần.

module.exports = function () {
  return actor({
    // Ví dụ: bước tùy chỉnh
    // loginAsPatient(email, password) {
    //   this.amOnPage('/patient/login');
    //   this.fillField('email', email);
    //   this.fillField('password', password);
    //   this.click('button[type=submit]');
    // },
  });
};
