package  com.healthtech.doccareplusadmin.domain.model

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val time: Long = System.currentTimeMillis(),
    val type: NotificationType = NotificationType.SYSTEM,
    val adminId: String = "",
    val read: Boolean = false
)