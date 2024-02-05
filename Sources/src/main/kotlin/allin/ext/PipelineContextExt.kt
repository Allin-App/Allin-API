package allin.ext

import allin.data.UserDataSource
import allin.dto.UserDTO
import allin.model.ApiMessage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*


suspend fun PipelineContext<*, ApplicationCall>.hasToken(content: suspend (principal: JWTPrincipal) -> Unit) =
    call.principal<JWTPrincipal>()?.let { content(it) } ?: call.respond(HttpStatusCode.Unauthorized)


suspend fun PipelineContext<*, ApplicationCall>.verifyUserFromToken(
    userDataSource: UserDataSource,
    principal: JWTPrincipal,
    content: suspend (user: UserDTO, password: String) -> Unit
) {
    val username = principal.payload.getClaim("username").asString()
    val userPassword = userDataSource.getUserByUsername(username)
    userPassword.first?.let { content(it, userPassword.second ?: "") }
        ?: call.respond(HttpStatusCode.NotFound, ApiMessage.TokenUserNotFound)
}