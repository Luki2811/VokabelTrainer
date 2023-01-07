package de.luki2811.dev.vokabeltrainer

import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Parcelize
data class Synonym(override var mainWord: String,
                   var otherWords: ArrayList<String>,
                   var language: Locale,
                   override var level: Int,
                   override var isIgnoreCase: Boolean,
                   override var alreadyUsedInExercise: Boolean = false,
                   override var typeOfWord: Int = VocabularyWord.TYPE_SYNONYM): VocabularyWord {


    override fun getAsJSON(withoutLanguage: Boolean): JSONObject {
        return JSONObject().apply {
            put("type", typeOfWord)
            put("mainWord", mainWord)
            put("otherWords", JSONArray().apply {
                otherWords.forEach { put(it) }
            })
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
                append(it)
                if(otherWords[otherWords.size-1] != it)
                    append("; ")
            }
        }.toString()
    }

    companion object{
        fun loadFromJSON(json: JSONObject): Synonym{
            val mainWord = json.getString("mainWord")
            val otherWords: ArrayList<String> = arrayListOf()
            for (i in 0 until json.getJSONArray("otherWords").length()){
                otherWords.add(json.getJSONArray("otherWords").getString(i))
            }
            val language = try{
                Locale.forLanguageTag(json.getString("language"))
            }catch (e: JSONException){
                Locale.ENGLISH
            }
            val ignoreCase = json.getBoolean("ignoreCase")
            val level = json.getInt("level")

            return Synonym(mainWord, otherWords, language, isIgnoreCase = ignoreCase, level =  level)
        }
    }


}