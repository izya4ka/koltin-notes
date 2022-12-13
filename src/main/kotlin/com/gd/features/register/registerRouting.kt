package com.gd.features.register

import com.gd.database.users.users
import com.gd.plugins.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRegisterRouting() {
    routing {
        post("/register") {
            val user = call.receive<User>()
            if (user.name.length > 15) call.respond(
                HttpStatusCode.BadRequest,
                "Username must be not longer 15 characters"
            )
            if (!users.isUsernameUnique(user.name)) call.respond(
                HttpStatusCode.Conflict,
                "Username is occupied"
            )
            users.insertUser(user)
            call.respond(HttpStatusCode.OK, "User registered")
        }
    }
}