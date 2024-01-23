package allin.routing

import allin.entities.BetsEntity.getBets
import allin.entities.ParticipationsEntity.getParticipationEntityFromBetId
import allin.entities.ParticipationsEntity.getParticipationEntityFromUserId
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.BetDetail
import allin.model.Participation
import allin.model.getBetAnswerDetail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.BetDetailRouter() {
    routing {
        authenticate {
            get("/betdetail/get/{id}") {
                hasToken { principal ->
                    verifyUserFromToken(principal) { user, _ ->
                        val id = call.parameters["id"].toString()
                        val participations = getParticipationEntityFromBetId(id)
                        val selectedBet = getBets().find { it.id == id }
                        if (selectedBet != null) {
                                call.respond(
                                    HttpStatusCode.Accepted,
                                    BetDetail(
                                        selectedBet,
                                        getBetAnswerDetail(participations),
                                        participations.toList(),
                                        getParticipationEntityFromUserId(user.username,id).lastOrNull()
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

