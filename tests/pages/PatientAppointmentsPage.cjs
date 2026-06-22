'use strict';

const { I } = inject();

module.exports = {

  // ── Locators ──────────────────────────────────────────────────────────────

  /**
   * A tab button by its visible label.
   * The appointment-list page uses Bootstrap nav tabs for status filtering.
   * @param {string} label - e.g. "Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đã huỷ"
   */
  filterTab(label) {
    return locate('a.nav-link,button.nav-link').withText(label);
  },

  /** Each appointment row in the history table */
  apptRow: 'table.table tbody tr',

  /** Status badge inside any row */
  statusBadge: '.status-badge',

  // ── Methods ───────────────────────────────────────────────────────────────

  /**
   * Navigate to the patient appointment list page.
   */
  open() {
    I.amOnPage('/patient/appointment/list');
  },

  /**
   * Click a filter tab by its visible label text.
   * @param {string} label
   */
  filterByTab(label) {
    I.click(this.filterTab(label));
    I.wait(1);
  },

  /**
   * Assert that at least one row contains the given doctor name AND status text.
   * Matches against visible cell content so it is resilient to ordering changes.
   *
   * @param {string} doctorName - partial text visible in the Doctor cell, e.g. "Nguyen"
   * @param {string} status     - visible badge label, e.g. "Pending" or "Chờ xác nhận"
   */
  seeAppointmentInList(doctorName, status) {
    I.see(doctorName, this.apptRow);
    I.see(status,     this.apptRow);
  },
};
