package allin

import allin.model.User
import allin.routing.BasicRouting
import allin.routing.BetRouter
import allin.routing.UserRouter
import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import allin.utils.TokenManager
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        extracted()
    }.start(wait = true)
}

private fun Application.extracted() {
    val config = HoconApplicationConfig(ConfigFactory.load())
    val tokenManager = TokenManager.getInstance(config)
    authentication {
        jwt {
            verifier(tokenManager.verifyJWTToken())
            realm=config.property("realm").getString()
            validate { jwtCredential ->
                if(jwtCredential.payload.getClaim("username").asString().isNotEmpty())
                    JWTPrincipal(jwtCredential.payload)
                else null
            }
        }
    }
    install(ContentNegotiation) {
        json()
    }
    BasicRouting()
    UserRouter()
    BetRouter()
}
