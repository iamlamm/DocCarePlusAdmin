package  com.healthtech.doccareplusadmin.domain.model

enum class EmailStatus {
    NOT_REGISTERED,           // Email chưa đăng ký
    REGISTERED_NOT_VERIFIED,  // Email đã đăng ký nhưng chưa xác thực
    VERIFIED                  // Email đã đăng ký và đã xác thực
}