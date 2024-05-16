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

val RegexCheckerUser = AppConfig.regexChecker
val CryptManagerUser = AppConfig.cryptManager
val tokenManagerUser = AppConfig.tokenManager
const val DEFAULT_COINS = 500


fun Application.userRouter() {

    val userDataSource = this.dataSource.userDataSource

    routing {
        post("/users/register", {
            description = "Allows a user to register"
            request {
                body<UserRequest> {
                    description = "User information"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "User created"
                    body<User> {
                        description = "The new user"
                    }
                }
                HttpStatusCode.Conflict to {
                    description = "Email or username already taken"
                    body(ApiMessage.USER_ALREADY_EXISTS)
                }
                HttpStatusCode.Forbidden to {
                    description = "Email invalid"
                    body(ApiMessage.INVALID_MAIL)
                }
            }
        }) {
            val tempUser = call.receive<UserRequest>()
            if (RegexCheckerUser.isEmailInvalid(tempUser.email)) {
                call.respond(HttpStatusCode.Forbidden, ApiMessage.INVALID_MAIL)
            }
            if (userDataSource.userExists(tempUser.username)) {
                call.respond(HttpStatusCode.Conflict, ApiMessage.USER_ALREADY_EXISTS)
            }
            if(userDataSource.emailExists(tempUser.email)){
                call.respond(HttpStatusCode.Conflict, ApiMessage.MAIL_ALREADY_EXISTS)
            }
            val user = User(
                id = UUID.randomUUID().toString(),
                username = tempUser.username,
                email = tempUser.email,
                password = tempUser.password,
                nbCoins = DEFAULT_COINS,
                token = null
            )
            CryptManagerUser.passwordCrypt(user)
            user.token = tokenManagerUser.generateOrReplaceJWTToken(user)
            userDataSource.addUser(user)
            call.respond(HttpStatusCode.Created, user)
        }

        post("/users/login", {
            description = "Allows a user to login"
            request {
                body<CheckUser> {
                    description = "User information"
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "User logged in"
                    body<UserDTO>()
                }
                HttpStatusCode.NotFound to {
                    description = "Invalid credentials"
                    body(ApiMessage.USER_NOT_FOUND)
                }
            }
        }) {
            val checkUser = call.receive<CheckUser>()
            val user = userDataSource.getUserByUsername(checkUser.login)
            if (CryptManagerUser.passwordDecrypt(user.second ?: "", checkUser.password)) {
                user.first?.let { userDtoWithToken ->
                    userDtoWithToken.token = tokenManagerUser.generateOrReplaceJWTToken(userDtoWithToken)
                    call.respond(HttpStatusCode.OK, userDtoWithToken)
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.USER_NOT_FOUND)
            } else {
                call.respond(HttpStatusCode.NotFound, ApiMessage.INCORRECT_LOGIN_PASSWORD)
            }
        }

        authenticate {
            post("/users/delete", {
                description = "Allow you to delete your account"

                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                    body<CheckUser> {
                        description = "User information"
                    }
                }
                response {
                    HttpStatusCode.InternalServerError to {
                        description = "User can't be delete"
                        body(ApiMessage.USER_CANT_BE_DELETE)
                    }
                    HttpStatusCode.Accepted to {
                        body<String> {
                            description = "Password of the user"
                        }
                    }
                    HttpStatusCode.NotFound to {
                        description = "User not found"
                        body(ApiMessage.INCORRECT_LOGIN_PASSWORD)
                    }
                }

            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, password ->
                        val checkUser = call.receive<CheckUser>()
                        if (CryptManagerUser.passwordDecrypt(password, checkUser.password)) {
                            if (!userDataSource.deleteUser(checkUser.login)) {
                                call.respond(HttpStatusCode.InternalServerError, ApiMessage.USER_CANT_BE_DELETE)
                            }
                            call.respond(HttpStatusCode.Accepted, password)
                        } else {
                            call.respond(HttpStatusCode.NotFound, ApiMessage.INCORRECT_LOGIN_PASSWORD)
                        }

                    }
                }
            }

            get("/users/token", {
                description = "Allows you to retrieve the user linked to a JWT token"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the user")
                }
                response {
                    HttpStatusCode.OK to {
                        body<UserDTO> {
                            description = "Limited user information"
                        }
                    }
                }
            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { userDto, _ ->
                        call.respond(HttpStatusCode.OK, userDto)
                    }
                }
            }
            get("/users/gift", {
                description = "Allows you to collect your daily gift"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Daily gift allowed !"
                        body<Int> {
                            description = "Number of coins offered"
                        }
                    }
                    HttpStatusCode.MethodNotAllowed to {
                        description = "You can't have you daily gift now"
                        body(ApiMessage.NO_GIFT)
                    }
                }

            }) {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { userDto, _ ->
                        if (userDataSource.canHaveDailyGift(userDto.username)) {
                            val dailyGift = (DAILY_GIFT_MIN..DAILY_GIFT_MAX).random()
                            userDataSource.addCoins(userDto.username, dailyGift)
                            call.respond(HttpStatusCode.OK, dailyGift)
                        } else call.respond(HttpStatusCode.MethodNotAllowed, ApiMessage.NO_GIFT)
                    }
                }
            }
        }
    }
}
