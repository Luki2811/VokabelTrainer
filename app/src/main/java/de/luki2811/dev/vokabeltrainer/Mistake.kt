package de.luki2811.dev.vokabeltrainer

import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Mistake {

    var word: VocabularyWord
    var wrongAnswer: String
    var typeOfPractice: Int
    var lastTimeWrong: Date = Calendar.getInstance().time
    var position: Int = 0

    constructor(json: JSONObject){
        try {

        }catch (e: JSONException)

        return
    }

    constructor(word: VocabularyWord, wrongAnswer: String, typeOfPractice: Int, lastTimeWrong: Date){
        this.word = word
        this.wrongAnswer = wrongAnswer
        this.typeOfPractice = typeOfPractice
        this.lastTimeWrong = lastTimeWrong
    }


}
