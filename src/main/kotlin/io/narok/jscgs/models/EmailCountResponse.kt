package io.narok.jscgs.models

import kotlinx.serialization.Serializable

@Serializable
data class EmailCountResponse(val emailCount: EmailCount?, val result: Result)
