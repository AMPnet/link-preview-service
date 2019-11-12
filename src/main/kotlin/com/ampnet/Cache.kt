package com.ampnet

import com.ampnet.response.PreviewResponse
import java.lang.ref.SoftReference

class Cache {

    private val map = HashMap<String, SoftReference<PreviewResponse>>()

    fun set(siteUrl: String, previewResponse: PreviewResponse) {
        map[siteUrl] = SoftReference(previewResponse)
    }

    fun get(siteUrl: String): PreviewResponse? {
        return map[siteUrl]?.get()
    }
}
