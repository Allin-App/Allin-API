package allin.routing

import allin.dataSource
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.BetDetail
import allin.model.getBetAnswerDetail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.BetDetailRouter() {
    val userDataSource = this.dataSource.userDataSource
    val betDataSource = this.dataSource.betDataSource
    val participationDataSource = this.dataSource.participationDataSource

    routing {
        authenticate {
            get("/betdetail/get/{id}") {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val id = call.parameters["id"].toString()
                        val participations = participationDataSource.getParticipationFromBetId(id)
                        val selectedBet = betDataSource.getBetById(id)
                        if (selectedBet != null) {
                            call.respond(
                                HttpStatusCode.Accepted,
                                BetDetail(
                                    selectedBet,
                                    getBetAnswerDetail(selectedBet, participations),
                                    participations.toList(),
                                    participationDataSource.getParticipationFromUserId(user.username, id).lastOrNull()
                                )
                            )
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Bet not found")
                        }
                    }
                }
            }
        }
    }
}

