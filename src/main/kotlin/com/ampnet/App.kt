package com.ampnet

import com.ampnet.graph.OpenGraph
import com.ampnet.response.HealthResponse
import com.ampnet.response.ImagePreviewResponse
import com.ampnet.response.PreviewResponse
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.commons.text.StringEscapeUtils

fun main() {
    val server = embeddedServer(Netty, port = 8126) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        install(CORS) {
            method(HttpMethod.Get)
            anyHost()
        }
        val cache = Cache()
        routing {
            get("/preview") {
                val queryParameters: Parameters = call.request.queryParameters
                val siteUrl: String? = queryParameters["url"]
                if (siteUrl.isNullOrEmpty()) {
                    return@get call.respond(HttpStatusCode.BadRequest)
                }
                cache.get(siteUrl)?.let {
                    return@get call.respond(it)
                }

                try {
                    val response = getSitePreviewResponse(siteUrl)
                    cache.set(siteUrl, response)
                    call.respond(response)
                } catch (exception: Exception) {
                    call.respond(HttpStatusCode.UnprocessableEntity, exception.localizedMessage)
                }
            }
            get("/health") {
                call.respond(HealthResponse("OK"))
            }
        }
    }
    server.start(wait = true)
}

private fun getSitePreviewResponse(siteUrl: String): PreviewResponse {
    val site = OpenGraph(siteUrl, true)
    val title = site.getContent("title")?.convertHtmlToUtf8().orEmpty()
    val url = site.getContent("url").orEmpty()
    val description = site.getContent("description")?.convertHtmlToUtf8().orEmpty()

    val image = site.getContent("image").orEmpty()
    val imageHeight = site.getContent("image:width").orEmpty()
    val imageWidth = site.getContent("image:height").orEmpty()
    val imageResponse = ImagePreviewResponse(image, imageHeight, imageWidth)
    return PreviewResponse(title, description, imageResponse, url)
}

fun String.convertHtmlToUtf8(): String = StringEscapeUtils.unescapeHtml4(this)
