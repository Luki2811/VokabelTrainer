package de.luki2811.dev.vokabeltrainer

import android.util.Log
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

@Parcelize
data class WordTranslation(override var mainWord: String,
                           var mainLanguage: Locale,
                           var otherWords: ArrayList<String>,
                           var otherLanguage: Locale,
                           override var isIgnoreCase: Boolean,
                           override var alreadyUsedInExercise: Boolean = false,
                           override var levelMain: Int,
                           override var levelOther: Int,
                           override var additionalInfo: String,
                           override var typeOfWord: Int = VocabularyWord.TYPE_TRANSLATION): VocabularyWord {

    override fun getAsJSON(withoutLanguage: Boolean): JSONObject {
        return JSONObject().apply {
            put("version", CURRENT_JSON_VERSION)
            put("type", VocabularyWord.TYPE_TRANSLATION)
            put("mainWord", mainWord.trim())
            if(!withoutLanguage)
                put("mainLanguage", mainLanguage.language)
            put("otherWords", JSONArray().apply { otherWords.forEach { put(it.trim()) }})
            if (!withoutLanguage)
                put("otherLanguage", otherLanguage.language)
            put("ignoreCase", isIgnoreCase)
            put("levelMain", levelMain)
            put("levelOther", levelOther)
            put("moreInfo", additionalInfo)
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

    override fun getAsCSV(): String {
        return StringBuilder().apply {
            append(typeOfWord).append(";;")
            append(mainWord).append(";;")
            append(getSecondWordsAsString()).append(";;")
            append(isIgnoreCase).append(";;")
            append(additionalInfo)
        }.toString()
    }

    companion object{

        const val CURRENT_JSON_VERSION = 1

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
            val additionalInfo = try {
                json.getString("moreInfo")
            }catch (e: JSONException){
                ""
            }

            return WordTranslation(mainWord, mainLanguageWord, otherWords, otherLanguageWord, isIgnoreCase, levelMain = levelMain, levelOther = levelOther, additionalInfo = additionalInfo)
        }

        fun loadFromCSV(csv: String, langMain: Locale, langOther: Locale): WordTranslation {
            val elements = csv.split(";;")
            // 0 is wordType
            val mainWord = elements[1]
            val otherWords: ArrayList<String> = with(elements[2]) {
                val splitString = split(";")
                splitString.onEach {
                    it.trim()
                }.toMutableList() as ArrayList
            }
            val ignoreCase = elements[3].toBoolean()
            val moreInfo = elements[4]

            return WordTranslation(mainWord, langMain, otherWords, langOther, ignoreCase, levelMain = 0, levelOther = 0, additionalInfo = moreInfo)
        }

    }


}