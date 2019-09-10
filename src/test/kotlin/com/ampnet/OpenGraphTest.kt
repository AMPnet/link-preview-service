package com.ampnet

import com.ampnet.graph.OpenGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenGraphTest {
    @Test
    fun mustGetOpenGraphDataFromSite() {
        val site = OpenGraph("https://www.tportal.hr/kultura/clanak/janekovic-dabac-pavic-najbolje-od-hrvatske-fotografije-20-stoljeca-na-izlozbi-u-moskvi-foto-20190626", true)
        assertEquals("Janeković, Dabac, Pavić: Najbolje od hrvatske fotografije 20. stoljeća na izložbi u Moskvi", site.getContent("title"))
        assertEquals("'Klasici hrvatske fotografije' naziv je izložbe zagrebačkog Muzeja za umjetnost i obrt (MUO) koja se 27. lipnja otvara u Multimedijskom umjetničkom muzeju u Moskvi i predstavlja izbor najboljih radova nekih od ključnih ličnosti hrvatske fotografije dvades", site.getContent("description"))
        assertEquals("https://www.tportal.hr/media/thumbnail/800x600/1016455.jpeg?cropId=958038", site.getContent("image"))
        assertEquals("800", site.getContent("image:width"))
        assertEquals("600", site.getContent("image:height"))
    }
}
