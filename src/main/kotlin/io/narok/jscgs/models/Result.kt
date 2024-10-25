package io.narok.jscgs.models

import kotlinx.serialization.Serializable

@Serializable
data class Result(val success: Boolean, val message: String?)
