package com.gd.features.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gd.database.users.users
import com.gd.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureLoginRouting() {
    routing {

        post("/login") {
            try {
                val user = call.receive<User>()
                if (!users.isPasswordEqual(user.name, encrypt(user.password))) call.respond(
                    HttpStatusCode.NotFound,
                    "Password incorrect"
                )
                else {
                    val token = JWT.create()
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .withClaim("name", user.name)
                        .withExpiresAt(Date(System.currentTimeMillis() + 36000000))
                        .sign(Algorithm.HMAC256(secret))
                    call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
                }
            } catch (_: Throwable) {
                call.respond(HttpStatusCode.BadRequest, "Bad request")
            }
        }
    }
}