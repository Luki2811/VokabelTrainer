package de.luki2811.dev.vokabeltrainer

import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Parcelize
data class WordFamily(override var mainWord: String,
                      var otherWords: ArrayList<Pair<String, Int>>,
                      var language: Locale,
                      override var isIgnoreCase: Boolean,
                      override var level: Int,
                      override var alreadyUsedInExercise: Boolean = false,
                      override var typeOfWord: Int = VocabularyWord.TYPE_WORD_FAMILY): VocabularyWord {

    override fun getAsJSON(withoutLanguage: Boolean): JSONObject {
        return JSONObject().apply {
            put("type", VocabularyWord.TYPE_WORD_FAMILY)
            put("mainWord", mainWord)
            put("otherWords", JSONArray().apply {
                otherWords.forEach {
                    put(JSONObject().apply {
                        put("word", it.first)
                        put("type", it.second)
                    })
                }
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
                append(it.first)
                if(otherWords[otherWords.size-1] != it)
                    append("; ")
            }
        }.toString()
    }

    companion object{

        fun loadFromJSON(json: JSONObject): WordFamily{
            val mainWord = json.getString("mainWord")
            val otherWords: ArrayList<Pair<String, Int>> = arrayListOf()
            for (i in 0 until json.getJSONArray("otherWords").length()){
                otherWords.add(Pair<String, Int>(
                    json.getJSONArray("otherWords").getJSONObject(i).getString("word"),
                    json.getJSONArray("otherWords").getJSONObject(i).getInt("type")
                ))
            }
            val language = try{
                Locale.forLanguageTag(json.getString("language"))
            }catch (e: JSONException){
                Locale.ENGLISH
            }
            val ignoreCase = json.getBoolean("ignoreCase")
            val level = json.getInt("level")

            return WordFamily(mainWord, otherWords, language, ignoreCase, level)
        }

        const val WORD_NOMEN = 10
        const val WORD_VERB = 11
        const val WORD_ADJECTIVE = 12
    }

}