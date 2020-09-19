package com.ampnet

import com.ampnet.graph.OpenGraph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenGraphTest {
    @Test
    fun mustGetOpenGraphDataFromSite() {
        val site = OpenGraph(
            "https://super1.telegram.hr/relax/u-zagrebu-se-otvara-najveci-coworking-prostor-u-ovom-dijelu-europe-evo-kako-ce-izgledati-impresivni-kompleks/",
            true
        )
        assertTrue(
            site.getContent("title")
                ?.startsWith("U Zagrebu se otvara najveći coworking prostor u ovom dijelu Europe.")
                ?: false
        )
        assertTrue(
            site.getContent("description")
                ?.startsWith("Wespa Spaces su moderno-industrijski uredi za digitalno doba.")
                ?: false
        )
        assertEquals(
            "https://super1.telegram.hr/wp-content/uploads/sites/3/2020/09/2020_9_16_naslovna4.jpg",
            site.getContent("image")
        )
        assertEquals("1200", site.getContent("image:width"))
        assertEquals("709", site.getContent("image:height"))
    }
}
