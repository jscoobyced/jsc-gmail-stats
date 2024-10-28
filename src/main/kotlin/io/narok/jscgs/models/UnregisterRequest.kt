package io.narok.jscgs.models

import kotlinx.serialization.Serializable

@Serializable
data class UnregisterRequest(val email: String)
