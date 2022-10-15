package de.luki2811.dev.vokabeltrainer

import org.json.JSONException
import org.json.JSONObject
import java.util.*

class VocabularyWord() {
    var typeOfWord: Int = TYPE_UNKNOWN

    lateinit var firstWord: String
    lateinit var firstLanguage: Locale

    lateinit var secondWord: String
    lateinit var secondLanguage: Locale

    var isIgnoreCase: Boolean = false

    constructor(firstWord: String, firstLanguage: Locale, secondWord: String, secondLanguage: Locale, isIgnoreCase: Boolean, typeOfWord: Int = TYPE_TRANSLATION): this(){
        this.firstWord = firstWord
        this.firstLanguage = firstLanguage
        this.secondWord = secondWord
        this.secondLanguage = secondLanguage
        this.isIgnoreCase = isIgnoreCase
        this.typeOfWord = typeOfWord
    }

    constructor(json: JSONObject, tempFirstLanguage: Locale? = null, tempSecondLanguage: Locale? = null) : this() {
        typeOfWord = try {
            json.getInt("type")
        }
        catch (e: JSONException){
            try {
                json.getInt("typeOfWord")
            } catch (e: JSONException) {
                TYPE_TRANSLATION
            }
        }
        firstWord = try {
            json.getString("first")
        } catch (e: JSONException){
            try {
                json.getString("knownWord")
            } catch (e: JSONException){
                json.getString("native")
            }
        }
        firstLanguage = tempFirstLanguage ?: try { Locale(json.getString("firstLanguage")) } catch (e: JSONException){ Locale(json.getString("languageKnownType")) }

        secondWord = try {
            json.getString("second")
        } catch (e: JSONException){
            try {
                json.getString("newWord")
            } catch (e: JSONException){
                json.getString("new")
            }
        }
        secondLanguage = tempSecondLanguage ?: try { Locale(json.getString("secondLanguage")) } catch (e: JSONException){ Locale(json.getString("languageNewType")) }
        isIgnoreCase = try{ json.getBoolean("ignoreCase") } catch (e: JSONException){ json.getBoolean("isIgnoreCase") }

        // isKnownWordAskedAsAnswer = json.getBoolean("askKnownWord")
    }

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

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        return if (other is VocabularyWord){
            this.getJson(true).toString() == other.getJson(true).toString()
        }else{
            false
        }
    }

    override fun hashCode(): Int {
        return this.getJson(true).toString().hashCode()
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
        }else
            JSONObject()
                .put("type", typeOfWord)
                .put("first", firstWord)
                .put("second", secondWord)
                .put("ignoreCase", isIgnoreCase)
    }

    companion object{
        const val TYPE_UNKNOWN = -1
        const val TYPE_TRANSLATION = 0
        const val TYPE_SYNONYM = 1
        const val TYPE_ANTONYM = 2
        // const val TYPE_WORD_FAMILY = 3
    }


}