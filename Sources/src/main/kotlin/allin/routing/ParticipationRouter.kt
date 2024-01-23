package allin.routing

import allin.entities.ParticipationsEntity.addParticipationEntity
import allin.entities.ParticipationsEntity.deleteParticipation
import allin.entities.ParticipationsEntity.getParticipationEntity
import allin.entities.UsersEntity.modifyCoins
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

fun Application.ParticipationRouter() {
    routing {
        authenticate {
            post("/participations/add") {
                hasToken { principal ->
                    val participation = call.receive<ParticipationRequest>()
                    verifyUserFromToken(principal) { user, _ ->
                        if (user.nbCoins >= participation.stake) {
                            addParticipationEntity(
                                Participation(
                                    id = UUID.randomUUID().toString(),
                                    betId = participation.betId,
                                    username = user.username,
                                    answer = participation.answer,
                                    stake = participation.stake
                                )
                            )
                            modifyCoins(user.username,participation.stake)
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
                    getParticipationEntity().find { it.id == participationId }?.let { participation ->
                        verifyUserFromToken(principal) { _, _ ->
                            deleteParticipation(participation)
                            call.respond(HttpStatusCode.NoContent)
                        }
                    } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.ParticipationNotFound)
                }
            }
        }
    }
}