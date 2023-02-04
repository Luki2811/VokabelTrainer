package de.luki2811.dev.vokabeltrainer

import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Parcelize
data class WordFamily(override var mainWord: String,
                      var otherWords: ArrayList<String>,
                      var otherWordsType: Int,
                      var language: Locale,
                      override var isIgnoreCase: Boolean,
                      override var level: Int = 0,
                      override var alreadyUsedInExercise: Boolean = false,
                      override var typeOfWord: Int = VocabularyWord.TYPE_WORD_FAMILY): VocabularyWord {

    override fun getAsJSON(withoutLanguage: Boolean): JSONObject {
        return JSONObject().apply {
            put("type", VocabularyWord.TYPE_WORD_FAMILY)
            put("mainWord", mainWord.trim())
            put("otherWords", JSONArray().apply {
                otherWords.forEach {
                    put(it)
                }
            })
            put("otherWordsType", otherWordsType)
            if(!withoutLanguage) {
                put("language", language.language)
            }
            put("ignoreCase", isIgnoreCase)
            put("level", level)
        }
    }

    override fun getSecondWordsAsString(): String{
        return StringBuilder().apply {
            otherWords.forEach {
                append(it.trim())
                if(otherWords[otherWords.size-1] != it)
                    append("; ")
            }
        }.toString()
    }

    companion object{

        fun loadFromJSON(json: JSONObject, tempLanguage: Locale? = null): WordFamily{
            val mainWord = json.getString("mainWord")
            val otherWords: ArrayList<String> = arrayListOf()
            for (i in 0 until json.getJSONArray("otherWords").length()){
                otherWords.add(json.getJSONArray("otherWords").getString(i))
            }
            val otherWordsType = json.getInt("otherWordsType")
            val language = tempLanguage ?: try{
                Locale.forLanguageTag(json.getString("language"))
            }catch (e: JSONException){
                Locale.ENGLISH
            }
            val ignoreCase = json.getBoolean("ignoreCase")
            val level = json.getInt("level")

            return WordFamily(mainWord, otherWords, otherWordsType , language, ignoreCase, level)
        }

        const val WORD_UNKNOWN = -10
        const val WORD_NOUN = 10
        const val WORD_VERB = 11
        const val WORD_ADJECTIVE = 12
        const val WORD_ADVERB = 13
    }

}