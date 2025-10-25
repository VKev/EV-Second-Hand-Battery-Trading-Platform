# 🚀 Hướng dẫn sử dụng EV Market App

## Tổng quan dự án

Ứng dụng Android hiện đại cho nền tảng mua bán pin và xe điện cũ, được xây dựng với **Jetpack Compose** và **Material Design 3**.

---

## 📂 Cấu trúc thư mục

### Data Layer (`data/`)
- **model/**: Chứa data classes (Battery, Vehicle, Auth, User)
- **remote/**: API services và Retrofit configuration
- **repository/**: Business logic và data operations

### UI Layer (`ui/`)
- **navigation/**: Navigation setup với Compose Navigation
- **screen/**: Các màn hình UI
  - `auth/`: Login & Register screens
  - `home/`: Home screen với danh sách sản phẩm
  - Các screen khác: Products, Wallet, Profile, AddPost
- **theme/**: Theme configuration (colors, typography)
- **viewmodel/**: ViewModels quản lý UI state

---

## 🎨 Màn hình chính

### 1. Login Screen
- Input: Email và Password
- Validation tự động
- Hiển thị lỗi khi API trả về lỗi
- Loading state khi đang xử lý
- Link chuyển sang Register

### 2. Register Screen  
- Input: Name, Email, Password, Confirm Password
- Validation:
  - Tất cả field bắt buộc
  - Password tối thiểu 8 ký tự
  - Password và Confirm Password phải khớp
- Tự động login sau khi đăng ký thành công

### 3. Home Screen
- **Batteries Section**: Hiển thị horizontal scroll
- **Vehicles Section**: Hiển thị vertical list
- **Lazy Loading**: Tự động load thêm khi scroll
- **Pull to Refresh**: Kéo xuống để làm mới
- **Filter**: Chỉ hiển thị status "AVAILABLE"

### 4. Bottom Navigation
- **Guest mode (3 tabs)**:
  - 🏠 Trang chủ
  - 🛒 Sản phẩm
  - 👤 Hồ sơ

- **Authenticated mode (5 tabs)**:
  - 🏠 Trang chủ
  - 🛒 Sản phẩm
  - ➕ Đăng tin (FAB giữa)
  - 💰 Ví
  - 👤 Hồ sơ

---

## 🔧 ViewModels

### AuthViewModel
```kotlin
// State management
val authState: StateFlow<AuthState>
val isLoggedIn: StateFlow<Boolean>

// Methods
fun login(email: String, password: String)
fun register(name: String, email: String, password: String)
fun logout()
```

**AuthState**:
- `Idle`: Trạng thái ban đầu
- `Loading`: Đang xử lý request
- `Success(user)`: Thành công
- `Error(message)`: Có lỗi
- `LoggedOut`: Đã đăng xuất

### HomeViewModel
```kotlin
// State
val state: StateFlow<HomeState>

// Methods
fun loadBatteries(page: Int)
fun loadVehicles(page: Int)
fun loadMoreBatteries()
fun loadMoreVehicles()
fun refresh()
```

**HomeState**:
- `batteries: List<Battery>`: Danh sách pin
- `vehicles: List<Vehicle>`: Danh sách xe
- `isLoadingBatteries/Vehicles`: Loading state
- `currentPage`: Trang hiện tại
- `hasMore`: Còn dữ liệu để load

---

## 💾 Data Models

### Battery
```kotlin
{
  "id": String,
  "title": String,
  "description": String,
  "price": Int,
  "images": List<String>,
  "status": String,
  "brand": String,
  "capacity": Int,
  "year": Int,
  "health": Int?,
  "specifications": BatterySpecifications,
  "isVerified": Boolean
}
```

### Vehicle
```kotlin
{
  "id": String,
  "title": String,
  "brand": String,
  "model": String,
  "year": Int,
  "price": Int,
  "mileage": Int,
  "images": List<String>,
  "status": String,
  "specifications": VehicleSpecifications,
  "isVerified": Boolean
}
```

---

## 🌐 API Integration

### Base URL
```
https://evmarket-api-staging.onrender.com/api/v1/
```

### Endpoints được sử dụng

**Authentication**
```
POST /auth/login
POST /auth/register
```

**Products**
```
GET /batteries/?page=1&limit=10
GET /vehicles/?page=1&limit=10
```

### Retrofit Setup
- Logging Interceptor cho debug
- Kotlinx Serialization converter
- Timeout: 30 seconds
- Automatic token management (sẵn sàng)

---

## 🎯 Tính năng nâng cao

### 1. Lazy Loading
- Detect khi user scroll gần cuối
- Tự động gọi API load thêm
- Không load lại nếu đang loading
- Hiển thị loading indicator

### 2. State Management
- Sử dụng StateFlow cho reactive updates
- Tự động update UI khi state thay đổi
- Lifecycle-aware với collectAsState()

### 3. Navigation
- Single Activity với Compose Navigation
- Tự động redirect dựa trên auth state
- Save & restore state khi navigate
- Back stack management

### 4. Image Loading
- Coil library cho hiệu suất tốt
- Placeholder và error handling
- Cache tự động

---

## 🔒 Security

### Token Storage
- Lưu trong SharedPreferences
- Tự động attach vào API requests
- Clear khi logout

### Validation
- Client-side validation trước khi gọi API
- Server-side error handling
- User-friendly error messages

---

## 🎨 UI/UX Best Practices

### Material Design 3
- Modern color scheme
- Elevation và shadows
- Ripple effects
- Smooth animations

### Responsive Design
- Adapt với nhiều kích thước màn hình
- Scrollable content
- Touch-friendly targets (min 48dp)

### Loading States
- CircularProgressIndicator khi loading
- Skeleton screens (có thể thêm)
- Error states với retry options

---

## 🐛 Debug & Testing

### Logging
- Retrofit logging cho API calls
- ViewModelScope cho coroutines
- Crash reporting (có thể thêm Firebase)

### Test Accounts
```
Email: eva_okuneva@hotmail.com
Password: password123
```

---

## 📝 Phát triển thêm

### Code để thêm tính năng mới:

1. **Thêm API endpoint mới**:
```kotlin
// Trong ProductApiService.kt hoặc tạo service mới
@GET("endpoint")
suspend fun getNewData(): Response<Data>
```

2. **Thêm màn hình mới**:
```kotlin
// Tạo file NewScreen.kt
@Composable
fun NewScreen() {
    // UI code
}

// Thêm vào AppNavigation.kt
composable(Screen.New.route) {
    NewScreen()
}
```

3. **Thêm ViewModel**:
```kotlin
class NewViewModel : ViewModel() {
    private val _state = MutableStateFlow(NewState())
    val state: StateFlow<NewState> = _state.asStateFlow()
}
```

---

## ✅ Checklist tính năng

- [x] Authentication (Login/Register)
- [x] Home screen với products
- [x] Lazy loading & pagination
- [x] Filter AVAILABLE products
- [x] Bottom navigation động
- [x] Pull to refresh
- [x] Image loading
- [x] Error handling
- [ ] Product detail screen
- [ ] Search & advanced filters
- [ ] User profile management
- [ ] Post creation
- [ ] Favorites
- [ ] Chat
- [ ] Notifications

---

## 🚀 Build & Run

1. **Sync Gradle**: Ctrl+Shift+O
2. **Build**: Build > Make Project
3. **Run**: Shift+F10

### Yêu cầu
- Android Studio Hedgehog trở lên
- Gradle 8.13
- Kotlin 2.0.21
- Min SDK 24
- Target SDK 36

---

## 📞 Hỗ trợ

Nếu gặp vấn đề:
1. Check Logcat cho errors
2. Verify internet connection
3. Check API endpoint availability
4. Clean & Rebuild project

---

**Chúc bạn code vui vẻ! 🎉**
