package  com.healthtech.doccareplusadmin.ui.auth

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
    data class InvalidRole(val message: String) : LoginState()
}