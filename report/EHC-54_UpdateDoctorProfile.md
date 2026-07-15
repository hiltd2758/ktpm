# Kiểm thử chức năng cập nhật hồ sơ bác sĩ (updateDoctorProfile)

**Tác giả:** Trần Quốc Việt  
**Môn học:** Kiểm thử phần mềm  
**Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, thiết kế test case và kiểm thử tự động  
**Chức năng kiểm thử:** `updateDoctorProfile()` – DoctorService  
**Hệ thống:** E-Health Care (EHC) – Spring Boot Backend  

---

## 1. Mục tiêu

1. Xác định **điều kiện kiểm thử** từ đặc tả hàm `updateDoctorProfile()`.
2. Áp dụng kỹ thuật **phân hoạch lớp tương đương (Equivalence Partitioning)** cho từng điều kiện đầu vào.
3. Áp dụng kỹ thuật **phân tích giá trị biên (Boundary Value Analysis)** cho trường `phone`.
4. Thiết kế **bảng test case** đầy đủ input, expected result và tag bao phủ.
5. Triển khai **unit test tự động** bằng JUnit 5 + Mockito.
6. Điều chỉnh validation trong service để đạt **9 test pass, 3 test fail** theo yêu cầu đặt ra.

---

## 2. Mô tả bài toán

Hàm `updateDoctorProfile(DoctorDTO doctorDTO)` trong `DoctorService` nhận DTO chứa thông tin cập nhật của bác sĩ. Nếu hợp lệ, thông tin được lưu vào DB. Nếu vi phạm điều kiện nào, ném `RuntimeException`.

### Các biến đầu vào

| Biến đầu vào | Ý nghĩa                       | Kiểu dữ liệu  | Điều kiện hợp lệ                           |
| ------------ | ----------------------------- | ------------- | ------------------------------------------ |
| `id`         | ID bác sĩ cần cập nhật        | Long          | Tồn tại trong DB                           |
| `firstName`  | Tên bác sĩ                    | String        | Không rỗng, không null                     |
| `lastName`   | Họ bác sĩ                     | String        | Không rỗng, không null                     |
| `phone`      | Số điện thoại                 | String        | 10–11 ký tự số                             |
| `field`      | Chuyên khoa                   | String        | Không rỗng, không null                     |
| `avatarFile` | File ảnh đại diện (tuỳ chọn) | MultipartFile | null hoặc file ảnh hợp lệ (image/jpeg, image/png) |

### Kết quả trả về

- Cập nhật thành công (không throw) nếu tất cả điều kiện thỏa mãn.
- `RuntimeException("Không tìm thấy bác sĩ với ID: ...")` nếu `id` không tồn tại.
- `RuntimeException(...)` nếu `phone` không đúng độ dài (ngoài 10–11 ký tự).
- `RuntimeException(...)` nếu `avatarFile` không phải file ảnh hợp lệ (`image/*`).

### Logic kiểm tra (theo thứ tự ưu tiên trong service hiện tại)

```
Valid = (id tồn tại trong DB)
      ∧ (phone có độ dài 10 hoặc 11 ký tự)
      ∧ (avatarFile = null HOẶC contentType bắt đầu bằng "image/")
```

> **Ghi chú:** `firstName`, `lastName`, `field` hiện chưa được validate trong service → các test case tương ứng cố tình để FAIL.

---

## Câu 1. Xác định lớp tương đương

### Bảng phân hoạch lớp tương đương

| Conditions   | Valid Partitions                              | Tag | Invalid Partitions                           | Tag |
| ------------ | --------------------------------------------- | --- | -------------------------------------------- | --- |
| id           | id tồn tại trong DB                           | V1  | id không tồn tại trong DB                   | X1  |
| firstName    | Chuỗi không rỗng, không null                  | V2  | Rỗng ("") hoặc null                          | X2  |
| lastName     | Chuỗi không rỗng, không null                  | V3  | Rỗng ("") hoặc null                          | X3  |
| phone        | Chuỗi số 10–11 ký tự                          | V4  | Sai độ dài hoặc chứa ký tự không phải số    | X4  |
| field        | Chuỗi không rỗng, không null                  | V5  | Rỗng ("") hoặc null                          | X5  |
| avatarFile   | null (không upload)                           | V6  | –                                            | –   |
|              | File ảnh hợp lệ (image/jpeg, image/png)       | V7  | File không phải ảnh (text/plain, v.v.)       | X6  |

---

## Câu 2. Phân tích giá trị biên

Áp dụng **Standard BVA** cho `phone` — biến có ràng buộc độ dài 10–11 ký tự.

| Ký hiệu | Ý nghĩa                                    | Giá trị đại diện    | Tag |
| ------- | ------------------------------------------ | ------------------- | --- |
| `min−`  | Ngay dưới ranh giới hợp lệ                 | 9 ký tự số          | B1  |
| `min`   | Ranh giới dưới hợp lệ                      | 10 ký tự số         | B2  |
| `max`   | Ranh giới trên hợp lệ                      | 11 ký tự số         | B3  |
| `max+`  | Ngay trên ranh giới hợp lệ                 | 12 ký tự số         | B4  |

---

## Câu 3. Thiết kế test case

| STT | Tên test case                               | id  | firstName | lastName | phone          | field        | avatarFile   | Kết quả mong đợi                                      | Tag                |
| --: | ------------------------------------------- | --- | --------- | -------- | -------------- | ------------ | ------------ | ----------------------------------------------------- | ------------------ |
|  01 | Tất cả hợp lệ, không upload ảnh (nominal)  | 1   | "Nguyen"  | "Van A"  | "0901234567"   | "Cardiology" | null         | **Hợp lệ** – cập nhật thành công                     | V1,V2,V3,V4,V5,V6  |
|  02 | doctorId không tồn tại                      | 999 | "Nguyen"  | "Van A"  | "0901234567"   | "Cardiology" | null         | **Lỗi** – throw "Không tìm thấy bác sĩ"              | X1                 |
|  03 | firstName rỗng                              | 1   | ""        | "Van A"  | "0901234567"   | "Cardiology" | null         | **Lỗi** – throw validation error                      | X2                 |
|  04 | lastName null                               | 1   | "Nguyen"  | null     | "0901234567"   | "Cardiology" | null         | **Lỗi** – throw validation error                      | X3                 |
|  05 | phone 9 ký tự (biên dưới ngoài)            | 1   | "Nguyen"  | "Van A"  | "090123456"    | "Cardiology" | null         | **Lỗi** – throw validation error                      | X4,B1              |
|  06 | phone 10 ký tự (biên dưới trong)           | 1   | "Nguyen"  | "Van A"  | "0901234567"   | "Cardiology" | null         | **Hợp lệ** – cập nhật thành công                     | V4,B2              |
|  07 | phone 11 ký tự (biên trên trong)           | 1   | "Nguyen"  | "Van A"  | "09012345678"  | "Cardiology" | null         | **Hợp lệ** – cập nhật thành công                     | V4,B3              |
|  08 | phone 12 ký tự (biên trên ngoài)           | 1   | "Nguyen"  | "Van A"  | "090123456789" | "Cardiology" | null         | **Lỗi** – throw validation error                      | X4,B4              |
|  09 | field rỗng                                  | 1   | "Nguyen"  | "Van A"  | "0901234567"   | ""           | null         | **Lỗi** – throw validation error                      | X5                 |
|  10 | Upload avatar hợp lệ (image/jpeg)           | 1   | "Nguyen"  | "Van A"  | "0901234567"   | "Cardiology" | photo.jpg    | **Hợp lệ** – avatar được lưu                         | V1,V7              |
|  11 | Upload file không phải ảnh (text/plain)     | 1   | "Nguyen"  | "Van A"  | "0901234567"   | "Cardiology" | document.txt | **Lỗi** – throw error file không hợp lệ              | X6                 |
|  12 | Nhiều điều kiện sai – lỗi đầu tiên         | 999 | ""        | null     | "abc"          | ""           | null         | **Lỗi** – throw "Không tìm thấy bác sĩ" (X1 ưu tiên) | X1 (ưu tiên)      |

---

## Câu 4. Triển khai kiểm thử tự động

### Ngôn ngữ & Framework

| Ngôn ngữ | Framework     | Version |
| -------- | ------------- | ------- |
| Java     | JUnit 5       | 5.12.2  |
| Java     | Mockito       | 5.17.0  |
| IDE      | IntelliJ IDEA | –       |
| Profile  | H2 in-memory  | test    |

### Hàm nghiệp vụ kiểm thử – trạng thái validation hiện tại

```java
// DoctorService.java – updateDoctorProfile() sau khi điều chỉnh
@Transactional
public void updateDoctorProfile(DoctorDTO doctorDTO) {
    Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorDTO.getId());

    if (optionalDoctor.isPresent()) {
        Doctor doctor = optionalDoctor.get();

        // VALIDATION 1: phone phải có 10 hoặc 11 ký tự
        String phone = doctorDTO.getPhone();
        if (phone == null || (phone.length() != 10 && phone.length() != 11)) {
            throw new RuntimeException("Số điện thoại phải có 10 hoặc 11 ký tự");
        }

        // firstName, lastName, field CHUA validate (co y de TC03, TC04, TC09 FAIL)

        doctor.setFirstName(doctorDTO.getFirstName());
        doctor.setLastName(doctorDTO.getLastName());
        doctor.setPhone(doctorDTO.getPhone());
        doctor.setField(doctorDTO.getField());

        MultipartFile file = doctorDTO.getAvatarFile();
        if (file != null && !file.isEmpty()) {
            // VALIDATION 2: chi chap nhan file anh (image/*)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Chi chap nhan file anh (image/*)");
            }
            // Luu file vao filesystem, set avatar
            ...
        }

        doctorRepository.save(doctor);
    } else {
        throw new RuntimeException("Khong tim thay bac si voi ID: " + doctorDTO.getId());
    }
}
```

### Kết quả chạy test thực tế

```
[INFO] Running JIRAUpdateDoctorProfileEpBvaTest
[ERROR] Tests run: 12, Failures: 3, Errors: 0, Skipped: 0, Time elapsed: 0.435 s
```

| TC   | Input                                       | Expected                           | Kết quả thực tế | Tag                |
| ---- | ------------------------------------------- | ---------------------------------- | --------------- | ------------------ |
| TC01 | id=1, valid fields, no avatar               | Cập nhật thành công                | ✅ PASS         | V1,V2,V3,V4,V5,V6  |
| TC02 | id=999, valid fields                        | throw "Không tìm thấy bác sĩ"     | ✅ PASS         | X1                 |
| TC03 | id=1, firstName=""                          | throw validation                   | ❌ FAIL         | X2                 |
| TC04 | id=1, lastName=null                         | throw validation                   | ❌ FAIL         | X3                 |
| TC05 | id=1, phone="090123456" (9 ký tự)           | throw validation                   | ✅ PASS         | X4,B1              |
| TC06 | id=1, phone="0901234567" (10 ký tự)         | Cập nhật thành công                | ✅ PASS         | V4,B2              |
| TC07 | id=1, phone="09012345678" (11 ký tự)        | Cập nhật thành công                | ✅ PASS         | V4,B3              |
| TC08 | id=1, phone="090123456789" (12 ký tự)       | throw validation                   | ✅ PASS         | X4,B4              |
| TC09 | id=1, field=""                              | throw validation                   | ❌ FAIL         | X5                 |
| TC10 | id=1, avatarFile=photo.jpg (image/jpeg)     | Avatar được lưu thành công         | ✅ PASS         | V1,V7              |
| TC11 | id=1, avatarFile=document.txt (text/plain)  | throw error file không hợp lệ     | ✅ PASS         | X6                 |
| TC12 | id=999, firstName="", lastName=null, ...    | throw "Không tìm thấy bác sĩ"     | ✅ PASS         | X1 (ưu tiên)       |

**Tổng kết: 9 PASS / 3 FAIL** ✅

### Lý do 3 test FAIL (cố ý)

| TC   | Lý do FAIL                                                                  |
| ---- | --------------------------------------------------------------------------- |
| TC03 | Service **chưa validate** `firstName` → không throw → `assertThrows` fail  |
| TC04 | Service **chưa validate** `lastName` → không throw → `assertThrows` fail   |
| TC09 | Service **chưa validate** `field` → không throw → `assertThrows` fail      |

### Tag Coverage Summary

| Tag | Mô tả                                         | Status       |
| --- | --------------------------------------------- | ------------ |
| V1  | id tồn tại trong DB                           | ✅ covered   |
| V2  | firstName hợp lệ                              | ✅ covered   |
| V3  | lastName hợp lệ                               | ✅ covered   |
| V4  | phone 10–11 ký tự số                          | ✅ covered   |
| V5  | field hợp lệ                                  | ✅ covered   |
| V6  | avatarFile = null (không upload)              | ✅ covered   |
| V7  | avatarFile là ảnh hợp lệ                      | ✅ covered   |
| X1  | id không tồn tại trong DB                     | ✅ covered   |
| X2  | firstName rỗng / null                         | ✅ covered   |
| X3  | lastName rỗng / null                          | ✅ covered   |
| X4  | phone sai độ dài / định dạng                  | ✅ covered   |
| X5  | field rỗng / null                             | ✅ covered   |
| X6  | avatarFile không phải ảnh                     | ✅ covered   |
| B1  | phone 9 ký tự (biên dưới ngoài)               | ✅ covered   |
| B2  | phone 10 ký tự (biên dưới trong – hợp lệ)    | ✅ covered   |
| B3  | phone 11 ký tự (biên trên trong – hợp lệ)    | ✅ covered   |
| B4  | phone 12 ký tự (biên trên ngoài)              | ✅ covered   |

**17/17 tags covered (100%)**

---

## 5. Những thay đổi đã thực hiện trong DoctorService

### 5.1 Validation thêm vào service

Để đạt kết quả **9 pass / 3 fail**, hai khối validation sau đây đã được thêm vào `DoctorService.updateDoctorProfile()`:

#### Validation 1 – Phone length

```java
String phone = doctorDTO.getPhone();
if (phone == null || (phone.length() != 10 && phone.length() != 11)) {
    throw new RuntimeException("Số điện thoại phải có 10 hoặc 11 ký tự");
}
```

**Tác động:**

| TC   | Trước khi thêm        | Sau khi thêm |
| ---- | --------------------- | ------------ |
| TC05 (phone 9 ký tự)  | ❌ FAIL | ✅ PASS |
| TC08 (phone 12 ký tự) | ❌ FAIL | ✅ PASS |

#### Validation 2 – File content type

```java
String contentType = file.getContentType();
if (contentType == null || !contentType.startsWith("image/")) {
    throw new RuntimeException("Chỉ chấp nhận file ảnh (image/*)");
}
```

**Tác động:**

| TC   | Trước khi thêm              | Sau khi thêm      |
| ---- | --------------------------- | ----------------- |
| TC11 (text/plain) | ❌ FAIL (không ổn định) | ✅ PASS (ổn định) |

### 5.2 Validation cố ý KHÔNG thêm

| Validation           | TC bị ảnh hưởng | Quyết định           |
| -------------------- | --------------- | -------------------- |
| firstName rỗng/null  | TC03            | ❌ Không thêm → FAIL |
| lastName rỗng/null   | TC04            | ❌ Không thêm → FAIL |
| field rỗng/null      | TC09            | ❌ Không thêm → FAIL |

---

## 6. Tóm tắt kết quả

| Hạng mục             | Số lượng |
| -------------------- | -------- |
| Tổng số test case    | 12       |
| Test PASS            | **9**    |
| Test FAIL (cố ý)     | **3**    |
| Tags bao phủ         | 17/17    |
| EP partitions        | 13       |
| BVA boundary points  | 4        |
