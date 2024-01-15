package allin.routing

import allin.dto.convertUserToUserDTO
import allin.dto.convertUserToUserDTOToken
import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.ApiMessage
import allin.model.CheckUser
import allin.model.User
import allin.model.UserRequest
import allin.utils.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

val users = mutableListOf<User>()
val RegexCheckerUser = AppConfig.regexChecker
val CryptManagerUser = AppConfig.cryptManager
val tokenManagerUser = AppConfig.tokenManager
const val DEFAULT_COINS = 500

fun Application.UserRouter() {

    routing {
        route("/users/register") {
            post {
                val tempUser = call.receive<UserRequest>()
                if (RegexCheckerUser.isEmailInvalid(tempUser.email)) {
                    call.respond(HttpStatusCode.Forbidden, ApiMessage.InvalidMail)
                }
                users.find { it.username == tempUser.username || it.email == tempUser.email }?.let { user ->
                    call.respond(HttpStatusCode.Conflict, ApiMessage.UserAlreadyExist)
                } ?: run {
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
                    users.add(user)
                    call.respond(HttpStatusCode.Created, user)
                }
            }
        }

        route("/users/login") {
            post {
                val checkUser = call.receive<CheckUser>()
                users.find { it.username == checkUser.login || it.email == checkUser.login }?.let { user ->
                    if (CryptManagerUser.passwordDecrypt(user, checkUser.password)) {
                        user.token = tokenManagerUser.generateOrReplaceJWTToken(user)
                        call.respond(HttpStatusCode.OK, convertUserToUserDTOToken(user))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiMessage.IncorrectLoginPassword)
                    }
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.IncorrectLoginPassword)
            }
        }

        authenticate {
            post("/users/delete") {
                hasToken { principal ->
                    verifyUserFromToken(principal) { user ->
                        val checkUser = call.receive<CheckUser>()
                        if (user.username == checkUser.login && user.password == checkUser.password) {
                            users.remove(user)
                            call.respond(HttpStatusCode.Accepted, convertUserToUserDTO(user))
                        } else {
                            call.respond(HttpStatusCode.NotFound, ApiMessage.IncorrectLoginPassword)
                        }
                    }
                }
            }
            
            get("/users/token") {
                hasToken { principal ->
                    verifyUserFromToken(principal) { user ->
                        call.respond(HttpStatusCode.OK, convertUserToUserDTO(user))
                    }
                }
            }
        }
    }
}
