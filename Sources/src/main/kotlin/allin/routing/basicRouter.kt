package allin.routing

import allin.model.ApiMessage
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File
import java.util.*

@Serializable
data class ImageResponse(val url: String)

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
        post("/image") {
            val base64Image = call.receiveText()
            val imageBytes = Base64.getDecoder().decode(base64Image)
            val fileName = "${UUID.randomUUID()}.png"
            val file = File("uploads/$fileName")

            file.parentFile.mkdirs()
            file.writeBytes(imageBytes)

            val imageUrl = "https://codefirst.iut.uca.fr/containers/AllDev-api/images/$fileName"
            call.respond(HttpStatusCode.OK, ImageResponse(imageUrl))
        }

        get("/images/{fileName}") {
            val fileName = call.parameters["fileName"]
            val file = File("uploads/$fileName")

            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "File not found")
            }
        }
    }
}