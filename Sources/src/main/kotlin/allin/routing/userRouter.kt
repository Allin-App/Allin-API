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
import java.io.File
import java.util.*

val RegexCheckerUser = AppConfig.regexChecker
val CryptManagerUser = AppConfig.cryptManager
val tokenManagerUser = AppConfig.tokenManager
val imageManagerUser = AppConfig.imageManager
val urlManager = AppConfig.urlManager

const val DEFAULT_COINS = 500


fun Application.userRouter() {

    val userDataSource = this.dataSource.userDataSource
    val logManager = AppConfig.logManager

    routing {
        post("/users/register", {
            description = "Allows a user to register"
            request {
                body<UserRequest> {
                    description = ApiMessage.USER_UPDATE_INFO
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
            logManager.log("Routing", "POST /users/register")
            val tempUser = call.receive<UserRequest>()
            if (RegexCheckerUser.isEmailInvalid(tempUser.email)) {
                logManager.log("Routing", "${ApiMessage.INVALID_MAIL} /users/register")
                call.respond(HttpStatusCode.Forbidden, ApiMessage.INVALID_MAIL)
            } else if (userDataSource.userExists(tempUser.username)) {
                logManager.log("Routing", "${ApiMessage.USER_ALREADY_EXISTS} /users/register")
                call.respond(HttpStatusCode.Conflict, ApiMessage.USER_ALREADY_EXISTS)
            } else if (userDataSource.emailExists(tempUser.email)) {
                logManager.log("Routing", "${ApiMessage.MAIL_ALREADY_EXISTS} /users/register")
                call.respond(HttpStatusCode.Conflict, ApiMessage.MAIL_ALREADY_EXISTS)
            } else {
                val user = User(
                    id = UUID.randomUUID().toString(),
                    username = tempUser.username,
                    email = tempUser.email,
                    password = tempUser.password,
                    nbCoins = DEFAULT_COINS,
                    token = null,
                    bestWin = 0,
                    nbFriends = 0,
                    nbBets = 0,
                )
                CryptManagerUser.passwordCrypt(user)
                user.token = tokenManagerUser.generateOrReplaceJWTToken(user)
                userDataSource.addUser(user)
                logManager.log("Routing", "ACCEPTED /users/register\t${user}")
                call.respond(HttpStatusCode.Created, user)
            }
        }

        post("/users/login", {
            description = "Allows a user to login"
            request {
                body<CheckUser> {
                    description = ApiMessage.USER_UPDATE_INFO
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
            logManager.log("Routing", "POST /users/login")
            val checkUser = call.receive<CheckUser>()
            val user = userDataSource.getUserByUsername(checkUser.login)
            if (CryptManagerUser.passwordDecrypt(user.second ?: "", checkUser.password)) {
                user.first?.let { userDtoWithToken ->
                    userDtoWithToken.token = tokenManagerUser.generateOrReplaceJWTToken(userDtoWithToken)
                    logManager.log("Routing", "ACCEPTED /users/login\t${userDtoWithToken}")
                    call.respond(HttpStatusCode.OK, userDtoWithToken)
                } ?: logManager.log("Routing", "${ApiMessage.USER_NOT_FOUND} /users/login")
                call.respond(HttpStatusCode.NotFound, ApiMessage.USER_NOT_FOUND)
            } else {
                logManager.log("Routing", "${ApiMessage.INCORRECT_LOGIN_PASSWORD} /users/login")
                call.respond(HttpStatusCode.NotFound, ApiMessage.INCORRECT_LOGIN_PASSWORD)
            }
        }

        get("/users/images/{fileName}") {
            logManager.log("Routing", "GET /users/images/{fileName}")
            val fileName = call.parameters["fileName"]
            val urlfile = "images/$fileName"
            val file = File("$urlfile.png")
            if (file.exists()) {
                call.respondFile(file)
            } else {
                val imageBytes = userDataSource.getImage(fileName.toString())
                if (imageBytes != null) {
                    imageManagerUser.saveImage(urlfile, imageBytes)
                    logManager.log("Routing", "ACCEPTED /users/images/{fileName}")
                    call.respondFile(file)
                } else {
                    logManager.log("Routing", "${ApiMessage.FILE_NOT_FOUND} /users/images/{fileName}")
                    call.respond(HttpStatusCode.NotFound, ApiMessage.FILE_NOT_FOUND)
                }
            }
        }

        authenticate {
            post("/users/delete", {
                description = "Allow you to delete your account"

                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    body<CheckUser> {
                        description = ApiMessage.USER_UPDATE_INFO
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
                logManager.log("Routing", "POST /users/delete")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { _, password ->
                        val checkUser = call.receive<CheckUser>()
                        if (CryptManagerUser.passwordDecrypt(password, checkUser.password)) {
                            if (!userDataSource.deleteUser(checkUser.login)) {
                                logManager.log("Routing", "${ApiMessage.USER_CANT_BE_DELETE} /users/delete")
                                call.respond(HttpStatusCode.InternalServerError, ApiMessage.USER_CANT_BE_DELETE)
                            }
                            logManager.log("Routing", "ACCEPTED /users/delete")
                            call.respond(HttpStatusCode.Accepted, password)
                        } else {
                            logManager.log("Routing", "${ApiMessage.INCORRECT_LOGIN_PASSWORD} /users/delete")
                            call.respond(HttpStatusCode.NotFound, ApiMessage.INCORRECT_LOGIN_PASSWORD)
                        }

                    }
                }
            }

            get("/users/token", {
                description = "Allows you to retrieve the user linked to a JWT token"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                }
                response {
                    HttpStatusCode.OK to {
                        body<UserDTO> {
                            description = "Limited user information"
                        }
                    }
                }
            }) {
                logManager.log("Routing", "GET /users/token")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { userDto, _ ->
                        logManager.log("Routing", "ACCEPTED /users/token\t${userDto}")
                        call.respond(HttpStatusCode.OK, userDto)
                    }
                }
            }
            get("/users/gift", {
                description = "Allows you to collect your daily gift"
                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
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
                logManager.log("Routing", "GET /users/gift")
                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { userDto, _ ->
                        if (userDataSource.canHaveDailyGift(userDto.username)) {
                            val dailyGift = (DAILY_GIFT_MIN..DAILY_GIFT_MAX).random()
                            userDataSource.addCoins(userDto.username, dailyGift)
                            logManager.log("Routing", "ACCEPTED /users/gift\t${dailyGift}")
                            call.respond(HttpStatusCode.OK, dailyGift)
                            logManager.log("Routing", "${ApiMessage.NO_GIFT} /users/gift")
                        } else call.respond(HttpStatusCode.MethodNotAllowed, ApiMessage.NO_GIFT)
                    }
                }
            }

            post("/users/images", {
                description = "Allow you to add a profil image"

                request {
                    headerParameter<JWTPrincipal>(ApiMessage.JWT_TOKEN_INFO)
                    body<CheckUser> {
                        description = ApiMessage.USER_UPDATE_INFO
                    }
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "Image added"
                    }
                    HttpStatusCode.NotFound to {
                        description = "User not found"
                        body(ApiMessage.INCORRECT_LOGIN_PASSWORD)
                    }
                }

            }) {
                logManager.log("Routing", "POST /users/images")

                hasToken { principal ->
                    verifyUserFromToken(userDataSource, principal) { user, _ ->

                        val base64Image = call.receiveText()

                        val urlfile = "images/${user.id}"
                        val imageByteArray = imageManagerUser.saveImage(urlfile, base64Image)
                        if (imageByteArray != null && imageByteArray.isNotEmpty()) {
                            userDataSource.removeImage(user.id)
                            userDataSource.addImage(user.id, imageByteArray)
                            logManager.log("Routing", "ACCEPTED /users/images")
                            call.respond(HttpStatusCode.OK, "${urlManager.getURL()}users/${urlfile}")
                        }
                        logManager.log("Routing", "${ApiMessage.FILE_NOT_FOUND} /users/images")
                        call.respond(HttpStatusCode.Conflict, ApiMessage.FILE_NOT_FOUND)
                    }
                }
            }

        }
    }
}
