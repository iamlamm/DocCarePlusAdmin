package  com.healthtech.doccareplusadmin.domain.model

enum class NotificationType {
    DOCTOR_REGISTRATION,    // Bác sĩ mới đăng ký
    USER_REGISTRATION,      // Người dùng mới đăng ký
    APPOINTMENT_BOOKED,     // Có lịch hẹn mới
    SYSTEM,                 // Thông báo hệ thống
    REPORT,                // Báo cáo mới
    ADMIN_NEW_APPOINTMENT  // Thông báo cuộc hẹn mới cho admin
}