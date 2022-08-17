package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Mistake {
    lateinit var word: VocabularyWord
    lateinit var wrongAnswer: String
    var typeOfPractice: Int = -1
    var lastTimeWrong: LocalDate = LocalDate.now()
    var position: Int = -1
    var isRepeated = false
    var alreadyUsed = false

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    constructor(json: JSONObject){
        try {
            this.word = VocabularyWord(json.getJSONObject("vocabularyWord"))
            this.wrongAnswer = json.getString("wrongAnswer")
            this.typeOfPractice = json.getInt("typeOfPractice")
            this.lastTimeWrong = LocalDate.parse(json.getString("lastTimeWrong"), dateTimeFormatter)
            try {
                this.position = json.getInt("position")
            }catch (e: JSONException){
                this.position = -1
            }

        }catch (e: JSONException){
            Log.e("Mistake", "Error while create Mistake ${e.printStackTrace()}")
        }
    }

    constructor(word: VocabularyWord, wrongAnswer: String, typeOfPractice: Int, lastTimeWrong: LocalDate, position: Int = -1){
        this.word = word
        this.wrongAnswer = wrongAnswer
        this.typeOfPractice = typeOfPractice
        this.lastTimeWrong = lastTimeWrong
        this.position = position
    }

    fun getAsJson(): JSONObject {
        val wordAsJson = word.getJson()
        wordAsJson.remove("isWrong")
        wordAsJson.remove("typeWrong")
        wordAsJson.remove("isAlreadyUsed")

        val dateAsString = lastTimeWrong.format(dateTimeFormatter)

        return JSONObject()
            .put("vocabularyWord", wordAsJson)
            .put("wrongAnswer", wrongAnswer)
            .put("typeOfPractice", typeOfPractice)
            .put("lastTimeWrong", dateAsString)
            .put("position", position)
    }

    fun addToFile(context: Context) {
        val file = File(context.filesDir, AppFile.NAME_FILE_LIST_WRONG_WORDS)
        val jsonArray = JSONArray(AppFile.loadFromFile(file))
        jsonArray.put(getAsJson())
        AppFile.writeInFile(jsonArray.toString(), file)
    }

    fun removeFromFile(context: Context){
        val file = File(context.filesDir, AppFile.NAME_FILE_LIST_WRONG_WORDS)
        val jsonArray = JSONArray(AppFile.loadFromFile(file))
        for(i in 0 until jsonArray.length() - 1){
            val mistake = Mistake(jsonArray.getJSONObject(i))
            if(mistake.lastTimeWrong == this.lastTimeWrong && mistake.wrongAnswer == this.wrongAnswer){
                jsonArray.remove(i)
                Log.i("Mistake","Removed a mistake from file")
                AppFile.writeInFile(jsonArray.toString(), file)
                return
            }
        }
    }

    companion object{
        fun loadAllFromFile(context: Context): ArrayList<Mistake>{
            val file = File(context.filesDir, AppFile.NAME_FILE_LIST_WRONG_WORDS)
            val jsonArray = JSONArray(AppFile.loadFromFile(file))
            val mistakes = arrayListOf<Mistake>()
            for(i in 0 until jsonArray.length()){
                mistakes.add(Mistake(jsonArray.getJSONObject(i)))
            }
            return mistakes
        }
    }
}
