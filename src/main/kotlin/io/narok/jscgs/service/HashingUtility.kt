package io.narok.jscgs.service

import de.mkammerer.argon2.Argon2Factory

private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64)

fun String.argon2(): String = argon2.hash(3, 64 * 1024, 1, this.toCharArray())

fun String.verify(hash: String): Boolean = argon2.verify(hash, this.toCharArray())
