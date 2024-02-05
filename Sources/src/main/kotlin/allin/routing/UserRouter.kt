package allin.routing

import allin.dataSource
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.*
import allin.utils.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

val RegexCheckerUser = AppConfig.regexChecker
val CryptManagerUser = AppConfig.cryptManager
val tokenManagerUser = AppConfig.tokenManager
const val DEFAULT_COINS = 500


fun Application.UserRouter() {

    val userDataSource = this.dataSource.userDataSource

    routing {
        route("/users/register") {
            post {
                val tempUser = call.receive<UserRequest>()
                if (RegexCheckerUser.isEmailInvalid(tempUser.email)) {
                    call.respond(HttpStatusCode.Forbidden, ApiMessage.InvalidMail)
                }
                if (userDataSource.userExists(tempUser.username, tempUser.email)) {
                    call.respond(HttpStatusCode.Conflict, ApiMessage.UserAlreadyExist)
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
        }

        route("/users/login") {
            post {
                val checkUser = call.receive<CheckUser>()
                val user = userDataSource.getUserByUsername(checkUser.login)
                if (CryptManagerUser.passwordDecrypt(user.second ?: "", checkUser.password)) {
                    user.first?.let { userDtoWithToken ->
                        userDtoWithToken.token = tokenManagerUser.generateOrReplaceJWTToken(userDtoWithToken)
                        call.respond(HttpStatusCode.OK, userDtoWithToken)
                    } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.UserNotFound)
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiMessage.IncorrectLoginPassword)
                }
            }
        }

        authenticate {
            post("/users/delete") {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, password ->
                        val checkUser = call.receive<CheckUser>()
                        if (CryptManagerUser.passwordDecrypt(password, checkUser.password)) {
                            if (!userDataSource.deleteUser(checkUser.login)) {
                                call.respond(HttpStatusCode.InternalServerError, "This user can't be delete now !")
                            }
                            call.respond(HttpStatusCode.Accepted, password)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Login and/or password incorrect.")
                        }

                    }
                }
            }

            get("/users/token") {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { userDto, _ ->
                        call.respond(HttpStatusCode.OK, userDto)
                    }
                }
            }
            get("/users/gift") {
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { userDto, _ ->
                        if (userDataSource.canHaveDailyGift(userDto.username)) {
                            val dailyGift = getDailyGift()
                            userDataSource.addCoins(userDto.username, dailyGift)
                            call.respond(HttpStatusCode.OK, dailyGift)
                        } else call.respond(HttpStatusCode.MethodNotAllowed, "Can't get daily gift.")
                    }
                }
            }
        }
    }
}
