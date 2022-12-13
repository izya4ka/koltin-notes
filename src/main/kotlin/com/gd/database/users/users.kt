package com.gd.database.users

import com.gd.plugins.User
import com.gd.plugins.encrypt
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object users : Table() {
    private val name = varchar("name", 15)
    private val password = binary("password")

    fun isUsernameUnique(username: String): Boolean {
        val row = transaction { users.select { name eq username }.singleOrNull() }
        return row == null
    }

    fun isPasswordEqual(username: String, password: ByteArray): Boolean {
        val row =
            transaction { users.select { (users.password eq password) and (name eq username) }.singleOrNull() }
        return row != null
    }

    fun insertUser(user: User) {
        transaction {
            users.insert {
                it[name] = user.name
                it[password] = encrypt(user.password)
            }
        }
    }
}
