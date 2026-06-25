# Test Report — Sprint 2 (Bổ sung)
**Tác giả:** Nguyễn Văn Trường
**Jira Task:** EHC-31  
**Sprint:** Sprint 2 — Unit Test & BVA  
**Project:** E-HealthCare System  
**Ngày thực hiện:** 11/06/2026  

---

## 1. Tổng quan

| Thông tin | Chi tiết |
|---|---|
| **Họ tên** | Nguyễn Văn Trường |
| **Task phụ trách** | EHC-31 |
| **Phương pháp** | BVA (Boundary Value Analysis) |
| **Công cụ** | JUnit 5, Jakarta Validation, BvaValidationHelper |
| **Class kiểm thử** | `PatientCreateDTO` |
| **File test** | `PatientCreateDTOBvaTest.java` |
| **Tổng test cases (sau bổ sung)** | 21 |
| **Test cases bổ sung lần này** | 7 |
| **Passed** | ✅ 6 |
| **Failed** | ❌ 1 |
| **Thời gian chạy** | (chưa đo — cần build lại) |

---

## 2. Phạm vi kiểm thử

### EHC-31 — BVA PatientCreateDTO (bổ sung 4n+1)

| Class | Field | Loại | Test Cases |
|---|---|---|---|
| `PatientCreateDTO` | email | BVA | 3 (đã có) |
| `PatientCreateDTO` | password | BVA | 4 (đã có) + 1 (bổ sung) |
| `PatientCreateDTO` | firstName | BVA | 4 (đã có) |
| `PatientCreateDTO` | lastName | BVA | 4 (đã có, trùng với 4 case yêu cầu) |
| `PatientCreateDTO` | phone | BVA | 2 (đã có) + 1 (bổ sung) |
| `PatientCreateDTO` | all nominal | BVA | +1 (bổ sung) |
| | | **Tổng** | **21** |

---

## 3. Equivalence Partitioning — PatientCreateDTO

| Field | Valid Partitions | Tag | Invalid Partitions | Tag |
|---|---|---|---|---|
| `email` | đúng định dạng email | V1 | null | X1 |
| | | | thiếu `@` | X2 |
| `password` | 8 ≤ length ≤ 50 | V2 | length < 8 | X3 |
| | | | length > 50 | X4 |
| `firstName` | 1 ≤ length ≤ 50 | V3 | rỗng `""` | X5 |
| | | | length > 50 | X6 |
| `lastName` | 1 ≤ length ≤ 50 | V4 | rỗng `""` | X7 |
| | | | length > 50 | X8 |
| `phone` | length ≤ 10 | V5 | length > 10 | X9 |
| | | | null *(theo yêu cầu BVA)* | X10 |

---

## 4. Boundary Value Analysis — PatientCreateDTO

| Field | min-1 | min | nominal | max | max+1 | Tag |
|---|---|---|---|---|---|---|
| `email` | — | — | hợp lệ | — | thiếu `@` | E1 |
| `password` | 7 chars | 8 chars | 25 chars | 50 chars | 51 chars | P1–P5 |
| `firstName` | 0 chars | 1 char | — | 50 chars | 51 chars | F1–F4 |
| `lastName` | 0 chars | 1 char | — | 50 chars | 51 chars | L1–L4 |
| `phone` | — | — | 10 chars | — | 11 chars / null | PH1–PH3 |

> **Áp dụng 4n+1:** n = 5 field → cần 21 test cases. File hiện có 14, bổ sung thêm 7 → đủ **21** ✅

---

## 5. Chi tiết Test Cases bổ sung

### 5.1 PatientCreateDTOBvaTest — 7 test case bổ sung (4n+1)

| Test ID | Tên Test Case | Precondition | Input | Expected Result | Type | Tag | Status |
|---|---|---|---|---|---|---|---|
| BVA-EHC31-15 | allNominal_shouldBeValid | `validDto()` mặc định | tất cả field nominal | `assertTrue(isValid())` | N | — | ✅ PASS |
| BVA-EHC31-16 | lastName_empty_isInvalid | DTO hợp lệ | `lastName = ""` | `assertFalse(isValid())` | B | X7 | ✅ PASS |
| BVA-EHC31-17 | lastName_1char_isValid | DTO hợp lệ | `lastName = "A"` (1 ký tự) | `assertTrue(isValid())` | B | L2 | ✅ PASS |
| BVA-EHC31-18 | lastName_50chars_isValid | DTO hợp lệ | `lastName = "A"×50` | `assertTrue(isValid())` | B | L3 | ✅ PASS |
| BVA-EHC31-19 | lastName_51chars_isInvalid | DTO hợp lệ | `lastName = "A"×51` | `assertFalse(isValid())` | B | X8 | ✅ PASS |
| BVA-EHC31-20 | phone_null_isInvalid | DTO hợp lệ | `phone = null` | `assertFalse(isValid())` | B | X10 | ❌ FAIL |
| BVA-EHC31-21 | password_25chars_nominal_isValid | DTO hợp lệ | `password = "A"×25` | `assertTrue(isValid())` | N | — | ✅ PASS |

---

## 6. Sự cố phát hiện được — BVA-EHC31-20 (`phone_null_isInvalid`)

**Mô tả:** Test kỳ vọng `phone = null` → `isValid(dto) = false`, nhưng kết quả thực tế là `isValid(dto) = true` → test **FAIL**.

**Nguyên nhân:** Field `phone` trong `PatientCreateDTO` chỉ có:

```java
@Size(max = 10, message = "Phone max 10 digits")
private String phone;
```

Theo Jakarta Bean Validation spec, `@Size` **bỏ qua giá trị `null`** (coi là hợp lệ) trừ khi có thêm `@NotNull` hoặc `@NotBlank`.

**Đề xuất khắc phục (chọn 1):**
- **Cách A:** Thêm `@NotNull(message = "Phone is required")` vào field `phone` trong `PatientCreateDTO.java` → test sẽ pass đúng kỳ vọng.

---

## 7. Kết quả chạy (dự kiến sau khi thêm code)

```
[INFO] Running PatientCreateDTOBvaTest
[INFO] Tests run: 21, Failures: 1, Errors: 0, Skipped: 0
[INFO]
[ERROR] Tests run: 21, Failures: 1
[ERROR]   PatientCreateDTOBvaTest.phone_null_isInvalid:XXX
            phone = null phải không hợp lệ ==> expected: <false> but was: <true>
```

> Sau khi áp dụng **Cách A** (thêm `@NotNull` vào `phone`), kết quả mong đợi:

```
[INFO] Running PatientCreateDTOBvaTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 8. Cấu trúc file test

```
src/test/java/com/e_health_care/web/
├── BaseServiceTest.java                          # Base class 
├── BvaValidationHelper.java                      # Validation helper 
└── admin/dto/
    └── PatientCreateDTOBvaTest.java              # EHC-31 — BVA (21 test cases, bổ sung 7)
```

---

*Report được tạo ngày 11/06/2026 — Nguyễn Văn Trường*
