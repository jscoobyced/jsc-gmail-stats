package io.narok.jscgs.repository

import io.ktor.server.application.Application
import io.narok.jscgs.plugins.getConnection

class UserRepository {
    companion object {

        fun registerUser(username: String, password: String): Boolean {
            val connection = getConnection()
            val preparedStatement = connection.prepareStatement("INSERT INTO users(username, password) VALUES (?, ?)")
            preparedStatement.setString(1, username)
            preparedStatement.setString(2, password)
            val result = preparedStatement.executeUpdate()
            connection.close()
            return result > 0
        }

        fun showUsers() {
            val connection = getConnection()
            val statement = connection.createStatement()
            val result = statement.executeQuery("SELECT username FROM users")
            while(result.next()) {
                println(result.getString("username"))
            }
            connection.close()
        }
    }
}

fun Application.createDatabase() {
    val connection = getConnection()
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS users(id int PRIMARY KEY AUTO_INCREMENT, username VARCHAR(64), password VARCHAR(64))")
    connection.close()
}