package allin.utils

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

object AppConfig {
    val config: HoconApplicationConfig = HoconApplicationConfig(ConfigFactory.load())
    val tokenManager = TokenManager.getInstance(config)
    val regexChecker= RegexChecker()
    val cryptManager = CryptManager()
}
