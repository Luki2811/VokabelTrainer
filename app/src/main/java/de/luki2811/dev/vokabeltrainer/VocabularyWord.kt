package de.luki2811.dev.vokabeltrainer

import org.json.JSONObject

interface VocabularyWord {
    var level: Int
    var isIgnoreCase: Boolean
    var alreadyUsedInExercise: Boolean
    var typeOfWord: Int

    fun getAsJSON(): JSONObject

    companion object{
        const val TYPE_UNKNOWN = -1
        const val TYPE_TRANSLATION = 0
        const val TYPE_SYNONYM = 1
        const val TYPE_ANTONYM = 2
        const val TYPE_WORD_FAMILY = 3
    }
}