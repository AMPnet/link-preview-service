package com.ampnet.response

data class PreviewResponse(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val image: ImagePreviewResponse? = null
)

data class ImagePreviewResponse(val url: String?, val height: String?, val width: String?)
