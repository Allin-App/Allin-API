package allin.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LogManager {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun log(type: String,message: String) {
        println("[${LocalDateTime.now().format(formatter)}] [${type}] $message")
    }
}