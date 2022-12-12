package com.gd.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val label: String,
    val text: String,
    val username: String,
)

@Serializable
data class RawTask (
    val label: String,
    val text: String,
)

fun Application.configureRest() {
    val tasks: MutableList<Task> = mutableListOf()

    routing {
        authenticate("auth-jwt") {
            route("/api/notes") {
                post {
                    try {
                        val received: RawTask = call.receive<RawTask>()
                        val ids: List<Int> = tasks.map { it.id }
                        val maxId: Int = ids.maxOrNull() ?: 0
                        val username: String = call.principal<JWTPrincipal>()!!.payload.getClaim("name").asString()
                        tasks.add(Task(maxId + 1, received.label, received.text, username))
                        call.respond(HttpStatusCode.Created, "Task created")
                    } catch (e: Throwable) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid object")
                    }
                }
                get {
                    val username: String = call.principal<JWTPrincipal>()!!.payload.getClaim("name").asString()
                    call.respond(tasks.filter {it.username == username})
                }

                delete("/{id}") {
                    val username: String = call.principal<JWTPrincipal>()!!.payload.getClaim("name").asString()
                    val id = call.parameters["id"]?.toIntOrNull() ?: call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    val toDelete = tasks.find {(it.username == username) && (it.id == id)} ?: call.respond(HttpStatusCode.NotFound, "Task not found")
                    tasks.remove(toDelete)
                    call.respond(HttpStatusCode.OK, "Note deleted")
                }
            }
        }
    }
}