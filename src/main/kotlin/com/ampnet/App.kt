package com.ampnet

import com.ampnet.graph.OpenGraph
import com.ampnet.response.HealthResponse
import com.ampnet.response.ImagePreviewResponse
import com.ampnet.response.OpenGraphResponse
import com.ampnet.response.PreviewResponse
import com.google.gson.FieldNamingPolicy
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CachingHeaders
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.CacheControl
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.CachingOptions
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
                serializeNulls()
                setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
        install(CORS) {
            method(HttpMethod.Get)
            anyHost()
        }
        install(CachingHeaders) {
            options {
                CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 3600))
            }
        }

        val cache = Cache()
        routing {
            get("/preview") {
                val queryParameters: Parameters = call.request.queryParameters
                val siteUrl: String? = queryParameters["url"]
                call.application.environment.log.info("Received request for url=$siteUrl")
                if (siteUrl.isNullOrEmpty()) {
                    call.application.environment.log.info("Missing query parameter `url`")
                    return@get call.respond(HttpStatusCode.BadRequest)
                }
                cache.get(siteUrl)?.let {
                    call.application.environment.log.debug("Returning data from cache")
                    return@get call.respond(it)
                }

                try {
                    val response = getSitePreviewResponse(siteUrl)
                    cache.set(siteUrl, response)
                    call.application.environment.log.info("Site data = $response")
                    call.respond(response)
                } catch (exception: Exception) {
                    call.application.environment.log.warn("Could not extract site data", exception)
                    val response = PreviewResponse(siteUrl)
                    call.response.headers.append(HttpHeaders.CacheControl, "no-cache, no-store")
                    call.respond(response)
                }
            }
            get("/health") {
                call.response.headers.append(HttpHeaders.CacheControl, "no-cache, no-store")
                call.respond(HealthResponse("OK"))
            }
        }
    }
    server.start(wait = true)
}

private fun getSitePreviewResponse(siteUrl: String): PreviewResponse {
    val site = OpenGraph(siteUrl, true)
    val title = site.getContent("title")?.convertHtmlToUtf8()
    val description = site.getContent("description")?.convertHtmlToUtf8()

    val image = site.getContent("image")
    val imageHeight = site.getContent("image:width")
    val imageWidth = site.getContent("image:height")
    val imageResponse = ImagePreviewResponse(image, imageHeight, imageWidth)
    val openGraphResponse = OpenGraphResponse(title, description, imageResponse)
    return PreviewResponse(siteUrl, openGraphResponse)
}

fun String.convertHtmlToUtf8(): String = StringEscapeUtils.unescapeHtml4(this)
