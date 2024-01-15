package allin.routing

import allin.model.ApiMessage
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.BasicRouting() {
    routing {
        get("/") {
            call.respond(ApiMessage.Welcome)
        }
    }
}