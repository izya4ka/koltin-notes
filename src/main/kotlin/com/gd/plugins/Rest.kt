package com.gd.plugins

import com.gd.database.tasks.RawTask
import com.gd.database.tasks.tasks
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRest() {

    routing {
        authenticate("auth-jwt") {
            route("/api/notes") {
                post {
                    try {
                        val received = call.receive<RawTask>()
                        val username = call.principal<JWTPrincipal>()!!.payload.getClaim("name").asString()
                        tasks.insertTask(received, username)
                        call.respond(HttpStatusCode.Created, "Task created")
                    } catch (e: Throwable) {
                        call.respond(HttpStatusCode.BadRequest, "Bad request")
                    }
                }
                get {
                    val username: String = call.principal<JWTPrincipal>()!!.payload.getClaim("name").asString()
                    call.respond(tasks.getAllTasksForUsername(username))
                }

                delete("/{id}") {
                    val username: String = call.principal<JWTPrincipal>()!!.payload.getClaim("name").asString()
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    if (tasks.isTaskExist(id!!, username)) tasks.deleteTaskByID(id, username) else call.respond(
                        HttpStatusCode.NotFound,
                        "Task not found"
                    )
                    call.respond(HttpStatusCode.OK, "Task deleted")
                }
            }
        }
    }
}