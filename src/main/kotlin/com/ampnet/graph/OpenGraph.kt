package com.ampnet.graph

import org.htmlcleaner.HtmlCleaner

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.Hashtable
import java.util.regex.Pattern

/**
 * A Java object representation of an Open Graph enabled webpage.
 * A simplified layer over a Hastable.
 *
 * @author Callum Jones
 */
class OpenGraph
/**
 * Create an open graph representation for generating your own Open Graph object
 */
() {
    /**
     * Get the original URL the Open Graph page was obtained from
     * @return The address to the Open Graph object page
     */
    lateinit var originalUrl: String
    private val pageNamespaces: ArrayList<OpenGraphNamespace> = ArrayList()
    private val metaAttributes: Hashtable<String, ArrayList<MetaElement>> = Hashtable()

    /**
     * Get the basic type of the Open graph page as per the specification
     * @return Base type as defined by specification, null otherwise
     */
    var baseType: String? = null
        private set
    /**
     * Test if the Open Graph object was initially a representation of a web page
     * @return True if the object is from a web page, false otherwise
     */
    var isFromWeb: Boolean = false
        private set // determine if the object is a new incarnation or representation of a web page
    private val hasChanged: Boolean // track if object has been changed

    /**
     * Get all the defined properties of the Open Graph object
     * @return An array of all currently defined properties
     */
    val properties: Array<MetaElement>
        get() {
            val allElements = ArrayList<MetaElement>()
            for (collection in metaAttributes.values)
                allElements.addAll(collection)

            return allElements.toTypedArray()
        }

    init {
        hasChanged = false
        isFromWeb = false
    }

    /**
     * Fetch the open graph representation from a web site
     * @param url The address to the web page to fetch Open Graph data
     * @param ignoreSpecErrors Set this option to true if you don't wish to have an exception throw if the page does not conform to the basic 4 attributes
     * @throws java.io.IOException If a network error occurs, the HTML parser will throw an IO Exception
     * @throws java.lang.Exception A generic exception is throw if the specific page fails to conform to the basic Open Graph standard as define by the constant REQUIRED_META
     */
    @Throws(java.io.IOException::class, Exception::class)
    constructor(url: String, ignoreSpecErrors: Boolean) : this() {
        isFromWeb = true

        // download the (X)HTML content, but only up to the closing head tag. We do not want to waste resources parsing irrelevant content
        val pageURL = URL(url)
        val siteConnection = pageURL.openConnection()
        val charset = getConnectionCharset(siteConnection)
        val dis = BufferedReader(InputStreamReader(siteConnection.getInputStream(), charset))
        val headContents = StringBuffer()

        val iterator = dis.lineSequence().iterator()
        while (iterator.hasNext()) {
            var inputLine = iterator.next()
            if (inputLine.contains("</head>")) {
                inputLine = inputLine.substring(0, inputLine.indexOf("</head>") + 7)
                inputLine = "$inputLine<body></body></html>"
                headContents.append(inputLine + "\r\n")
                break
            }
            headContents.append(inputLine + "\r\n")
        }

        val headContentsStr = headContents.toString()
        val cleaner = HtmlCleaner()
        // parse the string HTML
        val pageData = cleaner.clean(headContentsStr)

        // read in the declared namespaces
        var hasOGspec = false
        val headElement = pageData.findElementByName("head", true)
        if (headElement.hasAttribute("prefix")) {
            val namespaceData = headElement.getAttributeByName("prefix")
            val pattern = Pattern.compile("(([A-Za-z0-9_]+):\\s+(http:\\/\\/ogp.me\\/ns(\\/\\w+)*#))\\s*")
            val matcher = pattern.matcher(namespaceData)
            while (matcher.find()) {
                val prefix = matcher.group(2)
                val documentURI = matcher.group(3)
                pageNamespaces.add(OpenGraphNamespace(prefix, documentURI))
                if (prefix == "og")
                    hasOGspec = true
            }
        }

        // some pages do not include the new OG spec
        // this fixes compatibility
        if (!hasOGspec)
            pageNamespaces.add(OpenGraphNamespace("og", "http:// ogp.me/ns#"))

        // open only the meta tags
        val metaData = pageData.getElementsByName("meta", true)
        for (metaElement in metaData) {
            for (namespace in pageNamespaces) {
                var target: String? = null
                if (metaElement.hasAttribute("property"))
                    target = "property"
                else if (metaElement.hasAttribute("name"))
                    target = "name"

                if (target != null && metaElement.getAttributeByName(target).startsWith(namespace.prefix + ":")) {
                    setProperty(namespace, metaElement.getAttributeByName(target), metaElement.getAttributeByName("content"))
                    break
                }
            }
        }

        /**
         * Check that page conforms to Open Graph protocol
         */
        if (!ignoreSpecErrors) {
            for (req in REQUIRED_META) {
                if (!metaAttributes.containsKey(req))
                    throw Exception("Does not conform to Open Graph protocol")
            }
        }

        /**
         * Has conformed, now determine basic sub type.
         */
        baseType = null
        var currentType = getContent("type")
        // some apps use their OG namespace as a prefix
        if (currentType != null) {
            for ((prefix) in pageNamespaces) {
                if (currentType!!.startsWith("$prefix:")) {
                    currentType = currentType.replaceFirst("$prefix:".toRegex(), "")
                    break // done here
                }
            }
        }

        baseType = BASE_TYPES.keys.first {
            BASE_TYPES[it]?.firstOrNull { expandedType ->
                expandedType == currentType
            }.isNullOrEmpty()
        }

        // read the original page url
        val realURL = siteConnection.url
        originalUrl = realURL.toExternalForm()
    }

    /**
     * Get a value of a given Open Graph property
     * @param property The Open graph property key
     * @return Returns the value of the first property defined, null otherwise
     */
    fun getContent(property: String): String? {
        return if (metaAttributes.containsKey(property) && metaAttributes[property]!!.size > 0)
            metaAttributes[property]!!.get(0).content
        else
            null
    }

    /**
     * Get all the defined properties of the Open Graph object
     * @param property The property to focus on
     * @return An array of all currently defined properties
     */
    fun getProperties(property: String): Array<MetaElement>? {
        if (metaAttributes.containsKey(property)) {
            val target = metaAttributes[property]
            return target!!.toTypedArray()
        } else
            return null
    }


    /**
     * Get the HTML representation of the Open Graph data.
     * @return An array of meta elements as Strings
     */
    fun toHTML(): Array<String> {
        // allocate the array
        val returnHTML = ArrayList<String>()

        for (elements in metaAttributes.values) {
            for ((namespace, property, content) in elements)
                returnHTML.add("<meta property=\"" + namespace + ":" +
                        property + "\" content=\"" + content + "\" />")
        }

        // return the array
        return returnHTML.toTypedArray()
    }

    /**
     * Get the XHTML representation of the Open Graph data.
     * @return An array of meta elements as Strings
     */
    fun toXHTML(): Array<String> {
        // allocate the array
        val returnHTML = ArrayList<String>()

        for (elements in metaAttributes.values) {
            for ((namespace, property, content) in elements)
                returnHTML.add("<meta name=\"" + namespace.prefix + ":" +
                        property + "\" content=\"" + content + "\" />")
        }

        // return the array
        return returnHTML.toTypedArray()
    }

    /**
     * Set the Open Graph property to a specific value
     * @param namespace The OpenGraph namespace the content belongs to
     * @param property The og:XXXX where XXXX is the property you wish to set
     * @param content The value or contents of the property to be set
     */
    fun setProperty(namespace: OpenGraphNamespace, property: String, content: String) {
        var propertyKey = property
        if (!pageNamespaces.contains(namespace))
            pageNamespaces.add(namespace)

        propertyKey = propertyKey.replace((namespace.prefix + ":").toRegex(), "")
        val element = MetaElement(namespace, propertyKey, content)
        if (!metaAttributes.containsKey(propertyKey))
            metaAttributes[propertyKey] = ArrayList()

        metaAttributes[propertyKey]!!.add(element)
    }

    /**
     * Removed a defined property
     * @param property The og:XXXX where XXXX is the property you wish to remove
     */
    fun removeProperty(property: String) {
        metaAttributes.remove(property)
    }

    /**
     * Obtain the underlying HashTable
     * @return The underlying structure as a Hashtable
     */
    fun exposeTable(): Hashtable<String, ArrayList<MetaElement>> {
        return metaAttributes
    }

    /**
     * Test if the object has been modified by setters/deleters.
     * This is only relevant if this object initially represented a web page
     * @return True True if the object has been modified, false otherwise
     */
    fun hasChanged(): Boolean {
        return hasChanged
    }

    companion object {

        val REQUIRED_META = arrayOf("title", "type", "image", "url")

        val BASE_TYPES = Hashtable<String, Array<String>>()

        init {
            BASE_TYPES["activity"] = arrayOf("activity", "sport")
            BASE_TYPES["business"] = arrayOf("bar", "company", "cafe", "hotel", "restaurant")
            BASE_TYPES["group"] = arrayOf("cause", "sports_league", "sports_team")
            BASE_TYPES["organization"] = arrayOf("band", "government", "non_profit", "school", "university")
            BASE_TYPES["person"] = arrayOf("actor", "athlete", "author", "director", "musician", "politician", "profile", "public_figure")
            BASE_TYPES["place"] = arrayOf("city", "country", "landmark", "state_province")
            BASE_TYPES["product"] = arrayOf("album", "book", "drink", "food", "game", "movie", "product", "song", "tv_show")
            BASE_TYPES["website"] = arrayOf("blog", "website", "article")
        }

        /**
         * Gets the charset for specified connection.
         * Content Type header is parsed to get the charset name.
         *
         * @param connection the connection.
         * @return the Charset object for response charset name;
         * if it's not found then the default charset.
         */
        private fun getConnectionCharset(connection: URLConnection): Charset {
            var contentType: String? = connection.contentType
            if (contentType != null && contentType.length > 0) {
                contentType = contentType.toLowerCase()
                val charsetName = extractCharsetName(contentType)
                if (charsetName != null && charsetName.length > 0) {
                    try {
                        return Charset.forName(charsetName)
                    } catch (e: Exception) {
                        // specified charset is not found,
                        // skip it to return the default one
                    }

                }
            }

            // return the default charset
            return Charset.defaultCharset()
        }

        /**
         * Extract the charset name form the content type string.
         * Content type string is received from Content-Type header.
         *
         * @param contentType the content type string, must be not null.
         * @return the found charset name or null if not found.
         */
        private fun extractCharsetName(contentType: String): String? {
            // split onto media types
            val mediaTypes = contentType.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (mediaTypes.isNotEmpty()) {
                // use only the first one, and split it on parameters
                val params = mediaTypes[0].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                // find the charset parameter and return it's value
                val charset = params.firstOrNull { it.trim { trimmed -> trimmed <= ' ' }.startsWith("charset=") }

                return charset?.substring(8)?.trim { it <= ' ' }
            }

            return null
        }
    }
}
