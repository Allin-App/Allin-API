package allin.routing

import allin.dataSource
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

    val userDataSource = this.dataSource.userDataSource
    val participationDataSource = this.dataSource.participationDataSource

    routing {
        authenticate {
            post("/participations/add") {
                hasToken { principal ->
                    val participation = call.receive<ParticipationRequest>()
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        if (user.nbCoins >= participation.stake) {
                            participationDataSource.addParticipation(
                                Participation(
                                    id = UUID.randomUUID().toString(),
                                    betId = participation.betId,
                                    username = user.username,
                                    answer = participation.answer,
                                    stake = participation.stake
                                )
                            )

                            userDataSource.removeCoins(username = user.username, amount = participation.stake)

                            call.respond(HttpStatusCode.Created)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, ApiMessage.NOT_ENOUGH_COINS)
                        }
                    }
                }
            }
            delete("/participations/delete") {
                hasToken {
                    val participationId = call.receive<String>()
                    if (participationDataSource.deleteParticipation(participationId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiMessage.PARTICIPATION_NOT_FOUND)
                    }
                }
            }
        }
    }
}