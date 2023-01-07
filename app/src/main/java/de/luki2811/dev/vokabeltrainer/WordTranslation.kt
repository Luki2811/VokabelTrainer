package de.luki2811.dev.vokabeltrainer

import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Parcelize
data class WordTranslation(override var mainWord: String,
                           var mainLanguage: Locale,
                           var otherWords: ArrayList<String>,
                           var otherLanguage: Locale,
                           override var isIgnoreCase: Boolean,
                           override var alreadyUsedInExercise: Boolean = false,
                           override var level: Int = 0,
                           override var typeOfWord: Int = VocabularyWord.TYPE_TRANSLATION): VocabularyWord {

    override fun getAsJSON(withoutLanguage: Boolean): JSONObject {
        return JSONObject().apply {
            put("type", VocabularyWord.TYPE_TRANSLATION)
            put("first", mainWord)
            if(!withoutLanguage)
                put("firstLanguage", mainLanguage.language)
            put("second", JSONArray().apply { otherWords.forEach { put(it) }})
            if (!withoutLanguage)
                put("secondLanguage", otherLanguage.language)
            put("ignoreCase", isIgnoreCase)
            put("level", level)
        }
    }

    override fun getSecondWordsAsString(): String{
        return StringBuilder().apply {
            otherWords.forEach {
                append(it)
                if(otherWords[otherWords.size-1] != it)
                append("; ")
            }
        }.toString()
    }

    companion object{

        fun loadFromJSON(json: JSONObject, tempFirstLanguage: Locale? = null, tempSecondLanguage: Locale? = null): WordTranslation{

            val firstWord = try {
                json.getString("first")
            } catch (e: JSONException){
                try {
                    json.getString("knownWord")
                } catch (e: JSONException){
                    json.getString("native")
                }
            }
            val firstLanguage = tempFirstLanguage ?: try { Locale(json.getString("firstLanguage")) } catch (e: JSONException){ Locale(json.getString("languageKnownType")) }

            val secondWordString = try {
                json.getString("second")
            } catch (e: JSONException){
                try {
                    json.getString("newWord")
                } catch (e: JSONException){
                    json.getString("new")
                }
            }

            val secondWord = arrayListOf<String>()
            secondWordString.split(";").forEach {
                secondWord.add(it.trim())
            }

            val secondLanguage = tempSecondLanguage ?: try { Locale(json.getString("secondLanguage")) } catch (e: JSONException){ Locale(json.getString("languageNewType")) }
            val isIgnoreCase = try{ json.getBoolean("ignoreCase") } catch (e: JSONException){ json.getBoolean("isIgnoreCase") }

            val level = try { json.getInt("level") } catch (e: JSONException) { 0 }

            return WordTranslation(firstWord, firstLanguage, secondWord, secondLanguage, isIgnoreCase, level = level)
        }

    }


}