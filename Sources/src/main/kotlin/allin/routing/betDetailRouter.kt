package allin.routing

import allin.dataSource
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.ApiMessage
import allin.model.BetDetail
import allin.utils.AppConfig
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun Application.betDetailRouter() {
    val userDataSource = this.dataSource.userDataSource
    val betDataSource = this.dataSource.betDataSource
    val logManager = AppConfig.logManager

    routing {
        authenticate {
            get("/betdetail/get/{id}", {
                description = "Retrieves the details of a specific bet"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                    pathParameter<UUID>("Id of the desired detail bet")
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The bet can be returned"
                        body<BetDetail>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Bet not found in the selected source"
                        body(ApiMessage.BET_NOT_FOUND)
                    }
                }
            }) {
                logManager.log("Routing", "GET /betdetail/get/{id}")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val id = call.parameters["id"].toString()
                        val result = betDataSource.getBetDetailById(id, user.id)
                        if (result != null) {
                            logManager.log("Routing", "ACCEPTED GET /betdetail/get/{id}\t${result}")
                            call.respond(
                                HttpStatusCode.Accepted,
                                result
                            )
                        } else {
                            logManager.log("Routing", "${ApiMessage.BET_NOT_FOUND} GET /betdetail/get/{id}")
                            call.respond(HttpStatusCode.NotFound, ApiMessage.BET_NOT_FOUND)
                        }
                    }
                }
            }
        }
    }
}

