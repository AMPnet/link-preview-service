package com.ampnet

class Cache {

    private val map = HashMap<String, PreviewResponse>()

    fun set(siteUrl: String, previewResponse: PreviewResponse) {
        map[siteUrl] = previewResponse
    }

    fun get(siteUrl: String): PreviewResponse? {
        return map[siteUrl]
    }
}
