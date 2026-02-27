# Zyna E-Commerce Backend 🚀

Giải pháp Backend chuyên nghiệp, cấp độ doanh nghiệp cho nền tảng **Thương mại điện tử Thời trang**. Được xây dựng với **Spring Boot 3.5.7** và **Java 17**, hệ thống này cung cấp nền tảng mạnh mẽ, bảo mật và khả năng mở rộng cho trải nghiệm mua sắm trực tuyến hiện đại.

---

## 📖 Tổng Quan Dự Án

**Zyna E-Commerce** là hệ thống Backend RESTful API được thiết kế theo mô-đun để xử lý các nghiệp vụ phức tạp của ngành bán lẻ thời trang. Dự án tuân thủ kiến trúc phân tầng (layered architecture) với sự chú trọng đặc biệt vào bảo mật, khả năng mở rộng và tính bảo trì.

### Lĩnh Vực Kinh Doanh

Nền tảng được tối ưu riêng cho ngành thời trang, hỗ trợ sản phẩm đa biến thể (kích thước/màu sắc), kiểm kho định kỳ, hệ thống voucher linh hoạt, và quy trình vận hành đa vai trò.

### Kiến Trúc Cấp Cao

- **Modular Monolith**: Được thiết kế với ranh giới phân miền (sub-domain) rõ ràng (Đơn hàng, Sản phẩm, Vận chuyển, v.v.).
- **Xác thực Stateless**: Sử dụng JWT hoàn toàn với cơ chế quản lý token qua Redis.
- **Mô hình RBAC & Quyền hạn**: Kiểm soát truy cập phân quyền chi tiết bảo vệ mọi endpoint.
- **Thanh toán & Logistics**: Tích hợp cổng thanh toán VNPay và hệ thống theo dõi vận chuyển nội bộ.

### Đối Tượng Người Dùng

- **Khách hàng**: Xem sản phẩm, quản lý giỏ hàng, đặt hàng và theo dõi đơn giao.
- **Nhân viên (Staff)**: Quản lý dữ liệu sản phẩm, kho hàng và xử lý yêu cầu hỗ trợ.
- **Người giao hàng (Shipper)**: Xử lý lấy hàng, cập nhật trạng thái giao và quản lý tiền thu hộ (COD).
- **Quản trị viên (Admin)**: Kiểm soát toàn bộ hệ thống, quản lý vai trò/quyền hạn và phân tích kinh doanh.

---

## 🏗️ Kiến Trúc Hệ Thống

Dự án triển khai **Kiến trúc Phân tầng (Layered Architecture)** chuẩn mực để đảm bảo sự tách biệt giữa các thành phần và khả năng kiểm thử cao.

### Các Tầng Cốt Lõi

1.  **Tầng Controller**: Xử lý các yêu cầu HTTP, kiểm tra dữ liệu đầu vào và ánh xạ DTO sang model nội bộ.
2.  **Tầng Service**: Đóng gói logic nghiệp vụ, quản lý giao dịch (transaction) và chuyển đổi trạng thái.
3.  **Tầng Repository**: Trừu tượng hóa việc tương tác với cơ sở dữ liệu thông qua Spring Data JPA.
4.  **Tầng Security**: Chuỗi lọc (filter chain) tập trung để xác thực JWT và phân quyền.

### Luồng Dữ Liệu

```text
Client (Browser/Mobile) → [ JWT Security Filter ] → [ Controller ] → [ Service ] → [ Repository ] → [ Database ]
                                                         ↑              ↓
                                                 [ Redis Caching ] [ Mail Service ]
```

---

## 🛠️ Công Nghệ Sử Dụng

| Công nghệ           | Phiên bản | Mục đích                                      |
| :------------------ | :-------- | :-------------------------------------------- |
| **Java**            | 17+       | Ngôn ngữ lập trình chính.                     |
| **Spring Boot**     | 3.5.7     | Framework chính cho web, bảo mật và dữ liệu.  |
| **Spring Security** | 6.x       | Bảo mật RBAC và xác thực JWT.                 |
| **PostgreSQL**      | 16+       | Cơ sơ dữ liệu quan hệ chính.                  |
| **Redis**           | Latest    | Rate limiting, blacklist token và bộ nhớ đệm. |
| **JWT**             | 0.11.5    | Hỗ trợ xác thực không trạng thái (stateless). |
| **Lombok**          | 1.18.x    | Giảm thiểu mã boilerplate.                    |
| **Apache POI**      | 5.2.5     | Xuất báo cáo Excel chuyên nghiệp.             |
| **OpenPDF**         | 1.3.39    | Xuất báo cáo kiểm kho định dạng PDF.          |
| **VNPAY**           | 2.1.0     | Tích hợp cổng thanh toán trực tuyến.          |

---

## 🔐 Các Mô-đun Tính Năng

### Xác thực & Bảo mật

- **Đăng nhập bảo mật**: Cấp phát JWT với khóa bí mật entropy cao.
- **Chiến lược Refresh**: Xoay vòng token và blacklist qua Redis để đăng xuất an toàn.
- **RBAC**: Kiểm soát truy cập dựa trên vai trò tích hợp với `@PreAuthorize` của Spring Security.

### Quản lý Sản phẩm & Kho hàng

- **Kiểm soát biến thể**: Quản lý sản phẩm theo kích thước (size), thương hiệu và danh mục.
- **Theo dõi giá**: Tự động lưu vết lịch sử thay đổi giá để phân tích.
- **Kiểm kho (Audit)**: Dịch vụ chuyên dụng để điều chỉnh tồn kho với nhật ký kiểm toán bắt buộc (Có thể xuất ra PDF/Excel).

### Giỏ hàng & Quy trình Đơn hàng

- **Thanh toán Nguyên tử (Atomic)**: Kiểm tra tồn kho thời gian thực trong quá trình thanh toán.
- **Vòng đời đơn hàng**: Quản lý các trạng thái từ `PENDING` → `CONFIRMED` → `SHIPPING` → `DELIVERED` / `CANCELED`.
- **Tích hợp thanh toán**: Luồng VNPay đầy đủ với xử lý callback return-URL.

### Vận chuyển & Logistics

- **Phân phối**: Gán đơn hàng cho shipper cụ thể kèm mã vận đơn.
- **Logic Giao hàng**: Shipper cập nhật trạng thái `PICKED_UP`, `IN_DELIVERY`, `DELIVERED`, hoặc `FAILED`.
- **Tự động Hoàn hàng**: Quy trình hoàn hàng tích hợp yêu cầu phê duyệt từ Admin và shipper lấy hàng.

### Voucher & Marketing

- **Quy tắc Linh hoạt**: Giảm theo phần trăm (%), số tiền cố định, hoặc miễn phí vận chuyển.
- **Ràng buộc sử dụng**: Giá trị đơn hàng tối thiểu, giới hạn số lần dùng, và lịch trình hết hạn.
- **Tương tác**: Tự động thông báo email khi voucher sắp hết hạn hoặc mới được kích hoạt.

---

## 🛡️ Thiết Kế Bảo Mật & RBAC

### Phân Quyền Chi Tiết

Chúng tôi sử dụng hệ thống quyền hạn (permissions) tinh vi. Các Vai trò (Roles - Admin, Staff, v.v.) là tập hợp của các quyền cụ thể:

- `USER_READ`, `USER_WRITE`: Quản lý hồ sơ người dùng.
- `PRODUCT_READ`, `PRODUCT_WRITE`: Quản lý kho và dữ liệu sản phẩm.
- `ORDER_MANAGE`: Xử lý và xác nhận đơn hàng.
- `SHIPPING_MANAGE`: Quản lý shipper và vận chuyển.
- `SUPPORT_READ`, `SUPPORT_WRITE`: Xử lý yêu cầu hỗ trợ khách hàng.

### Quyền SYSADMIN

Vai trò **SYSADMIN** là vai trò đặc biệt được khởi tạo qua `PermissionSeeder`. Vai trò này tự động có toàn bộ quyền hạn trong hệ thống, đảm bảo khả năng quản trị tối cao để thiết lập và khôi phục hệ thống.

---

## 📂 Cấu Trúc Thư Mục

```text
com.zyna.dev.ecommerce
 ├── address/        # Quản lý địa chỉ giao hàng
 ├── authentication/ # Đăng ký, đăng nhập, mã kích hoạt và OTP
 ├── carts/          # Giỏ hàng và kiểm tra dữ liệu
 ├── common/         # Ngoại lệ toàn cục, enums và API wrapper
 ├── configs/        # Cấu hình hạ tầng (Redis, Mail, Security)
 ├── dashboard/      # Dịch vụ phân tích doanh thu và vận hành
 ├── inventory/      # Biến động kho và báo cáo kiểm toán
 ├── notifications/  # Gửi email và quản lý template
 ├── orders/         # Engine xử lý đơn hàng và máy trạng thái
 ├── payments/       # Cổng thanh toán VNPay và nhật ký giao dịch
 ├── products/       # Quản lý danh mục (Size, Brand, Category)
 ├── reviews/        # Đánh giá sau khi mua hàng và báo cáo
 ├── security/       # Provider JWT và các bộ lọc xác thực
 ├── shipping/       # Điều phối logistics và cổng thông tin shipper
 ├── support/        # Quản lý phiếu hỗ trợ (Helpdesk)
 ├── users/          # Quản lý hồ sơ và ánh xạ quyền hạn
 └── vouchers/       # Engine giảm giá và lập lịch hết hạn
```

---

## 🌍 Đa Ngôn Ngữ (i18n)

Zyna Backend đã được cấu hình với **toàn bộ thông báo bằng Tiếng Việt**. Mọi thông điệp `ApiResponse`, lỗi xác thực và ngoại lệ nghiệp vụ đều được trả về bằng Tiếng Việt chuyên nghiệp để đảm bảo trải nghiệm bản địa tốt nhất cho người dùng Việt Nam.

---

## 🚀 Cài Đặt & Thiết Lập

### Yêu cầu Tiên Quyết

- Java 17 (Khuyến nghị: Amazon Corretto hoặc Temurin)
- PostgreSQL 16
- Redis 7.x
- Maven 3.9

### 1. Cấu Hình Cơ Sở Dữ Liệu & Bảo Mật

Tạo file `.env` tại thư mục gốc của dự án:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/zyna_ecommerce
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

APP_JWT_SECRET=your_base64_secret_key_at_least_32_bytes
APP_ACTIVATION_JWT_SECRET=your_activation_secret_key

SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
```

### 2. Chạy Ứng Dụng

```bash
# Cài đặt các dependency và build dự án
mvn clean install

# Khởi chạy Spring Boot
mvn spring-boot:run
```

---

## 📑 Tổng Quan API (Tham khảo)

| Phương thức | Endpoint                        | Mô tả                  | Quyền truy cập |
| :---------- | :------------------------------ | :--------------------- | :------------- |
| **POST**    | `/api/auth/login`               | Xác thực và lấy JWT    | Công khai      |
| **GET**     | `/api/users/me`                 | Lấy hồ sơ hiện tại     | Đã đăng nhập   |
| **POST**    | `/api/orders/checkout`          | Xử lý đặt hàng         | USER           |
| **PATCH**   | `/api/shipments/{id}/delivered` | Đánh dấu đã giao hàng  | SHIPPER        |
| **GET**     | `/api/admin/dashboard/revenue`  | Xem thống kê doanh thu | ADMIN          |

---

_Phát triển bởi Zyna Dev Team. Giải pháp Backend Chuyên Nghiệp cho Thương Mại Hiện Đại._
