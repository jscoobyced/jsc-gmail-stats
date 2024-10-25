package io.narok.jscgs.models

import kotlinx.serialization.Serializable

@Serializable
data class EmailCount(val emailCount: Int, val dateFrom: String, val dateTo: String)
