package com.ampnet.response

data class PreviewResponse(
    val url: String,
    val openGraph: OpenGraphResponse? = null
)

data class OpenGraphResponse(
    val title: String? = null,
    val description: String? = null,
    val image: ImagePreviewResponse? = null
)

data class ImagePreviewResponse(val url: String?, val height: String?, val width: String?)
