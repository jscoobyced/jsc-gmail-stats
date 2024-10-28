package io.narok.jscgs.plugins

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.narok.jscgs.exception.UserNotFoundException
import io.narok.jscgs.models.EmailCountRequest
import io.narok.jscgs.models.EmailCountResponse
import io.narok.jscgs.models.ErrorCode
import io.narok.jscgs.models.LoginRequest
import io.narok.jscgs.models.Result
import io.narok.jscgs.models.UnregisterRequest
import io.narok.jscgs.repository.UNVERIFIED
import io.narok.jscgs.repository.UserRepository
import io.narok.jscgs.service.Credentials
import io.narok.jscgs.service.GmailExtractor
import io.narok.jscgs.service.RequestParameterFormatter
import java.lang.IllegalArgumentException

fun Application.configureRouting() {


    routing {

        staticResources("/", "static") {
            extensions("html", "htm")
            default("index.html")
            enableAutoHeadResponse()
            preCompressed(CompressedFileType.GZIP)
            exclude { url -> url.path.contains("excluded") }
        }

        route("/login") {
            post<LoginRequest> { loginRequest ->
                val username = loginRequest.email.filter { it.isLetter() }
                val password = loginRequest.password
                if (username.isBlank() || password.isBlank()) {
                    call.respond(Result(false, "Username or password cannot be empty.", ErrorCode.MISSING_FIELD.value))
                    return@post
                }
                try {
                    if (!UserRepository.registerUser(username, password)) {
                        call.respond(Result(false, "Cannot register user.", ErrorCode.CANNOT_CREATE_USER.value))
                        return@post
                    }
                } catch (exc: Exception) {
                    call.respond(Result(false, exc.message, ErrorCode.DATABASE_ERROR.value))
                    return@post
                }

                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                val authorizationUrl = Credentials.getCredentialsUrl(username, httpTransport)
                call.respond(
                    Result(
                        true,
                        authorizationUrl
                    )
                )
            }
        }

        route("/Callback") {
            get {
                val code = call.request.queryParameters["code"] ?: ""
                if (code.isBlank()) {
                    call.respond(Result(false, "Authorization code not found"))
                    return@get
                }
                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                Credentials.createToken(code, httpTransport)

                call.respondText(
                    "Authorization successful! You can close this tab and use the application.",
                    ContentType.Text.Html
                )
            }
        }

        route("/unregister") {
            post<UnregisterRequest> { unregisterRequest ->
                val username = unregisterRequest.email.filter { it.isLetter() }
                if (UserRepository.unregisterUser(username)) {
                    call.respond(Result(true))
                } else {
                    call.respond(Result(false, "User doesn't exist or can't be unregistered."))
                }

            }
        }

        route("/count") {

            post<EmailCountRequest> { emailCountRequest ->

                val safeUsername = emailCountRequest.email.filter { it.isLetter() }
                val password = emailCountRequest.password
                val user = UserRepository.verifyUser(safeUsername, password)
                if (user == UNVERIFIED) {
                    call.respond(
                        EmailCountResponse(
                            null, Result(
                                false, "Invalid user or password.",
                                ErrorCode.USER_NOT_REGISTERED.value
                            )
                        )
                    )
                    return@post
                }

                if (user.isBlank() || user != safeUsername) {
                    call.respond(
                        EmailCountResponse(
                            null, Result(
                                false, "User not registered.",
                                ErrorCode.INCORRECT_PASSWORD.value
                            )
                        )
                    )
                    return@post
                }

                val label = emailCountRequest.label
                val dateFrom = emailCountRequest.dateFrom
                val dateTo = emailCountRequest.dateTo

                try {
                    val response = GmailExtractor().extract(
                        safeUsername,
                        password,
                        label,
                        RequestParameterFormatter.formatDate(dateFrom),
                        RequestParameterFormatter.formatDate(dateTo)
                    )
                    call.respond(response)
                } catch (exc: UserNotFoundException) {
                    call.respond(
                        EmailCountResponse(
                            null, Result(
                                false, exc.message,
                                ErrorCode.USER_NOT_REGISTERED.value
                            )
                        )
                    )
                } catch (exc: IllegalArgumentException) {
                    call.respond(
                        EmailCountResponse(
                            null, Result(
                                false, exc.message,
                                ErrorCode.MISSING_FIELD.value
                            )
                        )
                    )
                }
            }

            route("/{...}") {
                handle {
                    call.respond(Result(false, "Date to cannot be empty."))
                }
            }


        }

        route("/{...}") {
            handle {
                call.respond(Result(false, "API not found."))
            }
        }
    }
}
