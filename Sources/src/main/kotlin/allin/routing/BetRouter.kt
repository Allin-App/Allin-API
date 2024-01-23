package allin.routing

import allin.entities.BetsEntity.addBetEntity
import allin.entities.BetsEntity.getBets
import allin.entities.ParticipationsEntity.getParticipationEntity
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
    routing {
        route("/bets/add") {
            authenticate {
            post {
                hasToken { principal ->
                val bet = call.receive<Bet>()
                val id = UUID.randomUUID().toString()
                val username = tokenManagerBet.getUsernameFromToken(principal)
                val bets = getBets()
                bets.find { it.id == id }?.let {
                    call.respond(HttpStatusCode.Conflict, ApiMessage.BetAlreadyExist)
                } ?: run {
                    val betWithId = Bet(
                        id,
                        bet.theme,
                        bet.sentenceBet,
                        bet.endRegistration,
                        bet.endBet,
                        bet.isPrivate,
                        bet.response,
                        username
                    )
                    addBetEntity(betWithId)
                    call.respond(HttpStatusCode.Created, betWithId)
                }
                }

            }

            }
        }

        route("/bets/gets") {
            get {
                // if(bets.size>0)
                val bets= getBets()
                call.respond(HttpStatusCode.Accepted, bets.toList())
                // else call.respond(HttpStatusCode.NoContent)
            }
        }

        route("/bets/get/{id}") {
            get {
                val bets= getBets()
                val id = call.parameters["id"] ?: ""
                bets.find { it.id == id }?.let { bet ->
                    call.respond(HttpStatusCode.Accepted, bet)
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
            }
        }

        route("/bets/delete") {
            post {
                val idbet = call.receive<Map<String, String>>()["id"]
                val bets= getBets()
                bets.find { it.id == idbet }?.let { findbet ->
                    bets.remove(findbet)
                    call.respond(HttpStatusCode.Accepted, findbet)
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
            }
        }
        route("bets/update") {
            post {
                val updatedBetData = call.receive<UpdatedBetData>()
                val bets= getBets()
                bets.find { it.id == updatedBetData.id }?.let { findbet ->
                    findbet.endBet = updatedBetData.endBet
                    findbet.isPrivate = updatedBetData.isPrivate
                    findbet.response = updatedBetData.response
                    call.respond(HttpStatusCode.Accepted, findbet)
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
            }
        }

        authenticate {
            get("/bets/current") {
                val bets= getBets()
                hasToken { principal ->
                    verifyUserFromToken(principal) { user, _ ->
                        val bets = getParticipationEntity()
                            .filter { it.username == user.username }
                            .mapNotNull { itParticipation -> bets.find { it.id == itParticipation.betId } }
                        call.respond(HttpStatusCode.OK, bets)
                    }
                }
            }
        }
    }
}