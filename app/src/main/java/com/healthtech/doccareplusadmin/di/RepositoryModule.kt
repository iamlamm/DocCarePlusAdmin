package  com.healthtech.doccareplusadmin.di

import com.healthtech.doccareplusadmin.data.repository.AuthRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.CategoryRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.DoctorRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.StorageRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.TimeSlotRepositoryImpl
import com.healthtech.doccareplusadmin.data.repository.UserRepositoryImpl
import com.healthtech.doccareplusadmin.domain.repository.AuthRepository
import com.healthtech.doccareplusadmin.domain.repository.CategoryRepository
import com.healthtech.doccareplusadmin.domain.repository.DoctorRepository
import com.healthtech.doccareplusadmin.domain.repository.StorageRepository
import com.healthtech.doccareplusadmin.domain.repository.TimeSlotRepository
import com.healthtech.doccareplusadmin.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
Lợi ích của việc return interface:
    1. Dependency Injection sẽ biết rằng khi một class yêu cầu CategoryRepository, nó sẽ cung cấp instance của CategoryRepositoryImpl
    2. Các class sử dụng repository (như MainViewModel) chỉ biết về interface, không biết về implementation cụ thể
    3. Chúng ta có thể dễ dàng thay đổi implementation bằng cách sửa trong module mà không ảnh hưởng đến các class khác

    @Provides
    @Singleton
    fun provideCategoryRepository(
        firebaseApi: FirebaseApi, localDataSource: CategoryLocalDataSource
    ): CategoryRepository {
        return CategoryRepositoryImpl(firebaseApi, localDataSource)
    }

    @Provides
    @Singleton
    fun provideDoctorRepository(
        firebaseApi: FirebaseApi, localDataSource: DoctorLocalDataSource
    ): DoctorRepository {
        return DoctorRepositoryImpl(firebaseApi, localDataSource)
    }



Đây là một câu hỏi rất hay! Hãy để tôi giải thích cách `@Binds` hoạt động:

1. **Tại sao không có thân hàm?**
- `@Binds` là một annotation đặc biệt mà Hilt/Dagger sử dụng để tự động tạo code binding tại thời điểm compile
- Function được đánh dấu `@Binds` phải là abstract vì Hilt sẽ tự động implement nó
- Không cần thân hàm vì mục đích duy nhất là "nói" với Hilt rằng: "khi cần CategoryRepository, hãy dùng CategoryRepositoryImpl"

2. **Các tham số được cung cấp từ đâu?**
Lấy ví dụ với CategoryRepository:

```kotlin
// Implementation với @Inject constructor
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val firebaseApi: FirebaseApi,
    private val localDataSource: CategoryLocalDataSource
) : CategoryRepository

// Binding trong Module
@Binds
abstract fun bindCategoryRepository(
    categoryRepositoryImpl: CategoryRepositoryImpl  // Parameter
): CategoryRepository  // Return type
```

Quá trình diễn ra như sau:

a) Khi một class nào đó cần inject CategoryRepository:
```kotlin
class MainViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository  // Yêu cầu CategoryRepository
)
```

b) Hilt sẽ:
1. Thấy rằng cần cung cấp CategoryRepository
2. Tìm trong các Module và thấy binding trong RepositoryModule
3. Biết rằng cần tạo CategoryRepositoryImpl
4. Nhìn vào constructor của CategoryRepositoryImpl và thấy cần:
   - FirebaseApi
   - CategoryLocalDataSource
5. Tự động tìm và inject các dependencies này
6. Tạo instance của CategoryRepositoryImpl
7. Trả về instance này khi có yêu cầu CategoryRepository

3. **So sánh với @Provides**:
```kotlin
// Cách dùng @Provides
@Provides
fun provideCategoryRepository(
    firebaseApi: FirebaseApi,  // Phải khai báo rõ dependencies
    localDataSource: CategoryLocalDataSource
): CategoryRepository {
    return CategoryRepositoryImpl(firebaseApi, localDataSource)  // Phải viết code khởi tạo
}

// Cách dùng @Binds
@Binds
abstract fun bindCategoryRepository(
    impl: CategoryRepositoryImpl  // Chỉ cần khai báo implementation
): CategoryRepository
```

4. **Code được Hilt tạo ra**:
Về cơ bản, Hilt sẽ tự động tạo code tương tự như sau:
```kotlin
// Code được tạo tự động bởi Hilt (pseudo code)
fun generatedBindCategoryRepository(): CategoryRepository {
    val firebaseApi = getFirebaseApi()  // Lấy từ dependency graph
    val localDataSource = getCategoryLocalDataSource()  // Lấy từ dependency graph
    return CategoryRepositoryImpl(firebaseApi, localDataSource)
}
```

Đây là lý do tại sao `@Binds`:
- Hiệu quả hơn: Ít code được tạo ra
- An toàn hơn: Kiểm tra type tại compile-time
- Ngắn gọn hơn: Không cần viết code khởi tạo
- Tự động hơn: Hilt tự quản lý việc inject dependencies

------



Đúng rồi! Parameter trong `@Binds` chính là mấu chốt. Hãy phân tích kỹ hơn:

```kotlin
@Binds
abstract fun bindCategoryRepository(
    categoryRepositoryImpl: CategoryRepositoryImpl  // <- Đây là implementation sẽ được inject
): CategoryRepository  // <- Đây là interface mà các class khác yêu cầu
```

Khi bạn khai báo như vậy, bạn đang nói với Hilt:
1. "Này Hilt, khi có ai đó yêu cầu `CategoryRepository`..."
2. "...hãy cung cấp một instance của `CategoryRepositoryImpl`"

Một số điểm quan trọng về parameter trong `@Binds`:

1. **Type Matching**:
```kotlin
// ✅ Đúng - Implementation phải implement interface
@Binds
abstract fun bind(impl: CategoryRepositoryImpl): CategoryRepository

// ❌ Sai - DoctorRepositoryImpl không implement CategoryRepository
@Binds
abstract fun bind(impl: DoctorRepositoryImpl): CategoryRepository
```

2. **Single Parameter**:
```kotlin
// ✅ Đúng - Chỉ một parameter
@Binds
abstract fun bind(impl: CategoryRepositoryImpl): CategoryRepository

// ❌ Sai - @Binds không thể có nhiều parameter
@Binds
abstract fun bind(
    impl: CategoryRepositoryImpl,
    api: FirebaseApi
): CategoryRepository
```

3. **Constructor Injection**:
```kotlin
// Implementation phải có @Inject constructor
@Singleton
class CategoryRepositoryImpl @Inject constructor(  // <- Quan trọng!
    private val firebaseApi: FirebaseApi,
    private val localDataSource: CategoryLocalDataSource
) : CategoryRepository
```

4. **Naming Convention**:
```kotlin
// Tên parameter nên mô tả rõ đây là implementation
@Binds
abstract fun bindCategoryRepository(
    categoryRepositoryImpl: CategoryRepositoryImpl  // impl, repositoryImpl, ...
): CategoryRepository

// Hoặc ngắn gọn
@Binds
abstract fun bindCategoryRepository(
    impl: CategoryRepositoryImpl
): CategoryRepository
```

5. **Scope Annotation**:
```kotlin
@Binds
@Singleton  // Scope annotation vẫn áp dụng bình thường
abstract fun bindCategoryRepository(
    impl: CategoryRepositoryImpl
): CategoryRepository
```

Tóm lại:
- Parameter trong `@Binds` chính là implementation mà Hilt sẽ sử dụng
- Implementation này phải có `@Inject constructor`
- Chỉ được phép có một parameter
- Parameter phải implement interface được trả về
- Hilt sẽ tự động resolve các dependencies trong constructor của implementation


 */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindDoctorRepository(
        doctorRepositoryImpl: DoctorRepositoryImpl
    ): DoctorRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTimeSlotReposiory(
        timeSlotReposiory: TimeSlotRepositoryImpl
    ): TimeSlotRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository
}