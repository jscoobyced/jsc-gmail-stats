package io.narok.jscgs.models

import kotlinx.serialization.Serializable

@Serializable
data class EmailCountRequest(
    val email: String,
    val password: String,
    val label: String,
    val dateFrom: String,
    val dateTo: String
)
