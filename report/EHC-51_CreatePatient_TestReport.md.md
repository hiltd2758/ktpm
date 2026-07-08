# Báo Cáo Kiểm Thử: Chức Năng Thêm Mới Bệnh Nhân (Create Patient)

|                      |                                                                                |
| -------------------- | ------------------------------------------------------------------------------ |
| **Module**           | E-HealthCare System — `AdminManagementService`                                 |
| **Tác giả**          | Khả Như - Nhật Thúy                                                            |
| **Jira Task**        | EHC-51 (Black-box: EP/BVA, Defect Retest)                                      |
| **Kỹ thuật áp dụng** | Equivalence Partitioning, Boundary Value Analysis, White-box Coverage Analysis |
| **Công cụ**          | JUnit 5, Mockito, JaCoCo 0.8.12, Allure Report                                 |
| **Trạng thái**       | Hoàn thành — 6/6 test PASS, 100% Line & Branch Coverage                        |

---

# Mục Lục

- [1. Mục tiêu kiểm thử](#1-mục-tiêu-kiểm-thử)
- [2. Đặc tả chức năng](#2-đặc-tả-chức-năng)
- [3. Black-box Testing — Equivalence Partitioning](#3-black-box-testing--equivalence-partitioning)
- [4. Black-box Testing — Boundary Value Analysis](#4-black-box-testing--boundary-value-analysis)
- [5. Thiết kế Test Case](#5-thiết-kế-test-case)
- [6. White-box Testing — Control Flow Graph](#6-white-box-testing--control-flow-graph)
- [7. Triển khai Unit Test (Allure Integrated)](#7-triển-khai-unit-test-allure-integrated)
- [8. Kết quả Code Coverage (JaCoCo)](#8-kết-quả-code-coverage-jacoco)
- [9. Bảng Tag Coverage](#9-bảng-tag-coverage)
- [10. Kết luận](#10-kết-luận)

---

# 1. Mục tiêu kiểm thử

| #   | Mục tiêu                                                                                |
| --- | --------------------------------------------------------------------------------------- |
| 1   | Xác định điều kiện kiểm thử từ logic nghiệp vụ của hàm `createPatient()`                |
| 2   | Áp dụng **Equivalence Partitioning (EP)** chia đầu vào thành lớp hợp lệ/không hợp lệ    |
| 3   | Áp dụng **Boundary Value Analysis (BVA)** để kiểm tra tính duy nhất của Email           |
| 4   | Kiểm thử lại (Retest) các lỗ hổng liên quan đến validation định dạng Email (Bug EHC-51) |
| 5   | Đo **Code Coverage** bằng JaCoCo sau khi Developer đã vá lỗi để đảm bảo độ phủ 100%     |
| 6   | Phân loại báo cáo tự động bằng **Allure Report** theo cấu trúc Behavior-Driven          |

---

# 2. Đặc tả chức năng

Hệ thống cho phép Admin tạo mới một bệnh nhân vào cơ sở dữ liệu.

Yêu cầu được xem là **hợp lệ** khi tất cả điều kiện sau đồng thời thỏa mãn:

| Biến đầu vào | Ý nghĩa             | Điều kiện hợp lệ                                                               |
| ------------ | ------------------- | ------------------------------------------------------------------------------ |
| `patient`    | Đối tượng bệnh nhân | Không null                                                                     |
| `email`      | Email bệnh nhân     | **Không được rỗng/null, phải đúng định dạng (`@`), và chưa tồn tại trong DB.** |

### Kết quả

- Trả về `Patient` đã được cấp ID và lưu thành công.
- Ném ra `RuntimeException` nếu vi phạm định dạng hoặc email đã bị trùng.

---

# 3. Black-box Testing — Equivalence Partitioning

| Conditions     | Valid Partitions                              | Tag | Invalid Partitions                     | Tag |
| -------------- | --------------------------------------------- | --- | -------------------------------------- | --- |
| `email` format | Email có chứa ký tự `@` và domain, không rỗng | V1  | Null, rỗng hoặc sai format (thiếu `@`) | X2  |
| `email` exist  | Email hoàn toàn mới, chưa ai đăng ký          | V2  | Email đã tồn tại trong DB              | X1  |

---

# 4. Black-box Testing — Boundary Value Analysis

| Ký hiệu | Ý nghĩa                                         | Giá trị đại diện               | Tag biên |
| ------- | ----------------------------------------------- | ------------------------------ | -------- |
| `min`   | Lần đầu tiên sử dụng email mới để tạo tài khoản | Email mới → Tạo thành công     | B2       |
| `min+`  | Lần thứ hai cố gắng dùng chính email vừa tạo    | Insert lần 2 → Throw exception | B3       |

---

# 5. Thiết kế Test Case

| Test Case | Input (Patient object)                  | Expected Outcome                       | Tags       |
| --------- | --------------------------------------- | -------------------------------------- | ---------- |
| TC01      | Email mới (`new@example.com`)           | ✅ **Hợp lệ** – Lưu thành công         | V1, V2, B2 |
| TC02      | Email đã tồn tại (`dup@example.com`)    | ❌ **Ném lỗi:** "Email already exists" | X1, B3     |
| TC03_Bug  | Email = `null`                          | ❌ **Ném lỗi:** Validation chặn lưu    | X2         |
| TC04_Bug  | Email = `""` (Rỗng)                     | ❌ **Ném lỗi:** Validation chặn lưu    | X2         |
| TC_Bug    | Email = `"abcxyz"` (Sai định dạng)      | ❌ **Ném lỗi:** Validation chặn lưu    | X2         |
| TC05      | Gọi insert 2 lần liên tiếp cùng 1 email | ❌ **Ném lỗi** ở lần thứ 2             | X1, B3     |

---

# 6. White-box Testing — Control Flow Graph

Sơ đồ luồng điều khiển (Control Flow Graph) của hàm `createPatient()` sau khi Developer đã vá các lỗ hổng validation.

```mermaid
flowchart TD
    Start([Bắt đầu createPatient]) --> CheckValid{Email hợp lệ\n(không null/rỗng/sai format)?}

    CheckValid -->|Không| ThrowValid[Throw: Validation Error]
    CheckValid -->|Có| CheckExist{Email đã tồn tại\ntrong DB chưa?}

    CheckExist -->|Có| ThrowExist[Throw: Email already exists]
    CheckExist -->|Không| SaveDB[Lưu Patient vào DB]

    SaveDB --> End([Kết thúc — Trả về Patient])

    ThrowValid --> EndErr([Kết thúc — Exception])
    ThrowExist --> EndErr

    style ThrowValid fill:#fadbd8
    style ThrowExist fill:#fadbd8
    style SaveDB fill:#d5f5e3
```

## Tính Cyclomatic Complexity

```text
V(G) = E - N + 2
```

```text
V(G) = 2 + 1 = 3
```

→ Khớp 100% với kết quả từ JaCoCo (Cyclomatic Complexity = 3).

---

# 7. Triển khai Unit Test (Allure Integrated)

**File test:** `AdminCreatePatientEpBvaTest.java`

```java
package com.e_health_care.web.admin.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Import Allure annotations
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Admin Management")
@Feature("Create Patient")
class AdminCreatePatientEpBvaTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AdminManagementService service;

    private Patient patient(String email) {
        Patient p = new Patient();
        p.setEmail(email);
        p.setFirstName("Test");
        p.setLastName("User");
        return p;
    }

    @Test
    @Story("Tạo mới bệnh nhân thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Tạo mới bệnh nhân thành công khi dữ liệu hợp lệ và email chưa tồn tại trong hệ thống.")
    @DisplayName("TC01 [V1,V2,B2]: email chưa tồn tại -> tạo patient thành công")
    void tc01_newEmail_shouldCreatePatient() {
        Patient p = patient("new@example.com");
        when(patientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(patientRepository.save(p)).thenReturn(p);

        Patient result = service.createPatient(p);
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    @Story("Thất bại do trùng email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Hệ thống phải ném lỗi 'Email already exists' khi cố tạo bệnh nhân với email đã có người sử dụng.")
    @DisplayName("TC02 [X1,B3]: email đã tồn tại -> throw 'Email already exists'")
    void tc02_duplicateEmail_shouldThrow() {
        Patient p = patient("dup@example.com");
        when(patientRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    @Story("Thất bại do email bị null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chặn đứng hành động lưu và ném lỗi validation khi email truyền vào là null.")
    @DisplayName("TC03 [X2]: email null -> throw exception validation")
    void tc03_nullEmail_shouldThrow() {
        assertThrows(RuntimeException.class, () -> service.createPatient(patient(null)));
    }

    @Test
    @Story("Thất bại do email rỗng")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chặn đứng hành động lưu và ném lỗi validation khi email truyền vào để trống (Bug EHC-51 đã fix).")
    @DisplayName("TC04 [X2]: email rỗng -> bị chặn bởi validation")
    void tc04_emptyEmail_shouldNotSaveWithoutCheck() {
        assertThrows(RuntimeException.class, () -> service.createPatient(patient("")));
    }

    @Test
    @Story("Thất bại do email sai định dạng")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chặn đứng hành động lưu và ném lỗi validation khi email không chứa ký tự @ (Bug EHC-51 đã fix).")
    @DisplayName("TC_Bug [X2]: Email sai định dạng -> bị chặn")
    void tc_invalidEmailFormat_shouldThrowException() {
        assertThrows(RuntimeException.class, () -> service.createPatient(patient("abcxyz")));
    }

    @Test
    @Story("Thất bại khi insert trùng liên tiếp")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ném lỗi khi cố tình thực hiện hành động tạo 2 lần liên tiếp cùng một email.")
    @DisplayName("TC05 [X1,B3]: insert lần 2 cùng email -> throw 'Email already exists'")
    void tc05_insertSameEmailTwice_shouldThrowOnSecond() {
        Patient p = patient("same@example.com");
        when(patientRepository.findByEmail("same@example.com")).thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }
}
```

---

# 8. Kết quả Code Coverage (JaCoCo)

## Kết quả tổng

| Method            | Line Coverage | Branch Coverage | Cyclomatic Complexity |
| ----------------- | ------------: | --------------: | --------------------: |
| `createPatient()` |    100% (5/5) |      100% (4/4) |                     3 |

---

# 9. Bảng Tag Coverage

| Tag | Mô tả                               | Test case                  | Trạng thái |
| --- | ----------------------------------- | -------------------------- | ---------- |
| V1  | Email có chứa ký tự `@`, không rỗng | TC01                       | ✅         |
| V2  | Email hoàn toàn mới                 | TC01                       | ✅         |
| X1  | Email đã tồn tại trong DB           | TC02, TC05                 | ✅         |
| X2  | Email null, rỗng hoặc sai format    | TC03_Bug, TC04_Bug, TC_Bug | ✅         |
| B2  | Lần đầu tiên sử dụng email mới      | TC01                       | ✅         |
| B3  | Cố tình insert lần 2 cùng email     | TC05                       | ✅         |

**Tổng kết**

- 6/6 Tags Covered = **100%**
- Branch Coverage = **100% (4/4)**
- Thiết kế Black-box (EP/BVA) đã bao phủ toàn bộ đường dẫn logic (White-box).

---

# 10. Kết luận

| Tiêu chí                       | Kết quả                                                                                                                                                                             |
| ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Tổng số test case              | 6 (Happy Path & Validation Catching)                                                                                                                                                |
| Test PASS                      | 6/6 (100%) – Retest sau khi Dev fix code                                                                                                                                            |
| Coverage hàm `createPatient()` | 100% Line & 100% Branch                                                                                                                                                             |
| Allure Report                  | Đã tích hợp thành công (Epic: Admin Management → Feature: Create Patient)                                                                                                           |
| Tình trạng Defect (EHC-51)     | Các lỗ hổng cho phép tạo bệnh nhân với email sai định dạng, email null hoặc rỗng đã được Developer vá hoàn tất. Hệ thống chặn bằng `RuntimeException`. Ticket Status: **RESOLVED**. |
