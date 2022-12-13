package com.gd.database.tasks

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object tasks : IntIdTable() {
    private val label = varchar("label", 80)
    private val text = varchar("text", 400).nullable()
    private val username = varchar("username", 15)

    fun getAllTasksForUsername(usernameReceived: String): List<Task> {
        val tasksTransaction = transaction {
            tasks.select { username eq usernameReceived }
                .map { Task(it[tasks.id].value, it[label], it[text]) }
        }
        return tasksTransaction
    }

    fun insertTask(task: RawTask, usernameReceived: String) {
        transaction {
            tasks.insert {
                it[label] = task.label
                it[text] = task.text
                it[username] = usernameReceived
            }
        }
    }

    fun deleteTaskByID(idReceived: Int, usernameReceived: String) {
        transaction {
            tasks.deleteWhere { (username eq usernameReceived) and (tasks.id eq idReceived) }
        }
    }

    fun isTaskExist(idReceived: Int, usernameReceived: String): Boolean {
        val task = transaction {
            tasks.select { (username eq usernameReceived) and (tasks.id eq idReceived) }.singleOrNull()
        }
        return task != null
    }
}

