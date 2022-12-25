package de.luki2811.dev.vokabeltrainer

import org.json.JSONObject
import java.util.Locale

data class Synonym(var mainWord: String,
                   var otherWords: ArrayList<String>,
                   var language: Locale,
                   override var level: Int,
                   override var isIgnoreCase: Boolean,
                   override var alreadyUsedInExercise: Boolean,
                   override var typeOfWord: Int = VocabularyWord.TYPE_SYNONYM): VocabularyWord {

    override fun getAsJSON(): JSONObject {
        TODO("Not yet implemented")
    }


}