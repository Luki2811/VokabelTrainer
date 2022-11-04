package de.luki2811.dev.vokabeltrainer

interface Exportable {
    val type: Int

    companion object{

        const val TYPE_LESSON = 0
        const val TYPE_VOCABULARY_GROUP = 1
        const val TYPE_SHORT_FORM = 2
    }
}