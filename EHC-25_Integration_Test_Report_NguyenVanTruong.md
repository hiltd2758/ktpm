# Integration Test Report — Sprint 3
**Tác giả:** Nguyễn Văn Trường  
**Jira Task:** EHC-25  
**Sprint:** Sprint 3 — Integration Test  
**Project:** E-HealthCare System  
**Ngày thực hiện:** 15/06/2026  

---

## 1. Tổng quan

| Thông tin | Chi tiết |
|---|---|
| **Họ tên** | Nguyễn Văn Trường |
| **Task phụ trách** | EHC-25 |
| **Loại kiểm thử** | Integration Test |
| **Phương pháp** | `@SpringBootTest` + `TestRestTemplate` + H2 In-Memory DB |
| **Công cụ** | JUnit 5, Spring Boot Test, H2, BCryptPasswordEncoder |
| **Database** | H2 In-Memory (`jdbc:h2:mem:testdb`) |
| **Tổng test cases** | 7 |
| **Passed** | ✅ 7 |
| **Failed** | ❌ 0 |
| **Thời gian chạy** | 19.216s |

---

## 2. Phạm vi kiểm thử

| Controller | Endpoint | Test Cases |
|---|---|---|
| `AdminApiController` | `GET /api/admin/dashboard` | 2 |
| `AdminApiController` | `GET /api/admin/statistics` | 1 |
| `AdminApiController` | `PUT /api/admin/doctor/update/info` | 1 |
| `AdminApiController` | `PUT /api/admin/doctor/update/password/{id}` | 1 |
| `AdminApiController` | `PUT /api/admin/patient/update/info` | 1 |
| `AdminApiController` | `DELETE /api/admin/patient/delete/{id}` | 1 |
| | **Tổng** | **7** |

---

## 3. Kiến trúc Integration Test

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────┐
│     Spring Security Filter Chain    │  ← JWT Bearer Token validation
│         (AdminJwtFilter)            │     Authorization: Bearer <token>
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│         REST Controller             │  ← AdminApiController
│                                     │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│            Service Layer            │  ← AdminService
│                                     │     DoctorManagementService
│                                     │     PatientManagementService
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│         H2 In-Memory Database       │  ← test profile
└─────────────────────────────────────┘
```

---

## 4. Luồng nghiệp vụ kiểm thử

### Luồng 1 — Admin xem Dashboard & Thống kê
```
Admin login  →  lấy JWT token từ response body
     │
     ▼
GET /api/admin/dashboard
với Header: Authorization: Bearer {token}
     │
     ▼
Kiểm tra HTTP 200 + response body chứa keys: doctors, patients

GET /api/admin/statistics
với Header: Authorization: Bearer {token}
     │
     ▼
Kiểm tra HTTP 200 + keys: totalDoctors, totalPatients, totalAppointments
```

### Luồng 2 — Admin quản lý Doctor
```
Admin login  →  lấy JWT token từ response body
     │
     ▼
PUT /api/admin/doctor/update/info       →  cập nhật thông tin Doctor
PUT /api/admin/doctor/update/password/{id}  →  đổi mật khẩu Doctor
với Header: Authorization: Bearer {token}
     │
     ▼
Kiểm tra HTTP 200 + response body chứa key: message
```

### Luồng 3 — Admin quản lý Patient
```
Admin login  →  lấy JWT token từ response body
     │
     ▼
PUT /api/admin/patient/update/info      →  cập nhật thông tin Patient
DELETE /api/admin/patient/delete/{id}   →  xóa Patient
với Header: Authorization: Bearer {token}
     │
     ▼
Kiểm tra HTTP 200 + xác nhận xóa khỏi DB (findById → empty)
```

---

## 5. Seed Data

### Admin (tạo trong `@BeforeEach`):
| Field | Giá trị |
|---|---|
| email | `it_admin@test.com` |
| password | `Admin@123` (BCrypt encoded) |

### Doctor (tạo trong `@BeforeEach`):
| Field | Giá trị |
|---|---|
| email | `it_mgmt_doctor@test.com` |
| password | `Doctor@123` (BCrypt encoded) |
| firstName | `Mgmt` |
| lastName | `Doctor` |
| field | `Cardiology` |
| phone | `0901111111` |
| address | `123 Test Street` |
| ROLE | `ROLE_DOCTOR` |

### Patient (tạo trong `@BeforeEach`):
| Field | Giá trị |
|---|---|
| email | `it_mgmt_patient@test.com` |
| password | `Patient@123` (BCrypt encoded) |
| firstName | `Mgmt` |
| lastName | `Patient` |
| phone | `0902222222` |
| address | `456 Test Avenue` |

---

## 6. Chi tiết Test Cases

### 6.1 AdminApiController — Dashboard & Statistics

| Test ID | Tên Test Case | Input | Expected | Type | Status |
|---|---|---|---|---|---|
| IT-EHC25-01 | getDashboard — authenticated | `GET /api/admin/dashboard` với Bearer token hợp lệ | HTTP 200 + keys `doctors`, `patients` | Happy Path | ✅ PASS |
| IT-EHC25-02 | getDashboard — no token | `GET /api/admin/dashboard` không có token | HTTP 401 hoặc 403 | Auth Check | ✅ PASS |
| IT-EHC25-03 | getStatistics — authenticated | `GET /api/admin/statistics` với Bearer token hợp lệ | HTTP 200 + keys `totalDoctors`, `totalPatients`, `totalAppointments` | Happy Path | ✅ PASS |

### 6.2 AdminApiController — Doctor Management

| Test ID | Tên Test Case | Input | Expected | Type | Status |
|---|---|---|---|---|---|
| IT-EHC25-04 | updateDoctorInfo — valid | `PUT /api/admin/doctor/update/info` với body hợp lệ (id, email, firstName, ...) | HTTP 200 + key `message` | Happy Path | ✅ PASS |
| IT-EHC25-05 | updateDoctorPassword — valid | `PUT /api/admin/doctor/update/password/{id}` với `{newPassword: "NewDoctor@456"}` | HTTP 200 + key `message` | Happy Path | ✅ PASS |

### 6.3 AdminApiController — Patient Management

| Test ID | Tên Test Case | Input | Expected | Type | Status |
|---|---|---|---|---|---|
| IT-EHC25-06 | updatePatientInfo — valid | `PUT /api/admin/patient/update/info` với body hợp lệ (id, email, firstName, ...) | HTTP 200 + key `message` | Happy Path | ✅ PASS |
| IT-EHC25-07 | deletePatient — found | `DELETE /api/admin/patient/delete/{id}` với patient tồn tại | HTTP 200 + key `message` + xác nhận xóa DB | Happy Path | ✅ PASS |

---

## 7. Kết quả chạy

```
[INFO] Running com.e_health_care.web.api.AdminApiControllerManagementIT

[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 13.81 s
       -- in com.e_health_care.web.api.AdminApiControllerManagementIT

[INFO] Results:
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  19.216 s
[INFO] Finished at: 2026-06-15T13:49:07+07:00
[INFO] ------------------------------------------------------------------------
```

---

## 8. Cấu trúc file test

```
src/test/java/com/e_health_care/web/
├── AbstractIntegrationTest.java                              # Base class (EHC-20)
│   ├── @SpringBootTest(RANDOM_PORT)
│   ├── @ActiveProfiles("test")
│   ├── TestRestTemplate
│   └── Helper methods: jsonHeaders(), bearerHeaders(), bearerJsonHeaders()
└── api/
    └── AdminApiControllerManagementIT.java                   # EHC-25 — 7 test cases
        ├── getDashboard_shouldReturn200_whenAdminAuthenticated
        ├── getDashboard_shouldReturn401_whenNoToken
        ├── getStatistics_shouldReturn200_whenAdminAuthenticated
        ├── updateDoctorInfo_shouldReturn200_whenValid
        ├── updateDoctorPassword_shouldReturn200_whenValid
        ├── updatePatientInfo_shouldReturn200_whenValid
        └── deletePatient_shouldReturn200_whenFound
```

---

*Report được tạo ngày 15/06/2026 — Nguyễn Văn Trường*
