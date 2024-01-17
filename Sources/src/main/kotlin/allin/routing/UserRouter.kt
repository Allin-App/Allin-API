package allin.routing

import allin.entities.UsersEntity.addUserEntity
import allin.entities.UsersEntity.deleteUserByUsername
import allin.entities.UsersEntity.getUserByUsernameAndPassword
import allin.entities.UsersEntity.getUserToUserDTO
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
                val users = getUserToUserDTO()
                users.find { it.username == tempUser.username || it.email == tempUser.email }?.let { _ ->
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
                    addUserEntity(user)
                    call.respond(HttpStatusCode.Created, user)
                }
            }
        }

        route("/users/login") {
            post {
                val checkUser = call.receive<CheckUser>()
                val user = getUserByUsernameAndPassword(checkUser.login)
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
                    verifyUserFromToken(principal) { _, password ->
                        val checkUser = call.receive<CheckUser>()

                        if (CryptManagerUser.passwordDecrypt(password, checkUser.password)) {
                            if (!deleteUserByUsername(checkUser.login)) {
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
                    verifyUserFromToken(principal) { userDto, _ ->
                        call.respond(HttpStatusCode.OK, userDto)
                    }
                }
            }
        }
    }
}
