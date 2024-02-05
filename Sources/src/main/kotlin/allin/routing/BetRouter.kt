package allin.routing

import allin.dataSource
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.ApiMessage
import allin.model.Bet
import allin.model.UpdatedBetData
import allin.utils.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

val tokenManagerBet = AppConfig.tokenManager


fun Application.BetRouter() {
    val userDataSource = this.dataSource.userDataSource
    val betDataSource = this.dataSource.betDataSource
    val participationDataSource = this.dataSource.participationDataSource

    routing {
        route("/bets/add") {
            authenticate {
                post {
                    hasToken { principal ->
                        val bet = call.receive<Bet>()
                        val id = UUID.randomUUID().toString()
                        val username = tokenManagerBet.getUsernameFromToken(principal)
                        betDataSource.getBetById(id)?.let {
                            call.respond(HttpStatusCode.Conflict, ApiMessage.BetAlreadyExist)
                        } ?: run {
                            val betWithId = bet.copy(id = id, createdBy = username)
                            betDataSource.addBet(betWithId)
                            call.respond(HttpStatusCode.Created, betWithId)
                        }
                    }

                }

            }
        }

        route("/bets/gets") {
            get {
                // if(bets.size>0)
                call.respond(HttpStatusCode.Accepted, betDataSource.getAllBets())
                // else call.respond(HttpStatusCode.NoContent)
            }
        }

        route("/bets/get/{id}") {
            get {
                val id = call.parameters["id"] ?: ""
                betDataSource.getBetById(id)?.let { bet ->
                    call.respond(HttpStatusCode.Accepted, bet)
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
            }
        }

        route("/bets/delete") {
            post {
                val id = call.receive<Map<String, String>>()["id"] ?: ""
                if (betDataSource.removeBet(id)) {
                    call.respond(HttpStatusCode.Accepted)
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
                }

            }
        }
        route("bets/update") {
            post {
                val updatedBetData = call.receive<UpdatedBetData>()
                if (betDataSource.updateBet(updatedBetData)) {
                    call.respond(HttpStatusCode.Accepted)
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
                }
            }
        }

        authenticate {
            get("/bets/current") {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val currentBets = betDataSource.getBetsNotFinished()
                            .filter { bet ->
                                val userParticipation =
                                    participationDataSource.getParticipationFromUserId(user.username, bet.id)
                                userParticipation.isNotEmpty()
                            }

                        call.respond(HttpStatusCode.OK, currentBets)
                    }
                }
            }
        }
    }
}