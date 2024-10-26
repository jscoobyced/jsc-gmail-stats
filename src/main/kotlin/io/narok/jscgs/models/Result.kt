package io.narok.jscgs.models

import kotlinx.serialization.Serializable

@Serializable
data class Result(val success: Boolean = true, val message: String? = null, val errorCode: ErrorCode = ErrorCode.OK)

enum class ErrorCode(val value: Int) {
    OK(0),
    LABEL_NOT_FOUND(1),
    USER_NOT_REGISTERED(2)
}