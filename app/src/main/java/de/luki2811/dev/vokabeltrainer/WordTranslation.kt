package de.luki2811.dev.vokabeltrainer

import android.util.Log
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
            put("mainWord", mainWord.trim())
            if(!withoutLanguage)
                put("mainLanguage", mainLanguage.language)
            put("otherWords", JSONArray().apply { otherWords.forEach { put(it.trim()) }})
            if (!withoutLanguage)
                put("otherLanguage", otherLanguage.language)
            put("ignoreCase", isIgnoreCase)
            put("level", level)
        }
    }

    override fun getSecondWordsAsString(): String{
        return StringBuilder().apply {
            otherWords.forEach {
                append(it.trim())
                if(otherWords.last() != it)
                append("; ")
            }
        }.toString()
    }

    companion object{

        fun loadFromJSON(json: JSONObject, mainLanguage: Locale? = null, otherLanguage: Locale? = null): WordTranslation{

            val mainWord = try {
                json.getString("mainWord")
            } catch (e: JSONException){
                try {
                    json.getString("newWord")
                } catch (e: JSONException){
                    json.getString("second")
                }
            }
            val mainLanguageWord = mainLanguage ?: try { Locale(json.getString("mainLanguage")) } catch (e: JSONException){ Locale(json.getString("secondLanguage")) }

            val otherWords: ArrayList<String> = try {
                val oWords = arrayListOf<String>()
                for (i in 0 until json.getJSONArray("otherWords").length()){
                    oWords.add(json.getJSONArray("otherWords").getString(i))
                }
                oWords
            }catch (e: JSONException){
                val array = json.getString("first").split(";").toMutableList() as ArrayList<String>
                array.onEach { it.trim() }
            }

            if(otherWords.isEmpty()){
                Log.w("WordTranslation","No other words found")
            }

            val otherLanguageWord = otherLanguage ?: try { Locale(json.getString("otherLanguage")) } catch (e: JSONException){ Locale(json.getString("firstLanguage")) }
            val isIgnoreCase = try{ json.getBoolean("ignoreCase") } catch (e: JSONException){ json.getBoolean("isIgnoreCase") }

            val level = try { json.getInt("level") } catch (e: JSONException) { 0 }

            return WordTranslation(mainWord, mainLanguageWord, otherWords, otherLanguageWord, isIgnoreCase, level = level)
        }

    }


}