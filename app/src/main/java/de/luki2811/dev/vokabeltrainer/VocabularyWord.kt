package de.luki2811.dev.vokabeltrainer

import org.json.JSONException
import org.json.JSONObject
import java.util.*

data class VocabularyWord(var firstWord: String, var firstLanguage: Locale, var secondWord: String, var secondLanguage: Locale, var isIgnoreCase: Boolean, var typeOfWord: Int = TYPE_TRANSLATION, var alreadyUsedInExercise: Boolean = false, var level: Int = 0) {

    fun getFirstWordList(): List<String>{
        val firstWordList = firstWord.split(";")
        for (i in firstWordList){
            i.trim()
        }
        return firstWordList
    }

    fun getSecondWordList(): List<String>{
        val secondWordList = secondWord.split(";")
        for (i in secondWordList){
            i.trim()
        }
        return secondWordList
    }

    fun getJson(all: Boolean = true): JSONObject{
        return if(all){
            JSONObject()
                .put("type", typeOfWord)
                .put("first", firstWord)
                .put("firstLanguage", firstLanguage.language)
                .put("second", secondWord)
                .put("secondLanguage", secondLanguage.language)
                .put("ignoreCase", isIgnoreCase)
                .put("level", level)
        }else
            JSONObject()
                .put("type", typeOfWord)
                .put("first", firstWord)
                .put("second", secondWord)
                .put("ignoreCase", isIgnoreCase)
                .put("level", level)
    }

    companion object{
        const val TYPE_UNKNOWN = -1
        const val TYPE_TRANSLATION = 0
        const val TYPE_SYNONYM = 1
        const val TYPE_ANTONYM = 2
        // const val TYPE_WORD_FAMILY = 3

        fun getVocabularyWord(json: JSONObject, tempFirstLanguage: Locale? = null, tempSecondLanguage: Locale? = null): VocabularyWord{
            val typeOfWord = try {
                json.getInt("type")
            }
            catch (e: JSONException){
                try {
                    json.getInt("typeOfWord")
                } catch (e: JSONException) {
                    TYPE_TRANSLATION
                }
            }
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

            val secondWord = try {
                json.getString("second")
            } catch (e: JSONException){
                try {
                    json.getString("newWord")
                } catch (e: JSONException){
                    json.getString("new")
                }
            }
            val secondLanguage = tempSecondLanguage ?: try { Locale(json.getString("secondLanguage")) } catch (e: JSONException){ Locale(json.getString("languageNewType")) }
            val isIgnoreCase = try{ json.getBoolean("ignoreCase") } catch (e: JSONException){ json.getBoolean("isIgnoreCase") }

            val level = try { json.getInt("level") } catch (e: JSONException) { 0 }

            return VocabularyWord(firstWord, firstLanguage, secondWord, secondLanguage, isIgnoreCase, typeOfWord).apply { this.level = level }
        }

    }


}