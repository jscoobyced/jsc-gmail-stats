package io.narok.jscgs.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email:String, val password:String)
