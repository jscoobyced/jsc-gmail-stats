package io.narok.jscgs.repository

import io.ktor.server.application.Application
import io.narok.jscgs.plugins.getConnection
import io.narok.jscgs.service.argon2
import io.narok.jscgs.service.verify

const val UNVERIFIED = "_unverified_"

class UserRepository {
    companion object {

        fun registerUser(username: String, password: String): Boolean {
            val connection = getConnection()
            val preparedStatement = connection.prepareStatement("INSERT INTO users(username, password) VALUES (?, ?)")
            preparedStatement.setString(1, username)
            preparedStatement.setString(2, password.argon2())
            val result = preparedStatement.executeUpdate()
            connection.close()
            return result > 0
        }


        fun unregisterUser(username: String): Boolean {
            val connection = getConnection()
            val preparedStatement = connection.prepareStatement("DELETE FROM users WHERE username = ?")
            preparedStatement.setString(1, username)
            val result = preparedStatement.executeUpdate()
            connection.close()
            return result > 0
        }

        fun verifyUser(username: String, password: String): String {
            val connection = getConnection()
            val preparedStatement = connection.prepareStatement("SELECT username, password FROM users WHERE username = ?")
            preparedStatement.setString(1, username)
            val result = preparedStatement.executeQuery()
            var verified = ""
            while(result.next()) {
                verified = UNVERIFIED
                val hashedPassword = result.getString(2)
                if(password.verify(hashedPassword)) {
                    verified = username
                }
            }
            connection.close()
            return verified
        }
    }
}

fun Application.createDatabase() {
    val connection = getConnection()
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS users(id int PRIMARY KEY AUTO_INCREMENT, username VARCHAR(64), password VARCHAR(255))")
    connection.close()
}