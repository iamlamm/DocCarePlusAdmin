package com.healthtech.doccareplusadmin.domain.mapper

import com.healthtech.doccareplusadmin.data.local.entity.UserEntity
import com.healthtech.doccareplusadmin.domain.model.Gender
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.domain.model.UserRole

fun UserEntity.toUser(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        role = UserRole.valueOf(role),
        createdAt = createdAt,
        avatar = avatar,
        height = height,
        weight = weight,
        age = age,
        bloodType = bloodType,
        about = about,
        gender = gender?.let { Gender.valueOf(it) }
    )
}

fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        role = role.name,
        createdAt = createdAt,
        avatar = avatar,
        height = height,
        weight = weight,
        age = age,
        bloodType = bloodType,
        about = about,
        gender = gender?.name
    )
}

// fun List<UserEntity>.toUserList(): List<User> {
//     return map { it.toUser() }
// }

// fun List<User>.toUserEntityList(): List<UserEntity> {
//     return map { it.toUserEntity() }
// }