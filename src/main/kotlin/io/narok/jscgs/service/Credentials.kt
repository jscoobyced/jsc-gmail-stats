package io.narok.jscgs.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.GmailScopes
import io.narok.jscgs.models.RequestParameters
import java.io.File
import java.io.InputStreamReader

private const val TOKENS_DIRECTORY_PATH = "tokens"
private const val CREDENTIALS_FILE = "credentials.json"
private val CALLBACK_URL = System.getenv("varname") ?: "http://localhost:8888/Callback"
private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

class Credentials {

    companion object {
        private val _scopes = setOf(
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_READONLY
        )

        private fun getTokenDirectoryPath(username: String): String {
            return "$TOKENS_DIRECTORY_PATH/$username"
        }

        private fun getCallbackUrl(username: String): String {
            return "$CALLBACK_URL/?${RequestParameters.USERNAME}=$username"
        }

        fun getCredentialsUrl(username: String, httpTransport: HttpTransport): String {
            val inputStream = File(CREDENTIALS_FILE).inputStream()
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))
            val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, _scopes)
                .setDataStoreFactory(FileDataStoreFactory(File(getTokenDirectoryPath(username))))
                .setAccessType("offline")
                .build()
            val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(getCallbackUrl(username)).build()
            println(authorizationUrl)
            return authorizationUrl
        }

        fun createToken(username: String, code: String, httpTransport: HttpTransport) {
            val inputStream = File(CREDENTIALS_FILE).inputStream()
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

            val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, _scopes)
                .setDataStoreFactory(FileDataStoreFactory(File(getTokenDirectoryPath(username))))
                .setAccessType("offline")
                .build()

            val tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(getCallbackUrl(username))
                .execute()

            flow.createAndStoreCredential(tokenResponse, "user")
        }

        fun getToken(username: String, httpTransport: HttpTransport): Credential? {
            val inputStream = File(CREDENTIALS_FILE).inputStream()
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

            val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, _scopes)
                .setDataStoreFactory(FileDataStoreFactory(File(getTokenDirectoryPath(username))))
                .setAccessType("offline")
                .build()

            return flow.loadCredential("user")
        }
    }
}