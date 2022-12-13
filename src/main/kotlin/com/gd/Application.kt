package com.gd

import com.gd.features.login.configureLoginRouting
import com.gd.features.register.configureRegisterRouting
import com.gd.plugins.configureRest
import com.gd.plugins.configureRouting
import com.gd.plugins.configureSecurity
import com.gd.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

fun main() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/base",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "1234"
    )

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureRest()
}
