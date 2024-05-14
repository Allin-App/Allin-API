package allin.routing

import allin.model.ApiMessage
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.basicRouter() {
    routing {
        get("/", {
            description = "Hello World of Allin API"
            response {
                HttpStatusCode.OK to {
                    description = "Successful Request"
                }
                HttpStatusCode.InternalServerError to {
                    description = "Something unexpected happened"
                }
            }
        }) {
            call.respond(ApiMessage.WELCOME)
        }
    }
}