package allin

import allin.routing.BasicRouting
import allin.routing.BetRouter
import allin.routing.ParticipationRouter
import allin.routing.UserRouter
import allin.utils.TokenManager
import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import org.ktorm.database.Database

val db_database=System.getenv().get("POSTGRES_DB")
val db_user=System.getenv().get("POSTGRES_USER")
val db_password=System.getenv().get("POSTGRES_PASSWORD")
var database : Database = instanceBD()
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        extracted()
    }.start(wait = true)
}

fun instanceBD(): Database{
    try{
        database = Database.connect("jdbc:postgresql://$db_database", user = db_user, password = db_password)
    }catch (e:Exception){
        //println("jdbc:postgresql://$db_database$db_user$db_password")
    }
    return database
}

private fun Application.extracted() {
    val config = HoconApplicationConfig(ConfigFactory.load())
    val tokenManager = TokenManager.getInstance(config)
    authentication {
        jwt {
            verifier(tokenManager.verifyJWTToken())
            realm = config.property("realm").getString()
            validate { jwtCredential ->
                if (jwtCredential.payload.getClaim("username").asString().isNotEmpty())
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
    ParticipationRouter()
}
