# EV Second-Hand Battery Trading Platform

Nền tảng mua bán pin và xe điện cũ với giao diện hiện đại và trải nghiệm người dùng tối ưu.

## 🎯 Tính năng

### Đã hoàn thành ✅
- ✅ Đăng nhập & Đăng ký tài khoản với validation
- ✅ Quản lý trạng thái authentication với StateFlow
- ✅ Trang chủ hiển thị pin & xe điện
- ✅ Lazy loading & Load more tự động
- ✅ Filter chỉ hiển thị sản phẩm AVAILABLE
- ✅ Bottom navigation động (3 tabs cho guest, 5 tabs cho user đã đăng nhập)
- ✅ Giao diện đẹp mắt, chuẩn mobile với Material Design 3

### Các trang
1. **Trang đăng nhập/đăng ký** - Authentication với validation đầy đủ
2. **Trang chủ** - Hiển thị pin và xe điện với lazy loading
3. **Sản phẩm** - Placeholder (sẽ phát triển sau)
4. **Đăng tin** - Placeholder cho tính năng đăng bán
5. **Ví** - Placeholder cho quản lý tài chính
6. **Hồ sơ** - Placeholder với nút đăng xuất

## 🏗️ Kiến trúc dự án

```
app/src/main/java/com/example/evsecondhand/
├── data/
│   ├── model/              # Data models (Battery, Vehicle, Auth)
│   ├── remote/             # API services & Retrofit setup
│   └── repository/         # Repositories (AuthRepository, ProductRepository)
├── ui/
│   ├── navigation/         # Navigation setup & routes
│   ├── screen/
│   │   ├── auth/          # Login & Register screens
│   │   ├── home/          # Home screen with products
│   │   └── ...            # Other screens
│   ├── theme/             # Material Design 3 theme
│   └── viewmodel/         # ViewModels với StateFlow
└── MainActivity.kt
```

## 🔧 Công nghệ sử dụng

- **Kotlin** - Ngôn ngữ lập trình chính
- **Jetpack Compose** - UI framework hiện đại
- **Material Design 3** - Design system
- **Retrofit** - HTTP client cho API calls
- **Kotlinx Serialization** - JSON parsing
- **Coil** - Image loading
- **StateFlow** - Quản lý state reactive
- **Navigation Compose** - Điều hướng giữa các màn hình
- **ViewModel** - Quản lý UI state
- **Coroutines** - Xử lý bất đồng bộ

## 📡 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - Đăng nhập
- `POST /api/v1/auth/register` - Đăng ký

### Products
- `GET /api/v1/batteries/` - Lấy danh sách pin (với pagination)
- `GET /api/v1/vehicles/` - Lấy danh sách xe điện (với pagination)

## 🎨 Thiết kế

### Màu sắc chủ đạo
- **Primary Green**: `#00C853` - Màu chủ đạo (xanh lá)
- **Accent Blue**: `#2196F3` - Màu phụ
- **Background**: `#F5F5F5` - Màu nền nhạt

### Bottom Navigation
- **Guest (3 tabs)**: Trang chủ, Sản phẩm, Hồ sơ
- **Authenticated (5 tabs)**: Trang chủ, Sản phẩm, Đăng tin (+), Ví, Hồ sơ

## 🚀 Cách chạy

1. Clone repository
2. Mở project trong Android Studio
3. Sync Gradle
4. Chạy app trên emulator hoặc thiết bị thật

## 📱 Tính năng đặc biệt

### Lazy Loading
- Tự động load thêm sản phẩm khi scroll đến cuối danh sách
- Hiển thị loading indicator khi đang tải
- Hỗ trợ pull-to-refresh

### Authentication
- Tự động lưu token và thông tin user
- Kiểm tra trạng thái đăng nhập khi khởi động app
- Tự động chuyển về login khi logout

### Filter
- Chỉ hiển thị sản phẩm có status "AVAILABLE"
- Filter được thực hiện ở repository layer

## 📝 Các tài khoản test

### Login
```json
{
  "email": "eva_okuneva@hotmail.com",
  "password": "password123"
}
```

### Register
```json
{
  "name": "Tên của bạn",
  "email": "email@example.com",
  "password": "12345678"
}
```

## 🔜 Phát triển tiếp theo

- [ ] Chi tiết sản phẩm
- [ ] Tìm kiếm & lọc nâng cao
- [ ] Quản lý sản phẩm của người dùng
- [ ] Tính năng đăng tin bán hàng
- [ ] Chat giữa người mua và người bán
- [ ] Quản lý ví & thanh toán
- [ ] Thông báo realtime
- [ ] Đánh giá & review

## 👨‍💻 Lưu ý khi phát triển

1. **API Base URL**: Hiện tại đang dùng staging API tại `https://evmarket-api-staging.onrender.com/`
2. **Min SDK**: 24 (Android 7.0)
3. **Target SDK**: 36
4. **Compose**: Sử dụng Material 3 cho UI components
5. **State Management**: Sử dụng StateFlow và collectAsState() trong Compose

## 📄 License

Copyright © 2025 EV Market Team
