package allin.routing

import allin.dataSource
import allin.dto.UserDTO
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.*
import allin.utils.AppConfig
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

val tokenManagerBet = AppConfig.tokenManager

fun Application.betRouter() {
    val userDataSource = this.dataSource.userDataSource
    val betDataSource = this.dataSource.betDataSource
    val participationDataSource = this.dataSource.participationDataSource
    val logManager = AppConfig.logManager

    routing {
        authenticate {
            post("/bets/add", {
                description = "Allows a user to create a new bet"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    body<Bet> {
                        description = "Bet to add in the selected source"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "the bet has been added"
                        body<Bet> {
                            description = "Bet with assigned id"
                        }
                    }
                    HttpStatusCode.Conflict to {
                        description = "Id already exist"
                        body(ApiMessage.BET_ALREADY_EXIST)
                    }
                }
            }) {
                logManager.log("Routing", "POST /bets/add")
                hasToken { principal ->
                    val bet = call.receive<Bet>()
                    val id = UUID.randomUUID().toString()
                    val username = tokenManagerBet.getUsernameFromToken(principal)
                    val user = userDataSource.getUserByUsername(username)
                    betDataSource.getBetById(id)?.let {
                        logManager.log("Routing", "${ApiMessage.BET_ALREADY_EXIST} /bets/add")
                        call.respond(HttpStatusCode.Conflict, ApiMessage.BET_ALREADY_EXIST)
                    } ?: run {
                        val betWithId = bet.copy(id = id, createdBy = user.first?.id.toString())

                        if (bet.isPrivate && bet.userInvited?.isNotEmpty() == true) {
                            betDataSource.addPrivateBet(betWithId)
                        } else betDataSource.addBet(betWithId)
                        logManager.log("Routing", "CREATED /bets/add\t${betWithId}")
                        call.respond(HttpStatusCode.Created, betWithId)
                    }
                }
            }
        }
        authenticate {
            post("/bets/gets", {
                description = "Allows you to recover all bets"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    body<List<BetFilter>> {
                        description = "List of filters"
                    }
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of bets is available"
                        body<List<Bet>> {
                            description = "List of all bet in the selected source"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "POST /bets/gets")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val filtersRequest =
                            kotlin.runCatching { call.receiveNullable<BetFiltersRequest>() }.getOrNull()
                        val filters =
                            filtersRequest?.filters ?: emptyList() // Use provided filters or empty list if null
                        logManager.log("Routing", "ACCEPTED /bets/gets\t${filters}")
                        call.respond(HttpStatusCode.Accepted, betDataSource.getAllBets(filters, user))
                    }
                }
            }
        }

        authenticate {
            get("/bets/popular", {
                description = "Allows you to recover the most popular public bets"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The most popular public bet is available"
                        body<Bet> {
                            description = "The most popular public bet"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "GET /bets/popular")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, _ ->
                        val bet = betDataSource.getMostPopularBet()
                        if (bet != null) {
                            logManager.log("Routing", "ACCEPTED /bets/popular\t${bet}")
                            call.respond(HttpStatusCode.Accepted, bet)
                        }
                        logManager.log("Routing", "${ApiMessage.BET_NOT_FOUND} /bets/popular")
                        call.respond(HttpStatusCode.NotFound, ApiMessage.BET_NOT_FOUND)
                    }
                }
            }
        }

        get("/bets/get/{id}", {
            description = "Retrieves a specific bet"
            request {
                pathParameter<String>(ApiMessage.ID_BET_INFO)
            }
            response {
                HttpStatusCode.Accepted to {
                    description = "The bet is available"
                    body<Bet> {
                        description = "Desired bet"
                    }
                }
                HttpStatusCode.NotFound to {
                    description = ApiMessage.BET_NOT_FOUND_INFO
                    body(ApiMessage.BET_NOT_FOUND)
                }
            }
        }) {
            logManager.log("Routing", "GET /bets/get/{id}")
            val id = call.parameters["id"] ?: ""
            betDataSource.getBetById(id)?.let { bet ->
                logManager.log("Routing", "ACCEPTED /bets/get/{id}\t ${bet}")
                call.respond(HttpStatusCode.Accepted, bet)
            } ?: logManager.log("Routing", "${ApiMessage.BET_NOT_FOUND} /bets/get/{id}")
            call.respond(HttpStatusCode.NotFound, ApiMessage.BET_NOT_FOUND)
        }

        post("/bets/delete", {
            description = "Delete a specific bet"
            request {
                body<Map<String, String>> {
                    description = ApiMessage.ID_BET_INFO
                }
            }
            response {
                HttpStatusCode.Accepted to {
                    description = "The bet has been deleted"
                }
                HttpStatusCode.NotFound to {
                    description = ApiMessage.BET_NOT_FOUND_INFO
                    body(ApiMessage.BET_NOT_FOUND)
                }
            }
        }) {
            logManager.log("Routing", "POST /bets/delete")
            val id = call.receive<Map<String, String>>()["id"] ?: ""
            if (betDataSource.removeBet(id)) {
                logManager.log("Routing", "ACCEPTED /bets/delete")
                call.respond(HttpStatusCode.Accepted)
            } else {
                logManager.log("Routing", "${ApiMessage.BET_NOT_FOUND} /bets/delete")
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
                    description = ApiMessage.BET_NOT_FOUND_INFO
                    body(ApiMessage.BET_NOT_FOUND)
                }
            }
        }) {
            logManager.log("Routing", "POST /bets/update")
            val updatedBetData = call.receive<UpdatedBetData>()
            if (betDataSource.updateBet(updatedBetData)) {
                logManager.log("Routing", "ACCEPTED /bets/delete")
                call.respond(HttpStatusCode.Accepted)
            } else {
                logManager.log("Routing", "${ApiMessage.BET_NOT_FOUND} /bets/delete")
                call.respond(HttpStatusCode.NotFound, ApiMessage.BET_NOT_FOUND)
            }
        }

        authenticate {
            get("/bets/toConfirm", {
                description = "Allows a user to know which bets can be validated"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of bets that can be validated is available"
                        body<List<BetDetail>> {
                            description = "list of bets that can be validated"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "GET /bets/toConfirm")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val response = betDataSource.getToConfirm(user)
                        logManager.log("Routing", "ACCEPTED /bets/toConfirm\t${response}")
                        call.respond(HttpStatusCode.Accepted, response)
                    }
                }
            }
        }

        authenticate {
            get("/bets/getWon", {
                description = "Allows a user to know their won bets"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of won bets is available"
                        body<List<BetResultDetail>> {
                            description = "List of won bets"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "GET /bets/getWon")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        logManager.log("Routing", "ACCEPTED /bets/getWon")
                        call.respond(HttpStatusCode.Accepted, betDataSource.getWonNotifications(user.username))
                    }
                }
            }
        }

        authenticate {
            get("/bets/history", {
                description = "Allows a user to know own history of bets"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "Bet history is available"
                        body<List<BetResultDetail>> {
                            description = "Betting history list"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "GET /bets/history")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        logManager.log(
                            "Routing",
                            "ACCEPTED /bets/toConfirm\t${betDataSource.getHistory(user.username)}"
                        )
                        call.respond(HttpStatusCode.Accepted, betDataSource.getHistory(user.username))
                    }
                }
            }
        }

        authenticate {
            get("/bets/current", {
                description = "Allows a user to know current bets"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
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
                logManager.log("Routing", "GET /bets/current")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        logManager.log(
                            "Routing",
                            "ACCEPTED /bets/toConfirm\t${betDataSource.getCurrent(user.username)}"
                        )
                        call.respond(HttpStatusCode.Accepted, betDataSource.getCurrent(user.username))
                    }
                }
            }
        }

        authenticate {
            post("/bets/confirm/{id}", {
                description = "allows the creator of a bet to confirm the final answer"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    pathParameter<String>(ApiMessage.ID_BET_INFO)
                    body<String> {
                        description = "Final answer of the bet"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "The final answer has been set"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = ApiMessage.NOT_CREATOR_INFO
                    }
                }
            }) {
                logManager.log("Routing", "GET /bets/confirm/{id}")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val betId = call.parameters["id"] ?: ""
                        val result = call.receive<String>()

                        if (betDataSource.getBetById(betId)?.createdBy == user.username) {
                            betDataSource.confirmBet(betId, result)
                            logManager.log("Routing", "ACCEPTED /bets/confirm/{id} $result")
                            call.respond(HttpStatusCode.OK)
                        } else {
                            logManager.log("Routing", "UNAUTHORIZED /bets/confirm/{id}")
                            call.respond(HttpStatusCode.Unauthorized)
                        }
                    }
                }
            }
            post("/bets/users", {
                description = "gets all userDTO of a bet"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    pathParameter<String>(ApiMessage.ID_BET_INFO)
                    body<List<UserDTO>> {
                        description = "UserDTO of the bet"
                    }
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "List of 4 user of the selected bet"
                    }
                }
            }) {
                logManager.log("Routing", "POST /bets/users")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, _ ->
                        val id = call.receive<Map<String, String>>()["id"] ?: ""
                        val participations = participationDataSource.getParticipationFromBetId(id)
                        val users =
                            participations.map { userDataSource.getUserByUsername(it.username).first }.toSet().take(4)
                                .toList()
                        call.respond(HttpStatusCode.Accepted, users)
                    }
                }
            }
            post("/bets/pvbet/update", {
                description = "Add new users to a private bet"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    body<UpdatedPrivateBet> {
                        description = "Bet id and list of new users"
                    }
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "Invited users list updated"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = ApiMessage.NOT_CREATOR_INFO
                    }
                }
            }) {
                logManager.log("Routing", "POST /bets/pvbet/update")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->
                        val updateRequest = call.receive<UpdatedPrivateBet>()
                        val bet = betDataSource.getBetById(updateRequest.betid)
                        if (user.username != bet?.createdBy) {
                            call.respond(HttpStatusCode.Unauthorized, ApiMessage.USER_DOESNT_HAVE_PERMISSION)
                        }
                        betDataSource.addUserInPrivatebet(updateRequest)
                        call.respond(HttpStatusCode.Accepted, updateRequest)
                    }
                }
            }
        }
    }
}