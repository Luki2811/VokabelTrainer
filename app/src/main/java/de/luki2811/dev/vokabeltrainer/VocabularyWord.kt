package de.luki2811.dev.vokabeltrainer

import org.json.JSONException
import org.json.JSONObject
import java.util.*

class VocabularyWord() {
    lateinit var knownWord: String
    lateinit var languageKnown: Locale
    lateinit var newWord: String
    lateinit var languageNew: Locale
    var isIgnoreCase: Boolean = false
    var isKnownWordAskedAsAnswer = false
    var isAlreadyUsed = false

    constructor(knownWord: String,languageKnown: Locale, newWord: String, languageNew: Locale, isIgnoreCase: Boolean): this()  {
        this.knownWord = knownWord
        this.languageKnown = languageKnown
        this.newWord = newWord
        this.languageNew = languageNew
        this.isIgnoreCase = isIgnoreCase
    }

    constructor(json: JSONObject) : this() {
        knownWord = json.getString("knownWord")
        languageKnown = Locale(json.getString("languageKnownType"))
        newWord = json.getString("newWord")
        languageNew = Locale(json.getString("languageNewType"))
        isIgnoreCase = json.getBoolean("isIgnoreCase")
        isKnownWordAskedAsAnswer = json.getBoolean("askKnownWord")

        try {
            isAlreadyUsed = json.getBoolean("isAlreadyUsed")
        }catch (e: JSONException){

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
            .put("languageKnownType", languageKnown.language)
            .put("newWord", newWord)
            .put("languageNewType", languageNew.language)
            .put("isIgnoreCase", isIgnoreCase)
            .put("askKnownWord", isKnownWordAskedAsAnswer)
            .put("isAlreadyUsed", isAlreadyUsed)
    }

}