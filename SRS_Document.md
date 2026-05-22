# TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION - SRS)

**Dự án:** Hệ thống quản lý Y tế điện tử (E-Healthcare System)
**Phiên bản:** 1.0
**Ngày lập:** 21/05/2026

---

## 1. GIỚI THIỆU CHUNG (INTRODUCTION)

### 1.1 Mục đích (Purpose)
Tài liệu này cung cấp một bản đặc tả chi tiết các yêu cầu phần mềm cho hệ thống **E-Healthcare System**. Mục đích của tài liệu là định nghĩa rõ ràng các chức năng, giao diện, và các yêu cầu phi chức năng của hệ thống để làm cơ sở cho quá trình bảo trì, mở rộng và phát triển các tính năng mới trong tương lai

### 1.2 Phạm vi (Scope)
Hệ thống **E-Healthcare** là một nền tảng y tế điện tử cung cấp giải pháp kết nối trực tuyến giữa Bệnh nhân (Patient), Bác sĩ (Doctor), và Quản trị viên (Admin). 
Phần mềm cho phép:
- **Bệnh nhân:** Đăng ký tài khoản, quản lý hồ sơ y tế cá nhân, đặt lịch khám trực tuyến, và giao tiếp với bác sĩ.
- **Bác sĩ:** Quản lý hồ sơ công việc, theo dõi danh sách lịch hẹn từ bệnh nhân, xem hồ sơ bệnh án của bệnh nhân và hỗ trợ tư vấn.
- **Quản trị viên:** Quản lý tổng thể hệ thống, quản lý tài khoản bác sĩ, bệnh nhân, và giám sát các hoạt động trên nền tảng.
- **Tương tác thời gian thực:** Hỗ trợ tính năng chat trực tuyến (WebSocket) để tăng hiệu quả tương tác từ xa.

### 1.3 Định nghĩa và Viết tắt (Definitions and Acronyms)
- **SRS:** Software Requirements Specification (Đặc tả yêu cầu phần mềm).
- **Admin:** Quản trị viên hệ thống.
- **Patient:** Bệnh nhân/Người dùng cuối có nhu cầu khám chữa bệnh.
- **Doctor:** Bác sĩ/Chuyên gia y tế cung cấp dịch vụ khám chữa bệnh.
- **WebSocket:** Công nghệ truyền thông 2 chiều thời gian thực trên môi trường web.

---

## 2. MÔ TẢ TỔNG QUAN (OVERALL DESCRIPTION)

### 2.1 Cấu trúc hệ thống (Product Perspective)
Hệ thống E-Healthcare là một ứng dụng Web (Web Application) được xây dựng trên kiến trúc Multi-Tier:
- **Backend Core:** Phát triển bằng Java Spring Boot, áp dụng mô hình MVC (Model-View-Controller) cho việc xử lý logic nghiệp vụ và trả về Web giao diện.
- **Backend Real-time:** Sử dụng Node.js và thư viện WebSocket (ws) chạy song song với Backend Core để xử lý các luồng dữ liệu thời gian thực (Chat/Messaging).
- **Database:** Sử dụng hệ quản trị cơ sở dữ liệu quan hệ MySQL để lưu trữ dữ liệu bền vững, được kết nối với Backend Core thông qua Spring Data JPA / Hibernate.
- **Frontend:** Xây dựng bằng HTML, CSS, JavaScript thuần kết hợp cơ chế Template Engine (Thymeleaf/JSP) render giao diện từ phía server.

### 2.2 Đối tượng người dùng (User Classes and Characteristics)
Hệ thống phục vụ 3 nhóm người dùng chính, mỗi nhóm có quyền hạn và nghiệp vụ riêng biệt:
1. **Admin (Quản trị viên):** Những người có quyền lực cao nhất, quản lý danh mục dữ liệu, phê duyệt/khóa tài khoản, và xem thống kê hoạt động của toàn hệ thống.
2. **Doctor (Bác sĩ):** Những người có chuyên môn y tế. Cần có giao diện riêng để theo dõi lịch trình khám, quản lý hồ sơ chuyên môn và xem lịch sử khám bệnh của bệnh nhân.
3. **Patient (Bệnh nhân):** Người dùng phổ thông. Yêu cầu giao diện trực quan, dễ sử dụng để tìm kiếm bác sĩ, đặt lịch hẹn nhanh chóng và trao đổi thông tin.

### 2.3 Môi trường vận hành (Operating Environment)
- **Hệ điều hành máy chủ:** Windows/Linux.
- **Môi trường Backend:** Java 11/17 (Spring Boot), Node.js (cho WebSocket Server).
- **Trình duyệt Web hỗ trợ:** Chrome, Firefox, Safari, Edge (phiên bản mới nhất).
- **Cơ sở dữ liệu:** MySQL (Port: 3306).
- **Cổng Web App:** Port 8080.

---

## 3. ĐẶC TẢ YÊU CẦU CHỨC NĂNG (SYSTEM FEATURES)

Hệ thống được chia thành 4 module chính tương ứng với các nhóm nghiệp vụ.

### 3.1 Module Quản trị viên (Admin Module)
| Mã YC | Tên chức năng | Mô tả chi tiết |
|---|---|---|
| ADM-01 | **Đăng nhập quản trị** | Admin có thể đăng nhập vào hệ thống bằng tài khoản (Username/Password) được cấu hình bảo mật bằng JWT. |
| ADM-02 | **Bảng điều khiển (Dashboard)** | Xem tổng quan thống kê về số lượng bệnh nhân, bác sĩ, và số lượng cuộc hẹn trong hệ thống. |
| ADM-03 | **Quản lý danh sách Bác sĩ** | Admin có quyền xem danh sách các bác sĩ đang hoạt động, thêm, chỉnh sửa thông tin hoặc vô hiệu hóa tài khoản bác sĩ. |
| ADM-04 | **Quản lý danh sách Bệnh nhân** | Xem danh sách bệnh nhân trên hệ thống, kiểm tra thông tin và chỉnh sửa hoặc khóa tài khoản nếu cần. |

### 3.2 Module Bác sĩ (Doctor Module)
| Mã YC | Tên chức năng | Mô tả chi tiết |
|---|---|---|
| DOC-01 | **Đăng nhập Bác sĩ** | Bác sĩ truy cập qua cổng đăng nhập riêng biệt, xác thực bảo mật bằng JWT. |
| DOC-02 | **Quản lý Hồ sơ cá nhân (Profile)** | Bác sĩ có thể xem và cập nhật thông tin cá nhân, hình ảnh đại diện, thông tin chuyên môn, chuyên khoa, kinh nghiệm. |
| DOC-03 | **Theo dõi cuộc hẹn (Appointments)** | Xem danh sách các cuộc hẹn (Appointments) do bệnh nhân đã đặt. Cho phép bác sĩ quản lý trạng thái, chấp nhận hoặc hủy các yêu cầu khám chữa bệnh. |
| DOC-04 | **Xem bệnh án (Patient Records)** | Khi có cuộc hẹn, bác sĩ có thể truy cập và xem chi tiết hồ sơ bệnh lý (Clinical Information) của bệnh nhân để chuẩn bị tư vấn. |

### 3.3 Module Bệnh nhân (Patient Module)
| Mã YC | Tên chức năng | Mô tả chi tiết |
|---|---|---|
| PAT-01 | **Đăng ký tài khoản mới** | Người dùng phổ thông có thể đăng ký tài khoản bệnh nhân bằng cách điền thông tin cá nhân cơ bản. |
| PAT-02 | **Đăng nhập Bệnh nhân** | Đăng nhập hệ thống bằng tài khoản đã đăng ký (Bảo mật bằng JWT). |
| PAT-03 | **Quản lý Hồ sơ cá nhân (Profile)** | Bệnh nhân có thể cập nhật thông tin liên lạc, đổi mật khẩu và cập nhật hình đại diện cá nhân. |
| PAT-04 | **Thông tin lâm sàng (Clinical Info)** | Bệnh nhân tự quản lý và cập nhật thông tin về nhóm máu, tiền sử bệnh lý, dị ứng, chiều cao, cân nặng... |
| PAT-05 | **Đặt lịch khám (Book Appointment)** | Cho phép bệnh nhân chọn bác sĩ, chọn ngày/giờ khám phù hợp và xác nhận gửi yêu cầu cuộc hẹn. |
| PAT-06 | **Quản lý cuộc hẹn** | Xem lại danh sách các lịch sử khám, các cuộc hẹn sắp tới, và trạng thái của các cuộc hẹn (Chờ duyệt, Đã duyệt, Đã hoàn thành). |

### 3.4 Module Tương tác Thời gian thực (Chat/Messaging)
| Mã YC | Tên chức năng | Mô tả chi tiết |
|---|---|---|
| CHT-01 | **Nhắn tin trực tuyến (Websocket)** | Cung cấp giao diện chat trực tiếp để bệnh nhân có thể trao đổi, nhận tư vấn từ bác sĩ theo thời gian thực (real-time) thông qua module Node.js Websocket. |

---

## 4. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)

### 4.1 Bảo mật (Security)
- **Xác thực và Phân quyền:** Hệ thống sử dụng công nghệ **JSON Web Tokens (JWT)** để quản lý phiên đăng nhập và phân quyền truy cập. Các Endpoint được bảo vệ tương ứng với các Security Context riêng biệt cho 3 nhóm Role: Admin, Doctor, Patient.
- **Bảo mật mật khẩu:** Toàn bộ mật khẩu trong Database phải được băm (hashing) bảo mật, không được lưu dưới dạng plaintext.
- **Upload file an toàn:** Tài nguyên upload (hình ảnh đại diện, tài liệu) được lưu trữ vào thư mục cấu hình riêng (`static/img/avatars/`).

### 4.2 Hiệu suất (Performance)
- Hệ thống hỗ trợ xử lý nhiều Request đồng thời nhờ sức mạnh của hệ sinh thái Spring Boot.
- Phản hồi tác vụ thông thường (truy xuất DB) trong khoảng < 2 giây.
- Kênh nhắn tin Websocket đảm bảo độ trễ thấp (low latency) với lưu lượng lớn dữ liệu chat qua lại.

### 4.3 Khả năng bảo trì (Maintainability)
- Ứng dụng tuân thủ mô hình thiết kế **MVC** (Model-View-Controller).
- Cấu trúc thư mục được chia rạch ròi theo nghiệp vụ domain (`admin`, `doctor`, `patient`, `security`) giúp cho việc đọc hiểu code và duy trì trở nên thuận lợi.

---
