package  com.healthtech.doccareplusadmin.domain.mapper

import com.healthtech.doccareplusadmin.data.local.entity.CategoryEntity
import com.healthtech.doccareplusadmin.domain.model.Category

fun CategoryEntity.toCategory(): Category {
    return Category(
        id = id, code = code, name = name, icon = icon, description = description
    )
}

fun Category.toCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = id, code = code, name = name, icon = icon, description = description
    )
}