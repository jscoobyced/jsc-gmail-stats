package io.narok.jscgs.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import java.io.File
import java.io.InputStreamReader

private const val TOKENS_DIRECTORY_PATH = "tokens"
private const val CREDENTIALS_FILE = "credentials.json"
private val CALLBACK_URL = System.getenv("JSC_CALLBACK_URL") ?: "http://localhost:8888/Callback"
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

        fun getCredentialsUrl(username: String, httpTransport: HttpTransport): String {
            val inputStream = File(CREDENTIALS_FILE).inputStream()
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))
            val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, _scopes)
                .setDataStoreFactory(FileDataStoreFactory(File(getTokenDirectoryPath(username))))
                .setAccessType("offline")
                .build()
            val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build()
            return authorizationUrl
        }

        fun createToken(code: String, httpTransport: HttpTransport) {
            val inputStream = File(CREDENTIALS_FILE).inputStream()
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))
            val username = RandomGenerator.getRandomString(15)

            val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, _scopes)
                .setDataStoreFactory(FileDataStoreFactory(File(getTokenDirectoryPath(username))))
                .setAccessType("offline")
                .build()

            val tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(CALLBACK_URL)
                .execute()

            flow.createAndStoreCredential(tokenResponse, "user")

            val credential = getToken(username, httpTransport)

            val service = Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
            val profile = service.users().getProfile("me").execute()
            val emailAddress = profile.emailAddress.filter { it.isLetter() }
            val newTokenFile = File(getTokenDirectoryPath(emailAddress))
            if (newTokenFile.exists()) {
                newTokenFile.deleteRecursively()
            }
            File(getTokenDirectoryPath(username)).renameTo(newTokenFile)
        }

        fun getToken(username:String, httpTransport: HttpTransport): Credential? {
            try {
                val inputStream = File(CREDENTIALS_FILE).inputStream()
                val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

                val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, _scopes)
                    .setDataStoreFactory(FileDataStoreFactory(File(getTokenDirectoryPath(username))))
                    .setAccessType("offline")
                    .build()

                return flow.loadCredential("user")
            } catch (exc: Exception) {
                return null
            }
        }
    }
}