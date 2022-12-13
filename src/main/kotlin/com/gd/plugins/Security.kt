package com.gd.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gd.database.users.users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val password: String)

const val secret = "secret" // Change me!
const val issuer = "http://0.0.0.0:8080/"
const val audience = "http://0.0.0.0:8080/api"
const val myRealm = "Access to API"
const val salt = "abc123"

val encrypt = getDigestFunction("SHA-256") { "${salt}${it.length}" } // Change me!

fun Application.configureSecurity() {

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (!users.isUsernameUnique(credential.payload.getClaim("name").asString())) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
