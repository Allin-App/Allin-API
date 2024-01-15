package allin.ext

import allin.model.ApiMessage
import allin.model.User
import allin.routing.users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.hasToken(content: suspend (principal: JWTPrincipal) -> Unit) =
    call.principal<JWTPrincipal>()?.let { content(it) } ?: call.respond(HttpStatusCode.Unauthorized)

suspend fun PipelineContext<*, ApplicationCall>.verifyUserFromToken(
    principal: JWTPrincipal,
    content: suspend (user: User) -> Unit
) {
    val username = principal.payload.getClaim("username").asString()
    users.find { it.username == username }?.let { content(it) }
        ?: call.respond(HttpStatusCode.NotFound, ApiMessage.TokenUserNotFound)
}