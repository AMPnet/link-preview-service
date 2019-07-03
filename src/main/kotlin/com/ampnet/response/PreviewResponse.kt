package com.ampnet.response

data class PreviewResponse(val title: String, val description: String, val image: ImagePreviewResponse, val url: String)
data class ImagePreviewResponse(val url: String, val height: String, val width: String)
