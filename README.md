# E-Commerce Backend (Spring Boot 3)

Java 17 / Spring Boot 3.5.x backend cho web bán hàng, bao gồm auth JWT, quản lý sản phẩm/đơn hàng/voucher, email notification, payment VNPay, và các job định kỳ.

## Kiến trúc & Stack

- Java 17, Spring Boot 3.5.x
- Spring Security + JWT
- Spring Data JPA (PostgreSQL), Redis (rate-limit / blacklist token)
- Mail: JavaMailSender (SMTP, hỗ trợ SendGrid/SES)
- Scheduling: @EnableScheduling (cron birthday, voucher expiring)
- OpenAPI (springdoc)

## Chức năng chính

- **Auth & User**
  - Đăng ký, đăng nhập, refresh token, logout (blacklist).
  - Kích hoạt tài khoản qua email (JWT token), resend, đổi email (reset về PENDING + gửi activation).
  - Đổi mật khẩu, quên mật khẩu với OTP email.
  - Role/permission-based, endpoint `GET /users/me`.
  - Audit user create/activate, login rate-limit Redis.
- **Sản phẩm & Gallery**
  - CRUD sản phẩm, lịch sử giá.
  - Gallery đa ảnh, trả về id/url, update từng ảnh, delete 1 ảnh hoặc toàn bộ.
  - Search/filter + soft delete/restore.
- **Đơn hàng & Kho**
  - Checkout (từ list hoặc giỏ), giảm tồn kho, shipment nội bộ.
  - Thông báo email ORDER_PLACED (kèm shipping info).
  - Cảnh báo low-stock (admin/inventory) cả khi adjust stock và khi checkout.
  - Inventory audit log (xuất Excel/PDF).
- **Voucher / Promotion**
  - Percentage, fixed, freeship (cap), điều kiện min order, max usage, per-user, remaining.
  - Activate/deactivate, apply API, auto expiring cron, notification voucher expiring/activated.
- **Mail & Notification**
  - HTML template sáng, CTA; activation (kèm mật khẩu tạm cho user do admin tạo), OTP reset, birthday, low-stock, order placed, voucher events.
  - Notification service (email) với các type chính; in-app placeholder.
- **Payment**
  - VNPay create URL + return handler.
  - Payment transaction log (`payment_transaction_logs`) và endpoint list logs (`GET /payments/logs`, quyền ORDER_MANAGE).
- **Scheduler**
  - Birthday email hằng ngày.
  - Voucher expiring reminder cron.

## Cấu hình môi trường

Thiết lập qua `application.yaml` + biến môi trường:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=yourpass

SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

APP_JWT_SECRET=your_jwt_secret
APP_ACTIVATION_JWT_SECRET=activation_secret_32chars
APP_ACTIVATION_BASE_URL=https://your-frontend.com/activate

APP_MAIL_FROM=no-reply@yourdomain.com
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=you@gmail.com
SPRING_MAIL_PASSWORD=app_password

APP_PASSWORD_RESET_TTL_MINUTES=10
APP_ACTIVATION_TTL_HOURS=24
APP_INVENTORY_LOW_STOCK_THRESHOLD=5

APP_SCHEDULER_BIRTHDAY_ENABLED=true
APP_SCHEDULER_BIRTHDAY_CRON=0 0 6 * * ?
APP_SCHEDULER_VOUCHER_EXPIRING_ENABLED=true
APP_SCHEDULER_VOUCHER_EXPIRING_CRON=0 0 7 * * ?
APP_SCHEDULER_VOUCHER_EXPIRING_DAYS_BEFORE=3
```

## Chạy dự án

```
mvn clean spring-boot:run
```

API base path: `/api` (server.servlet.context-path).

## Endpoint tham khảo (một phần)

- Auth: `/auth/register`, `/auth/login`, `/auth/activate`, `/auth/activate/resend`, `/auth/change-email`, `/auth/change-password`, `/auth/forgot-password`, `/auth/reset-password`.
- User: `/users/me`, CRUD `/users`.
- Products: CRUD `/products`, gallery `/products/{id}/gallery` (POST upload, PUT update one, DELETE one/all), `/products/{id}/price-history`.
- Orders: `/orders/checkout`, `/orders/checkout/cart`, `/orders/my`, `/orders/{id}`; admin `/orders/admin`.
- Inventory: `/inventory/adjust-stock`, audit log export.
- Voucher: CRUD `/vouchers`, apply `/vouchers/apply`.
- Payments: VNPay `/payments/vnpay/create`, `/payments/vnpay/return`; logs `/payments/logs`.

## Ghi chú bảo mật

- Đặt secret đủ mạnh (JWT, activation).
- Sử dụng app password/SMTP provider an toàn (SendGrid/SES) cho mail.
- Cân nhắc cấu hình HTTPS, CORS cho FE domain.

## Hướng phát triển tiếp

- In-app notification + WebSocket/SSE.
- Tích hợp GHN/GHTK/MoMo/ZaloPay/PayOS.
- Audit log toàn diện (order/voucher CRUD), dashboard thống kê.
- Voucher theo danh mục/sản phẩm, stacking rule, recommendation.
- Spring AI, chat bot.
- Chat real time với WebSocket.
