package io.narok.jscgs.service

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import io.narok.jscgs.exception.UserNotFoundException
import io.narok.jscgs.models.EmailCount
import io.narok.jscgs.models.EmailCountResponse
import io.narok.jscgs.models.ErrorCode
import io.narok.jscgs.models.Result

private const val MESSAGE_PER_CALL = 25L
private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
private const val APPLICATION_NAME = "JSC G Application"

class GmailExtractor {

    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

    fun extract(
        username: String,
        password: String,
        labelName: String,
        dateFrom: String,
        dateTo: String
    ): EmailCountResponse {
        val safeUserName = username.filter { it.isLetter() }
        val credential = Credentials.getToken(safeUserName, httpTransport)

        if (credential === null) {
            throw UserNotFoundException("User is not yet registered.")
        }

        if (username.isBlank()) {
            throw IllegalArgumentException("Email is required.")
        }

        if (password.isBlank()) {
            throw IllegalArgumentException("Password is required.")
        }

        if (dateFrom.isBlank() || dateTo.isBlank()) {
            throw IllegalArgumentException("Please provide date period.")
        }

        val service = Gmail.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()

        val user = "me"
        val labelId = getLabelIdByName(service, user, labelName)
        var counter = 0

        if (labelId != null) {
            var pageToken: String? = null
            do {
                val response = listEmailsByLabel(service, user, labelId, dateFrom, dateTo, pageToken)

                if (response != null && response.messages != null) {
                    counter += response.messages.size
                    pageToken = response.nextPageToken
                } else {
                    pageToken = null
                }

            } while (pageToken != null)
        } else {
            return EmailCountResponse(
                null,
                result = Result(false, "Label '$labelName' not found.", ErrorCode.LABEL_NOT_FOUND)
            )
        }

        return EmailCountResponse(EmailCount(counter, dateFrom, dateTo), result = Result())
    }

    private fun getLabelIdByName(service: Gmail, userId: String, labelName: String): String? {
        val labelsListResponse = service.users().labels().list(userId).execute()
        val labels = labelsListResponse.labels
        for (label in labels) {
            if (label.name == labelName) {
                return label.id
            }
        }
        return null
    }

    private fun listEmailsByLabel(
        service: Gmail,
        userId: String,
        labelId: String,
        dateFrom: String,
        dateTo: String,
        pageToken: String? = null
    ): ListMessagesResponse? {
        val request = service.users().messages().list(userId).apply {
            labelIds = listOf(labelId)
            maxResults = MESSAGE_PER_CALL
            q = "after:$dateFrom before:$dateTo"
        }
        if (pageToken != null) {
            request.pageToken = pageToken
        }
        return request.execute()
    }
}