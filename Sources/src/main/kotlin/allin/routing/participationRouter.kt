package allin.routing

import allin.dataSource
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.ApiMessage
import allin.model.Participation
import allin.model.ParticipationRequest
import io.github.smiley4.ktorswaggerui.dsl.delete
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun Application.participationRouter() {

    val userDataSource = this.dataSource.userDataSource
    val participationDataSource = this.dataSource.participationDataSource

    routing {
        authenticate {
            post("/participations/add", {
                description = "Allows a user to add a stake to a bet"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                    body<ParticipationRequest> {
                        description = "Participation in a bet"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "The stake has been bet"
                    }
                    HttpStatusCode.Forbidden to {
                        description = "User does not have enough coins"
                        body(ApiMessage.NOT_ENOUGH_COINS)
                    }
                }

            }) {
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
            delete("/participations/delete", {
                description = "Allows to delete a participation to a bet"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                    body<String> {
                        description = "Id of the participation"
                    }
                }
                response {
                    HttpStatusCode.NotFound to {
                        description = "Participation was not found"
                        body(ApiMessage.PARTICIPATION_NOT_FOUND)
                    }
                    HttpStatusCode.NoContent to {
                        description = "The operation was successful"
                    }
                }
            }) {
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