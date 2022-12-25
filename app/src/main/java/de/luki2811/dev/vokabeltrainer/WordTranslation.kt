package de.luki2811.dev.vokabeltrainer

import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

data class WordTranslation(var firstWord: String,
                           var firstLanguage: Locale,
                           var secondWord: ArrayList<String>,
                           var secondLanguage: Locale,
                           override var isIgnoreCase: Boolean,
                           override var alreadyUsedInExercise: Boolean = false,
                           override var level: Int = 0,
                           override var typeOfWord: Int = VocabularyWord.TYPE_TRANSLATION): VocabularyWord {

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

    override fun getAsJSON(): JSONObject {
        TODO("Not yet implemented")
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

        fun getVocabularyWord(json: JSONObject, tempFirstLanguage: Locale? = null, tempSecondLanguage: Locale? = null): WordTranslation{
            val typeOfWord = try {
                json.getInt("type")
            }
            catch (e: JSONException){
                try {
                    json.getInt("typeOfWord")
                } catch (e: JSONException) {
                    VocabularyWord.TYPE_TRANSLATION
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

            return WordTranslation(firstWord, firstLanguage, secondWord, secondLanguage, isIgnoreCase, typeOfWord).apply { this.level = level }
        }

    }


}