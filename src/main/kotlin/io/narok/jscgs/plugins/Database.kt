package io.narok.jscgs.plugins

import java.sql.Connection
import java.sql.DriverManager

private val DB_PATH = System.getenv("DATABASE_PATH") ?: "./data"
private val DB_NAME = System.getenv("DATABASE_NAME") ?: "registration"
private val DB_USER = System.getenv("DATABASE_USER") ?: "root"
private val DB_PASSWORD = System.getenv("DATABASE_PASSWORD") ?: "root"

fun getConnection(): Connection {
    return DriverManager.getConnection("jdbc:h2:file:$DB_PATH/$DB_NAME", DB_USER, DB_PASSWORD)
}