package allin.routing

import allin.model.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

val users = mutableListOf(User("user1", "user1@example.com", "mdp",1000))
fun Application.UserRouter() {
    routing {
        route("/users") {
            get {
                call.respond(users)
            }
            post {
                val newUser = call.receive<User>()
                users.add(newUser)
                call.respond(HttpStatusCode.Created, newUser)
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
