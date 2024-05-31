package allin.utils

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object AppConfig {
    private val config: HoconApplicationConfig = HoconApplicationConfig(ConfigFactory.load())
    val tokenManager = TokenManager.getInstance(config)
    val regexChecker = RegexChecker()
    val cryptManager = CryptManager()
    val imageManager = ImageManager()
    val urlManager = URLManager()
}
