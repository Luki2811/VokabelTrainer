package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONException
import org.json.JSONObject

class VocabularyWord() {

    lateinit var knownWord: String
    lateinit var languageKnown: Language
    lateinit var newWord: String
    lateinit var languageNew: Language
    var isIgnoreCase: Boolean = false

    var isWrong = false
    var typeWrong = 1
    var isKnownWordAskedAsAnswer = false
    var isAlreadyUsed = false


    constructor(knownWord: String,languageKnown: Language, newWord: String, languageNew: Language, isIgnoreCase: Boolean): this()  {
        this.knownWord = knownWord
        this.languageKnown = languageKnown
        this.newWord = newWord
        this.languageNew = languageNew
        this.isIgnoreCase = isIgnoreCase
    }

    constructor(json: JSONObject, context: Context) : this() {
        knownWord = json.getString("knownWord")
        languageKnown = Language(json.getInt("languageKnownType"), context)
        newWord = json.getString("newWord")
        languageNew = Language(json.getInt("languageNewType"), context)
        isIgnoreCase = json.getBoolean("isIgnoreCase")
        isKnownWordAskedAsAnswer = json.getBoolean("askKnownWord")

        try {
            isWrong = json.getBoolean("isWrong")
            typeWrong = json.getInt("typeWrong")
            isAlreadyUsed = json.getBoolean("isAlreadyUsed")
        }catch (e: JSONException){
            e.printStackTrace()
        }

    }

    fun getKnownWordList(): List<String>{
        val knownWordsList = knownWord.split(";")
        for (i in knownWordsList){
            i.trim()
        }
        return knownWordsList
    }


    fun getJson(): JSONObject{
        return JSONObject()
            .put("knownWord", knownWord)
            .put("languageKnownType", languageKnown.type)
            .put("newWord", newWord)
            .put("languageNewType", languageNew.type)
            .put("isIgnoreCase", isIgnoreCase)
            .put("isWrong", isWrong)
            .put("typeWrong", typeWrong)
            .put("askKnownWord", isKnownWordAskedAsAnswer)
            .put("isAlreadyUsed", isAlreadyUsed)
    }

}