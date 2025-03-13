package  com.healthtech.doccareplusadmin.utils

import javax.inject.Singleton

@Singleton
object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        if (password.length !in 6..64) return false

        val upperCase = "(?=.*[A-Z])".toRegex()    // Ít nhất 1 chữ hoa
        val lowerCase = "(?=.*[a-z])".toRegex()    // Ít nhất 1 chữ thường
        val digit = "(?=.*\\d)".toRegex()          // Ít nhất 1 số
        val specialChar =
            "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])".toRegex() // Ít nhất 1 ký tự đặc biệt

        val passwordPattern = listOf(
            upperCase,
            lowerCase,
            digit,
            specialChar
        ).fold("^") { acc, regex -> acc + regex.pattern } + ".*\$"

        return password.matches(passwordPattern.toRegex())
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.length >= 10
    }

    fun isValidName(name: String): Boolean {
        return name.length >= 2
    }
}