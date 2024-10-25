package io.narok.jscgs.plugins

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.narok.jscgs.models.RequestParameters
import io.narok.jscgs.models.Result
import io.narok.jscgs.service.Credentials
import io.narok.jscgs.service.GmailExtractor
import io.narok.jscgs.service.RequestParameterFormatter

fun Application.configureRouting() {


    routing {
        route("/login") {
            get {
                val username = call.parameters[RequestParameters.USERNAME] ?: ""
                if (username.isBlank()) {
                    call.respond(Result(false, "Username cannot be empty."))
                    return@get
                }

                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                val authorizationUrl = Credentials.getCredentialsUrl(username, httpTransport)
                call.respond(
                    Result(
                        true,
                        "Click <a href=\"$authorizationUrl\" target=\"_blank\">here</a> to authorize the application."
                    )
                )
            }
        }

        route("/Callback") {
            get {
                val username = call.parameters[RequestParameters.USERNAME] ?: ""
                if (username.isBlank()) {
                    call.respond(Result(false, "Username cannot be empty."))
                    return@get
                }

                val code = call.request.queryParameters["code"] ?: ""
                if (code.isBlank()) {
                    call.respond(Result(false, "Authorization code not found"))
                    return@get
                }
                val safeUsername = username.filter { it.isLetter() }

                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                Credentials.createToken(safeUsername, code, httpTransport)

                call.respond(Result(true, "Authorization successful! You can now use the Google API."))
            }
        }

        route("/count") {
            route("/{${RequestParameters.USERNAME}}") {
                route("/{${RequestParameters.LABEL}}") {
                    route("/{${RequestParameters.DATE_FROM}}") {
                        route("/{${RequestParameters.DATE_TO}}") {
                            get {

                                val username = call.parameters[RequestParameters.USERNAME] ?: ""
                                val safeUsername = username.filter { it.isLetter() }
                                val label = call.parameters[RequestParameters.LABEL] ?: "_NONE_"
                                val dateFrom = call.parameters[RequestParameters.DATE_FROM] ?: "2200-01-01"
                                val dateTo = call.parameters[RequestParameters.DATE_TO] ?: "2201-01-01"

                                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

                                val response = GmailExtractor().extract(
                                    httpTransport,
                                    safeUsername,
                                    label,
                                    RequestParameterFormatter.formatDate(dateFrom),
                                    RequestParameterFormatter.formatDate(dateTo)
                                )
                                call.respond(response)
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
                            call.respond(Result(false, "Date from cannot be empty."))
                        }
                    }
                }

                route("/{...}") {
                    handle {
                        call.respond(Result(false, "Label cannot be empty."))
                    }
                }
            }

            route("/{...}") {
                handle {
                    call.respond(Result(false, "Username cannot be empty."))
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
