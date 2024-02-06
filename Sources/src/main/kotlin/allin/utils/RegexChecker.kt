package allin.utils

class RegexChecker {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"

    fun isEmailInvalid(email: String): Boolean {
        val emailRegex = Regex(emailRegex)
        return !emailRegex.matches(email)
    }
}