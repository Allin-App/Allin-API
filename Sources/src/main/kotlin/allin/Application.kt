package allin

import allin.data.AllInDataSource
import allin.data.mock.MockDataSource
import allin.data.postgres.PostgresDataSource
import allin.routing.*
import allin.utils.TokenManager
import allin.utils.TokenManager.Companion.Claims.USERNAME
import allin.utils.kronJob
import com.typesafe.config.ConfigFactory
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSort
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSyntaxHighlight
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@ExperimentalTime
val BET_VERIFY_DELAY = 1.minutes

val data_source = System.getenv()["DATA_SOURCE"]
val isCodeFirstContainer = System.getenv()["CODEFIRST_CONTAINER"].orEmpty()

private val allInDataSource: AllInDataSource = when (data_source) {
    "mock" -> MockDataSource()
    "postgres" -> PostgresDataSource()
    else -> MockDataSource()
}
val Application.dataSource: AllInDataSource
    get() = allInDataSource

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        extracted()
    }.start(wait = true)
}

@OptIn(ExperimentalTime::class)
private fun Application.extracted() {
    val config = HoconApplicationConfig(ConfigFactory.load())
    val tokenManager = TokenManager.getInstance(config)
    authentication {
        jwt {
            verifier(tokenManager.verifyJWTToken())
            realm = config.property("realm").getString()
            validate { jwtCredential ->
                if (jwtCredential.payload.getClaim(USERNAME).asString().isNotEmpty())
                    JWTPrincipal(jwtCredential.payload)
                else null
            }
        }
    }
    install(ContentNegotiation) { json() }
    install(SwaggerUI){
        swagger {
            swaggerUrl = "swagger"
            rootHostPath= isCodeFirstContainer
            swaggerUrl= "$isCodeFirstContainer/swagger"
            onlineSpecValidator()
            displayOperationId = true
            showTagFilterInput = true
            sort = SwaggerUiSort.HTTP_METHOD
        }
        info {
            title = "Allin API"
            version = "latest"
            description = "Allin API"
            license {
                name = "Apache 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.html"
            }
        }
    }

    BasicRouting()
    UserRouter()
    BetRouter()
    ParticipationRouter()
    BetDetailRouter()

    kronJob(BET_VERIFY_DELAY) {
        dataSource.betDataSource.updateBetStatuses(ZonedDateTime.now())
    }
}