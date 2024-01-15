package allin.routing

import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.ApiMessage
import allin.model.Participation
import allin.model.ParticipationRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

val participations = mutableListOf<Participation>()

fun Application.ParticipationRouter() {
    routing {
        authenticate {
            post("/participations/add") {
                hasToken { principal ->
                    val participation = call.receive<ParticipationRequest>()
                    verifyUserFromToken(principal) { user ->
                        if (user.nbCoins >= participation.stake) {
                            participations.add(
                                Participation(
                                    id = UUID.randomUUID().toString(),
                                    betId = participation.betId,
                                    username = user.username,
                                    answer = participation.answer,
                                    stake = participation.stake
                                )
                            )
                            call.respond(HttpStatusCode.Created)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, ApiMessage.NotEnoughCoins)
                        }
                    }
                }
            }
            delete("/participations/delete") {
                hasToken { principal ->
                    val participationId = call.receive<String>()
                    participations.find { it.id == participationId }?.let { participation ->
                        verifyUserFromToken(principal) { user ->
                            user.nbCoins += participation.stake
                            participations.remove(participation)
                            call.respond(HttpStatusCode.NoContent)
                        }
                    } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.ParticipationNotFound)
                }
            }
        }
    }
}