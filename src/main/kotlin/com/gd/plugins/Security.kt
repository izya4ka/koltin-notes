package com.gd.plugins

import java.util.*
import io.ktor.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*

@Serializable
data class User(val name: String, val password: String)

data class UserEncrypted(val name: String, val password: ByteArray)
val users: MutableList<UserEncrypted> = mutableListOf()

const val secret = "secret" // Change me!
const val issuer = "http://0.0.0.0:8080/"
const val audience = "http://0.0.0.0:8080/api"
const val myRealm = "Access to API"

val encrypt = getDigestFunction("SHA-256") { "secret_salt${it.length}" } // Change me!

fun Application.configureSecurity() {

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())
            validate { credential ->
                if (users.map {it.name}.contains(credential.payload.getClaim("name").asString())) {
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

    routing {

        post("/login") {
            val user = call.receive<User>()
            if(!users.map {it.name}.contains(user.name)) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else if (!(users.find {user.name == it.name}!!.password.contentEquals(encrypt(user.password)))) {
                call.respond(HttpStatusCode.NotFound, "Password incorrect")
            } else {
                val token = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withClaim("name", user.name)
                    .withExpiresAt(Date(System.currentTimeMillis() + 36000000))
                    .sign(Algorithm.HMAC256(secret))
                call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
            }
        }

        post("/register") {
            val user = call.receive<User>()
            if(users.map {it.name}.contains(user.name)) {
                call.respond(HttpStatusCode.Conflict, "User already exist")
            }
            users.add(UserEncrypted(user.name, encrypt(user.password)))
            call.respond(HttpStatusCode.Accepted, "Register completed!")
        }

    }
}
