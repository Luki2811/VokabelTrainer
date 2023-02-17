package de.luki2811.dev.vokabeltrainer

import android.content.Context
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
                      override var levelMain: Int,
                      override var levelOther: Int,
                      override var alreadyUsedInExercise: Boolean = false,
                      override var typeOfWord: Int = VocabularyWord.TYPE_WORD_FAMILY): VocabularyWord {

    override fun getAsJSON(withoutLanguage: Boolean): JSONObject {
        return JSONObject().apply {
            put("version", CURRENT_JSON_VERSION)
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
            put("levelMain", levelMain)
            put("levelOther", levelOther)
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

    fun getTypeDisplayName(context: Context): String{
        return when(otherWordsType){
            WORD_NOUN -> context.getString(R.string.word_type_noun)
            WORD_ADVERB -> context.getString(R.string.word_type_adverb)
            WORD_VERB -> context.getString(R.string.word_type_verb)
            WORD_ADJECTIVE -> context.getString(R.string.word_type_adjective)
            else -> "UNKNOWN"
        }
    }

    companion object{

        const val CURRENT_JSON_VERSION = 1

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
            val levelMain = try {
                json.getInt("levelMain")
            } catch (e: JSONException) {
                try {
                    json.getInt("level")
                }catch (e: JSONException) {
                    0
                }
            }
            val levelOther = try {
                json.getInt("levelOther")
            }catch (e: JSONException) {
                levelMain
            }

            return WordFamily(mainWord, otherWords, otherWordsType , language, ignoreCase, levelMain = levelMain, levelOther = levelOther)
        }

        const val WORD_UNKNOWN = -10
        const val WORD_NOUN = 10
        const val WORD_VERB = 11
        const val WORD_ADJECTIVE = 12
        const val WORD_ADVERB = 13
    }

}