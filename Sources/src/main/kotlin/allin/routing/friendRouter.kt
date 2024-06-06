package allin.routing

import allin.dataSource
import allin.dto.UserDTO
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.ApiMessage
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

fun Application.friendRouter() {

    val userDataSource = this.dataSource.userDataSource
    val friendDataSource = this.dataSource.friendDataSource
    val logManager = AppConfig.logManager

    routing {
        authenticate {
            get("/friends/gets", {
                description = "Allows you to recover all friends of a JWT Token"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of friends is available"
                        body<List<UserDTO>> {
                            description = "List of friends"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "GET /friends/gets")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, _ ->
                        val username = tokenManagerBet.getUsernameFromToken(principal)
                        val user = userDataSource.getUserByUsername(username).first
                        logManager.log(
                            "Routing",
                            "ACCEPTED /friends/gets\t${friendDataSource.getFriendFromUserId(user?.id.toString())}"
                        )
                        call.respond(HttpStatusCode.Accepted, friendDataSource.getFriendFromUserId(user?.id.toString()))
                    }
                }

            }
            get("/friends/requests", {
                description = "Allows you to recover all friend requests of a JWT Token"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "The list of friend requests is available"
                        body<List<UserDTO>> {
                            description = "List of friend requests"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "GET /friends/requests")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, _ ->
                        val username = tokenManagerBet.getUsernameFromToken(principal)
                        val user = userDataSource.getUserByUsername(username).first
                        logManager.log(
                            "Routing", "ACCEPTED /friends/requests\t${
                                friendDataSource.getFriendRequestsFromUserId(user?.id.toString())
                            }"
                        )
                        call.respond(
                            HttpStatusCode.Accepted,
                            friendDataSource.getFriendRequestsFromUserId(user?.id.toString())
                        )
                    }
                }

            }
            post("/friends/add", {
                description = "Allows a user to add a friend"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    body<String> {
                        description = "User to add in the friends list"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "the friend has been added"
                        body<String> {
                            description = "Friend with assigned id"
                        }
                    }
                    HttpStatusCode.Conflict to {
                        description = "Friend already exist in the friends list"
                        body(ApiMessage.FRIENDS_ALREADY_EXISTS)
                    }
                }
            }) {
                logManager.log("Routing", "POST /friends/add")
                hasToken { principal ->
                    val requestMap = call.receive<Map<String, String>>()
                    val usernameFriend = requestMap["username"] ?: return@hasToken call.respond(
                        HttpStatusCode.BadRequest,
                        "Username is missing"
                    )
                    val username = tokenManagerBet.getUsernameFromToken(principal)

                    val user = userDataSource.getUserByUsername(username).first
                    val userFriend = userDataSource.getUserByUsername(usernameFriend).first

                    if (user == null || userFriend == null) {
                        logManager.log("Routing", "${ApiMessage.USER_NOT_FOUND} /friends/add")
                        call.respond(HttpStatusCode.Conflict, ApiMessage.USER_NOT_FOUND)
                    } else if (userFriend == user) {
                        logManager.log("Routing", "${ApiMessage.FRIENDS_REQUEST_HIMSELF} /friends/add")
                        call.respond(HttpStatusCode.Conflict, ApiMessage.FRIENDS_REQUEST_HIMSELF)
                    } else {
                        val friendlist = friendDataSource.getFriendFromUserId(user.id)
                        if (friendlist.map { it.id }.contains(userFriend.id)) {
                            logManager.log("Routing", "${ApiMessage.FRIENDS_ALREADY_EXISTS} /friends/add")
                            call.respond(HttpStatusCode.Conflict, ApiMessage.FRIENDS_ALREADY_EXISTS)
                        } else {
                            logManager.log("Routing", "ACCEPTED /friends/add\t${usernameFriend}")
                            friendDataSource.addFriend(user.id, userFriend.id)
                            call.respond(HttpStatusCode.Created, usernameFriend)
                        }
                    }
                }
            }
            post("/friends/delete", {
                description = "Allows a user to delete a friend"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    body<String> {
                        description = "User to delete in the friends list"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "the friend has been delete"
                        body<String> {
                            description = "Friend with assigned id"
                        }
                    }
                    HttpStatusCode.Conflict to {
                        description = "Friend doesn't exist in the friends list"
                        body(ApiMessage.FRIENDS_DOESNT_EXISTS)
                    }
                }
            }) {
                logManager.log("Routing", "POST /friends/delete")
                hasToken { principal ->
                    val requestMap = call.receive<Map<String, String>>()
                    val usernameFriend = requestMap["username"] ?: return@hasToken call.respond(
                        HttpStatusCode.BadRequest,
                        "Username is missing"
                    )
                    val username = tokenManagerBet.getUsernameFromToken(principal)

                    val user = userDataSource.getUserByUsername(username).first
                    val userFriend = userDataSource.getUserByUsername(usernameFriend).first

                    if (user == null || userFriend == null) {
                        logManager.log("Routing", "${ApiMessage.USER_NOT_FOUND} /friends/delete")
                        call.respond(HttpStatusCode.Conflict, ApiMessage.USER_NOT_FOUND)
                    } else {
                        if (friendDataSource.deleteFriend(user.id, userFriend.id)) {
                            logManager.log("Routing", "ACCEPTED /friends/delete\t${usernameFriend}")
                            call.respond(HttpStatusCode.Created, usernameFriend)
                        } else {
                            logManager.log("Routing", "${ApiMessage.FRIENDS_DOESNT_EXISTS} /friends/delete")
                            call.respond(HttpStatusCode.Conflict, ApiMessage.FRIENDS_DOESNT_EXISTS)
                        }
                    }
                }

            }

            get("/friends/search/{search}", {
                description = "Search for users based on username"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    pathParameter<String>("Search string")
                }
                response {
                    HttpStatusCode.OK to {
                        body<List<UserDTO>> {
                            description = "Filtered users."
                        }
                    }
                }

            }) {
                logManager.log("Routing", "GET /friends/search/{search}")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { userDto, _ ->
                        val users = friendDataSource.filterUsersByUsername(
                            fromUserId = userDto.id,
                            search = call.parameters["search"] ?: ""
                        )
                        logManager.log("Routing", "ACCEPTED /friends/search/{search}\t${users}")
                        call.respond(HttpStatusCode.OK, users)
                    }
                }
            }

        }
    }
}