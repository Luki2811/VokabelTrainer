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
    var askedForSecondWord = false
    lateinit var wrongAnswer: String
    var typeOfPractice: Int = -1
    var lastTimeWrong: LocalDate = LocalDate.now()
    var position: Int = -1
    var isRepeated = false

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    constructor(json: JSONObject){
        try {
            this.askedForSecondWord = try { json.getBoolean("askedForSecondWord") } catch (e: JSONException){ false }
            this.word = VocabularyWord.getVocabularyWord(json.getJSONObject("vocabularyWord"))
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

    constructor(word: VocabularyWord, wrongAnswer: String, typeOfPractice: Int, lastTimeWrong: LocalDate, position: Int = -1, askedForSecondWord: Boolean){
        this.word = word
        this.wrongAnswer = wrongAnswer
        this.typeOfPractice = typeOfPractice
        this.lastTimeWrong = lastTimeWrong
        this.position = position
        this.askedForSecondWord = askedForSecondWord
    }

    fun getAsJson(ignoreDate: Boolean = false): JSONObject {
        val wordAsJson = word.getJson()
        wordAsJson.remove("isWrong")
        wordAsJson.remove("typeWrong")
        wordAsJson.remove("isAlreadyUsed")

        val dateAsString = lastTimeWrong.format(dateTimeFormatter)

        return if(ignoreDate){
            JSONObject()
                .put("vocabularyWord", wordAsJson)
                .put("wrongAnswer", wrongAnswer)
                .put("typeOfPractice", typeOfPractice)
                .put("position", position)
                .put("askedForSecondWord", askedForSecondWord)
        }else{
            JSONObject()
                .put("vocabularyWord", wordAsJson)
                .put("wrongAnswer", wrongAnswer)
                .put("typeOfPractice", typeOfPractice)
                .put("lastTimeWrong", dateAsString)
                .put("position", position)
                .put("askedForSecondWord",askedForSecondWord)
        }
    }

    fun addToFile(context: Context) {
        val file = File(context.filesDir, FileUtil.NAME_FILE_LIST_WRONG_WORDS)
        val jsonArray = JSONArray(FileUtil.loadFromFile(file))

        val allMistakes = loadAllFromFile(context)

        if(!allMistakes.contains(this)){
            jsonArray.put(this.getAsJson())
            FileUtil.writeInFile(jsonArray.toString(), file)
            return
        }else{
            allMistakes[allMistakes.indexOf(this)].lastTimeWrong = this.lastTimeWrong
            Log.i("Mistake", "Mistake already in list, update lastTimeWrong")
        }
    }

    fun removeFromFile(context: Context){
        val file = File(context.filesDir, FileUtil.NAME_FILE_LIST_WRONG_WORDS)
        val jsonArray = JSONArray(FileUtil.loadFromFile(file))

        for(i in 0 until jsonArray.length()){
            if(Mistake(jsonArray.getJSONObject(i)) == this){
                jsonArray.remove(i)
                Log.i("Mistake","Removed mistake (${this.word.secondWord}) from file")
                FileUtil.writeInFile(jsonArray.toString(), file)
                return
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        return if (other is Mistake){
            this.word == other.word
        }else{
            false
        }
    }

    override fun hashCode(): Int {
        return word.hashCode()
    }

    companion object{
        fun loadAllFromFile(context: Context): ArrayList<Mistake>{
            val file = File(context.filesDir, FileUtil.NAME_FILE_LIST_WRONG_WORDS)
            val jsonArray = JSONArray(FileUtil.loadFromFile(file))
            val mistakes = arrayListOf<Mistake>()
            for(i in 0 until jsonArray.length()){
                val mistakeToAdd = Mistake(jsonArray.getJSONObject(i))
                mistakes.add(mistakeToAdd)
            }

            return mistakes
        }
    }
}
