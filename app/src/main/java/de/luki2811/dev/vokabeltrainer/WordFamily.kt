package de.luki2811.dev.vokabeltrainer

import org.json.JSONObject
import java.util.*

data class WordFamily(var mainWord: String,
                      var otherWords: ArrayList<Pair<String, Int>>,
                      var language: Locale,
                      override var isIgnoreCase: Boolean,
                      override var level: Int,
                      override var alreadyUsedInExercise: Boolean,
                      override var typeOfWord: Int = VocabularyWord.TYPE_WORD_FAMILY): VocabularyWord {

    override fun getAsJSON(): JSONObject {
        TODO("Not yet implemented")
    }

}