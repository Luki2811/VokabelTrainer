package de.luki2811.dev.vokabeltrainer

import org.json.JSONArray
import org.json.JSONObject

class Exercise() {

    var type = TYPE_UNKNOWN
    var isSecondWordAskedAsAnswer: Boolean = false
    var askAllWords: Boolean = false
    var readOut: ArrayList<Boolean> = arrayListOf(false, false)
    var words: ArrayList<VocabularyWord> = arrayListOf()
    var typeOfWord: Int = VocabularyWord.TYPE_UNKNOWN
    // Result
    var isCorrect: Boolean = false

    constructor(type: Int, words: ArrayList<VocabularyWord>, read: ArrayList<Boolean>, askAllWords: Boolean, askForSecondWord: Boolean) : this() {
        this.type = type
        this.words = words
        this.typeOfWord = words[0].typeOfWord
        this.readOut = read
        this.askAllWords = askAllWords
        this.isSecondWordAskedAsAnswer = askForSecondWord
    }

    constructor(json: JSONObject): this(){
        this.type = json.getInt("type")
        this.words = arrayListOf()
        for(i in 0 until json.getJSONArray("words").length())
            this.words.add(VocabularyWord(json.getJSONArray("words").getJSONObject(i)))
        this.typeOfWord = words[0].typeOfWord
        val readOutList = json.getString("readOut").split(';')
        this.readOut.add(0, (readOutList[0] == "true"))
        this.readOut.add(1, (readOutList[1] == "true"))
        this.askAllWords = json.getBoolean("askAllWords")
        this.isSecondWordAskedAsAnswer = json.getBoolean("askForSecondWord")
        this.isCorrect = json.getBoolean("isCorrect")
    }

    fun getJson(): JSONObject{
        val wordsForJson = JSONArray()
        words.forEach { wordsForJson.put(it.getJson(true)) }
        val readOut = "${this.readOut[0]};${this.readOut[1]}"

        return JSONObject()
            .put("type", this.type)
            .put("words", wordsForJson)
            .put("readOut", readOut)
            .put("askAllWords", askAllWords)
            .put("askForSecondWord", isSecondWordAskedAsAnswer)
            .put("isCorrect", isCorrect)
    }

    companion object{
        const val TYPE_UNKNOWN = -1
        const val TYPE_TRANSLATE_TEXT = 1
        const val TYPE_CHOOSE_OF_THREE_WORDS = 2
        const val TYPE_MATCH_FIVE_WORDS = 3
    }
}