package allin.routing

import allin.model.CheckUser
import allin.model.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

val users = mutableListOf<User>()

fun Application.UserRouter() {
    routing {
        route("/users") {
            get {
                call.respond(users)
            }
        }

        route("/users/register"){
            post {
                val TempUser = call.receive<User>()
                val user = users.find { it.username == TempUser.username || it.email == TempUser.email }
                if(user == null) {
                    users.add(TempUser)
                    call.respond(HttpStatusCode.Created, TempUser)
                }
                call.respond(HttpStatusCode.Conflict)
            }
        }

        route("/users/login") {
            post {
                val checkUser = call.receive<CheckUser>()
                val user = users.find { it.username == checkUser.login || it.email == checkUser.login }
                if (user != null && user.password == checkUser.password) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }


        route("/users/{username}") {
            get {
                val username = call.parameters["username"]
                val user = users.find { it.username == username }
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            put {
                val username = call.parameters["username"]
                val userIndex = users.indexOfFirst { it.username == username }
                if (userIndex != -1) {
                    val updatedUser = call.receive<User>()
                    users[userIndex] = updatedUser
                    call.respond(updatedUser)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            delete {
                val username = call.parameters["username"]
                val user = users.find { it.username == username }
                if (user != null) {
                    users.remove(user)
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
// REGISTER 201 created 400 bad request
// LOGIN 200 OK 404
