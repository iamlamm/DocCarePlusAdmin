package  com.healthtech.doccareplusadmin.domain.model

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val time: Long = 0,
    val type: NotificationType = NotificationType.APPOINTMENT,
    val userId: String = "",
    val read: Boolean = false
)