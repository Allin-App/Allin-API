package allin.routing

import allin.dataSource
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.*
import allin.utils.AppConfig
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.swagger.annotations.Api
import java.util.*

val tokenManagerBet = AppConfig.tokenManager


fun Application.BetRouter() {
    val userDataSource = this.dataSource.userDataSource
    val betDataSource = this.dataSource.betDataSource
    val participationDataSource = this.dataSource.participationDataSource

    routing {
        authenticate {
            post("/bets/add", {
                description = "Allows a user to create a new bet"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                    body<Bet> {
                        description = "Bet to add in the selected source"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "the bet has been added"
                        body<Bet>() {
                            description = "Bet with assigned id"
                        }
                    }
                    HttpStatusCode.Conflict to {
                        description = "Id already exist"
                        body(ApiMessage.BET_ALREADY_EXIST)
                    }
                }
            }) {
                hasToken { principal ->
                    val bet = call.receive<Bet>()
                    val id = UUID.randomUUID().toString()
                    val username = tokenManagerBet.getUsernameFromToken(principal)
                    betDataSource.getBetById(id)?.let {
                        call.respond(HttpStatusCode.Conflict, ApiMessage.BET_ALREADY_EXIST)
                    } ?: run {
                        val betWithId = bet.copy(id = id, createdBy = username)
                        betDataSource.addBet(betWithId)
                        call.respond(HttpStatusCode.Created, betWithId)
                    }
                }
            }
        }
        authenticate {
            get("/bets/gets", {
                description = "Allows you to recover all bets"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of bets is available"
                        body<List<Bet>>() {
                            description = "List of all bet in the selected source"
                        }
                    }
                }
            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, _ ->
                        call.respond(HttpStatusCode.Accepted, betDataSource.getAllBets())
                    }
                }
            }
        }

        get("/bets/get/{id}", {
            description = "Retrieves a specific bet"
            request {
                pathParameter<UUID>("Id of the desired bet")
            }
            response {
                HttpStatusCode.Accepted to {
                    description = "The bet is available"
                    body<Bet> {
                        description = "Desired bet"
                    }
                }
                HttpStatusCode.NotFound to {
                    description = "Bet not found in the selected source"
                    body(ApiMessage.BET_NOT_FOUND)
                }
            }
        }) {
            val id = call.parameters["id"] ?: ""
            betDataSource.getBetById(id)?.let { bet ->
                call.respond(HttpStatusCode.Accepted, bet)
            } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.BET_NOT_FOUND)
        }

        post("/bets/delete", {
            description = "Delete a specific bet"
            request {
                body<Map<String, String>> {
                    description = "Id of the desired bet"
                }
            }
            response {
                HttpStatusCode.Accepted to {
                    description = "The bet has been deleted"
                }
                HttpStatusCode.NotFound to {
                    description = "Bet not found in the selected source"
                    body(ApiMessage.BET_NOT_FOUND)
                }
            }
        }) {
            val id = call.receive<Map<String, String>>()["id"] ?: ""
            if (betDataSource.removeBet(id)) {
                call.respond(HttpStatusCode.Accepted)
            } else {
                call.respond(HttpStatusCode.NotFound, ApiMessage.BET_NOT_FOUND)
            }
        }

        post("bets/update", {
            description = "Update a specific bet"
            request {
                body<UpdatedBetData> {
                    description = "Information of the updated bet"
                }
            }
            response {
                HttpStatusCode.Accepted to {
                    description = "The bet has been updated"
                }
                HttpStatusCode.NotFound to {
                    description = "Bet not found in the selected source"
                    body(ApiMessage.BET_NOT_FOUND)
                }
            }
        }) {
            val updatedBetData = call.receive<UpdatedBetData>()
            if (betDataSource.updateBet(updatedBetData)) {
                call.respond(HttpStatusCode.Accepted)
            } else {
                call.respond(HttpStatusCode.NotFound, ApiMessage.BET_NOT_FOUND)
            }
        }

        authenticate {
            get("/bets/toConfirm", {
                description = "Allows a user to know which bets can be validated"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of bets that can be validated is available"
                        body<List<BetDetail>>() {
                            description = "list of bets that can be validated"
                        }
                    }
                }
            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val response = betDataSource.getToConfirm(user.username).map {
                            val participations = participationDataSource.getParticipationFromBetId(it.id)
                            BetDetail(
                                it,
                                getBetAnswerDetail(it, participations),
                                participations,
                                participations.find { it.username == user.username }
                            )
                        }
                        call.respond(HttpStatusCode.Accepted, response)
                    }
                }
            }
        }

        authenticate {
            get("/bets/getWon", {
                description = "Allows a user to know their won bets"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of won bets is available"
                        body<List<BetResultDetail>>() {
                            description = "List of won bets"
                        }
                    }
                }
            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        call.respond(HttpStatusCode.Accepted, betDataSource.getWonNotifications(user.username))
                    }
                }
            }
        }

        authenticate {
            get("/bets/history", {
                description = "Allows a user to know own history of bets"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "Bet history is available"
                        body<List<BetResultDetail>>() {
                            description = "Betting history list"
                        }
                    }
                }
            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        call.respond(HttpStatusCode.Accepted, betDataSource.getHistory(user.username))
                    }
                }
            }
        }

        authenticate {
            get("/bets/current", {
                description = "Allows a user to know current bets"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "List of current bets is available"
                        body<List<BetDetail>> {
                            description = "List of current bets"
                        }
                    }
                }
            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        call.respond(HttpStatusCode.Accepted, betDataSource.getCurrent(user.username))
                    }
                }
            }
        }

        authenticate {
            post("/bets/confirm/{id}", {
                description = "allows the creator of a bet to confrm the final answer"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                    pathParameter<UUID>("Id of the desired bet")
                    body<String> {
                        description = "Final answer of the bet"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "The final answer has been set"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "The user is not the creator of the bet"
                    }
                }
            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val betId = call.parameters["id"] ?: ""
                        val result = call.receive<String>()

                        if (betDataSource.getBetById(betId)?.createdBy == user.username) {
                            betDataSource.confirmBet(betId, result)
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.Unauthorized)
                        }

                    }
                }
            }
        }

    }
}