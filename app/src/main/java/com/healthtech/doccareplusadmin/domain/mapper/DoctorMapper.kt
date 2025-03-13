package  com.healthtech.doccareplusadmin.domain.mapper

import com.healthtech.doccareplusadmin.data.local.entity.DoctorEntity
import com.healthtech.doccareplusadmin.domain.model.Doctor

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
        image = image,
        available = available,
        biography = biography,
        email = email,
        phoneNumber = phoneNumber,
        emergencyContact = emergencyContact,
        address = address
    )
}

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
        image = image,
        available = available,
        biography = biography,
        email = email,
        phoneNumber = phoneNumber,
        emergencyContact = emergencyContact,
        address = address
    )
}