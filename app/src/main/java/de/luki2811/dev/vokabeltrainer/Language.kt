package de.luki2811.dev.vokabeltrainer

class Language(var type: Int) {
    var name: String? = null
    @JvmName("getName1")
    fun getName(): String? {
        return when (type) {
            0 -> "Englisch"
            1 -> "Deutsch"
            2 -> "Französisch"
            3 -> "Schwedisch"
            else -> null
        }
    }

    companion object {
        const val ENGLISH = 0
        const val GERMAN = 1
        const val FRENCH = 2
        const val SWEDISH = 3
    }
}