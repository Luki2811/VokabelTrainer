package de.luki2811.dev.vokabeltrainer

import android.os.Parcelable
import org.json.JSONObject

interface VocabularyWord: Parcelable {
    var levelMain: Int
    var levelOther: Int
    var isIgnoreCase: Boolean
    var alreadyUsedInExercise: Boolean
    var typeOfWord: Int
    var mainWord: String

    fun getAsJSON(withoutLanguage: Boolean): JSONObject

    fun getSecondWordsAsString(): String

    companion object{
        const val TYPE_UNKNOWN = -1
        const val TYPE_TRANSLATION = 0
        const val TYPE_SYNONYM = 1
        const val TYPE_ANTONYM = 2
        const val TYPE_WORD_FAMILY = 3
    }
}