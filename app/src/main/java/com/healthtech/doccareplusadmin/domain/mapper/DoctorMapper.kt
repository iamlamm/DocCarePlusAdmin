import com.healthtech.doccareplusadmin.data.local.entity.DoctorEntity
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.domain.model.UserRole

// Kiểm tra các phương thức chuyển đổi
fun Doctor.toDoctorEntity(): DoctorEntity {
    return DoctorEntity(
        id = id,
        code = code,
        name = name,
        specialty = specialty,
        categoryId = categoryId,
        rating = rating,
        reviews = reviews,
        fee = fee,
        avatar = avatar,
        available = available,
        biography = biography,
        role = role.name,
        email = email,
        phoneNumber = phoneNumber,
        emergencyContact = emergencyContact,
        address = address
    )
}

fun DoctorEntity.toDoctor(): Doctor {
    return Doctor(
        id = id,
        code = code,
        name = name,
        specialty = specialty,
        categoryId = categoryId,
        rating = rating,
        reviews = reviews,
        fee = fee,
        avatar = avatar,
        available = available,
        biography = biography,
        role = UserRole.valueOf(role),
        email = email,
        phoneNumber = phoneNumber,
        emergencyContact = emergencyContact,
        address = address
    )
}