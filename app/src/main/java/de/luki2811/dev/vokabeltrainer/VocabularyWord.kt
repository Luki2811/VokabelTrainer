package de.luki2811.dev.vokabeltrainer

import org.json.JSONObject
import java.util.*

class VocabularyWord() {
    lateinit var knownWord: String
    lateinit var languageKnown: Locale
    lateinit var newWord: String
    lateinit var languageNew: Locale
    var isIgnoreCase: Boolean = false
    var isKnownWordAskedAsAnswer = false

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
    }

    fun getKnownWordList(): List<String>{
        val knownWordsList = knownWord.split(";")
        for (i in knownWordsList){
            i.trim()
        }
        return knownWordsList
    }

    fun equalsVocabularyWord(word: VocabularyWord, controlAll: Boolean = false) = this.getJson(controlAll).toString() == word.getJson(controlAll).toString()

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
                .put("knownWord", knownWord)
                .put("languageKnownType", languageKnown.language)
                .put("newWord", newWord)
                .put("languageNewType", languageNew.language)
                .put("isIgnoreCase", isIgnoreCase)
                .put("askKnownWord", isKnownWordAskedAsAnswer)
        }else
            JSONObject()
                .put("knownWord", knownWord)
                .put("languageKnownType", languageKnown.language)
                .put("newWord", newWord)
                .put("languageNewType", languageNew.language)
    }


}