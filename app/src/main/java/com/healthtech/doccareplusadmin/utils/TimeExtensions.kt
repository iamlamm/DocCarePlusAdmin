package  com.healthtech.doccareplusadmin.utils

import java.util.concurrent.TimeUnit

fun Long.getTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Vừa xong"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} phút trước"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} giờ trước"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} ngày trước"
        else -> {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            sdf.format(this)
        }
    }
}