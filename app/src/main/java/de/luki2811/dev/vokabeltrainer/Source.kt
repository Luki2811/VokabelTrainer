package de.luki2811.dev.vokabeltrainer

import java.net.URL

class Source(val name: String, val version: String?, val type: String, val link: URL, val other: String = "") {

    companion object{
        const val TYPE_MIT = "The MIT License"
        const val TYPE_APACHE_2_0 = "The Apache Software License, Version 2.0"

        const val LINK_APACHE_2_0_DEFAULT = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }
}