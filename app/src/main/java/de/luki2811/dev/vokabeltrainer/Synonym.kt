package de.luki2811.dev.vokabeltrainer

import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

@Parcelize
data class Synonym(override var mainWord: String,
                   var otherWords: ArrayList<String>,
                   var language: Locale,
                   override var levelMain: Int,
                   override var levelOther: Int,
                   override var isIgnoreCase: Boolean,
                   override var alreadyUsedInExercise: Boolean = false,
                   override var additionalInfo: String,
                   override var typeOfWord: Int = VocabularyWord.TYPE_SYNONYM): VocabularyWord {

    override fun getAsJSON(withoutLanguage: Boolean): JSONObject {
        return JSONObject().apply {
            put("version", CURRENT_JSON_VERSION)
            put("type", typeOfWord)
            put("mainWord", mainWord.trim())
            put("otherWords", JSONArray().apply {
                otherWords.forEach { put(it.trim()) }
            })
            if(!withoutLanguage) {
                put("language", language.language)
            }
            put("ignoreCase", isIgnoreCase)
            put("levelMain", levelMain)
            put("levelOther", levelOther)
            put("additionalInfo", additionalInfo)
        }
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

        const val CURRENT_JSON_VERSION = 1

        fun loadFromCSV(csv: String, language: Locale): Synonym{
            val elements = csv.split(";;")
            val type = elements[0].toInt()
            val mainWord = elements[1]
            val otherWords: ArrayList<String> = with(elements[2]) {
                val splitString = split(";")
                splitString.onEach {
                    it.trim()
                }.toMutableList() as ArrayList
            }
            val ignoreCase = elements[3].toBoolean()
            val additionalInfo = elements[4]

            return Synonym(mainWord, otherWords, language, levelMain = 0, levelOther = 0, ignoreCase, typeOfWord = type, additionalInfo = additionalInfo)
        }

        fun loadFromJSON(json: JSONObject, tempLanguage: Locale? = null): Synonym{
            val mainWord = try {
                json.getString("mainWord")
            } catch (e: JSONException){
                json.getString("first")
            }
            val otherWords: ArrayList<String> = try {
                val oWords = arrayListOf<String>()
                for (i in 0 until json.getJSONArray("otherWords").length()){
                    oWords.add(json.getJSONArray("otherWords").getString(i))
                }
                oWords
            }catch (e: JSONException){
                val array = json.getString("second").split(";").toMutableList() as ArrayList<String>
                array.onEach { it.trim() }
            }
            val typeOfWord = try {
                json.getInt("type")
            }catch (e: JSONException){
                VocabularyWord.TYPE_SYNONYM
            }

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
            val additionalInfo = try {
                json.getString("moreInfo")
            }catch (e: JSONException){
                ""
            }

            return Synonym(mainWord, otherWords, language, isIgnoreCase = ignoreCase, levelMain = levelMain, levelOther = levelOther, typeOfWord = typeOfWord, additionalInfo = additionalInfo)
        }
    }


}