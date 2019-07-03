package com.ampnet

import com.ampnet.graph.OpenGraph
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val server = embeddedServer(Netty, port = 8200) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
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

                val response = getSitePreviewResponse(siteUrl)
                cache.set(siteUrl, response)
                call.respond(response)
            }
            get("/health") {
                call.respondText("OK")
            }
        }
    }
    server.start(wait = true)
}

private fun getSitePreviewResponse(siteUrl: String): PreviewResponse {
    val site = OpenGraph(siteUrl, true)
    val title = site.getContent("title").orEmpty()
    val url = site.getContent("url").orEmpty()
    val description = site.getContent("description").orEmpty()

    val image = site.getContent("image").orEmpty()
    val imageHeight = site.getContent("image:width").orEmpty()
    val imageWidth = site.getContent("image:height").orEmpty()
    val imageResponse = ImagePreviewResponse(image, imageHeight, imageWidth)
    return PreviewResponse(title, description, imageResponse, url)
}
