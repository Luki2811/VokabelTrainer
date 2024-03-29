package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Parcelize
data class Mistake(var word: VocabularyWord,
                   var isOtherWordAskedAsAnswer: Boolean,
                   var typeOfPractice: Int,
                   var wrongAnswer: String,
                   var position: Int,
                   var lastTimeWrong: LocalDate,
                   var isRepeated: Boolean = false): Parcelable {

    init {
        word.levelMain = 0
        word.levelOther = 0
        word.alreadyUsedInExercise = false
    }

    fun getAsJson(ignoreDate: Boolean = false): JSONObject {
        val wordAsJson = word.getAsJSON(withoutLanguage = false)
        wordAsJson.remove("isWrong")
        wordAsJson.remove("typeWrong")
        wordAsJson.remove("isAlreadyUsed")

        val dateAsString = lastTimeWrong.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

        return if(ignoreDate){
            JSONObject()
                .put("vocabularyWord", wordAsJson)
                .put("wrongAnswer", wrongAnswer)
                .put("typeOfPractice", typeOfPractice)
                .put("position", position)
                .put("askedForSecondWord", isOtherWordAskedAsAnswer)
        }else{
            JSONObject()
                .put("vocabularyWord", wordAsJson)
                .put("wrongAnswer", wrongAnswer)
                .put("typeOfPractice", typeOfPractice)
                .put("lastTimeWrong", dateAsString)
                .put("position", position)
                .put("askedForSecondWord",isOtherWordAskedAsAnswer)
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
        val all = loadAllFromFile(context)
        all.removeAll { it == this }

        FileUtil.writeInFile(JSONArray().apply {
            all.forEach{ put(it.getAsJson()) }
        }.toString(), file)
    }

    companion object{

        fun fromJson(json: JSONObject): Mistake?{
            return try {
                val askedForSecondWord = try { json.getBoolean("askedForSecondWord") } catch (e: JSONException){ false }
                val word: VocabularyWord = when(json.getJSONObject("vocabularyWord").getInt("type")){
                    VocabularyWord.TYPE_TRANSLATION -> WordTranslation.loadFromJSON(json.getJSONObject("vocabularyWord"))
                    VocabularyWord.TYPE_WORD_FAMILY -> WordFamily.loadFromJSON(json.getJSONObject("vocabularyWord"))
                    VocabularyWord.TYPE_SYNONYM, VocabularyWord.TYPE_ANTONYM -> Synonym.loadFromJSON(json.getJSONObject("vocabularyWord"))
                    else -> null
                } ?: return null
                val wrongAnswer = json.getString("wrongAnswer")
                val typeOfPractice = json.getInt("typeOfPractice")
                val lastTimeWrong = LocalDate.parse(json.getString("lastTimeWrong"), DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                val position = try {
                    json.getInt("position")
                }catch (e: JSONException){
                    -1
                }
                Mistake(word, askedForSecondWord, typeOfPractice, wrongAnswer, position, lastTimeWrong)
            }catch (e: JSONException){
                Log.e("Mistake", "Error while create Mistake ${e.printStackTrace()}")
                null
            }

        }
        fun loadAllFromFile(context: Context): ArrayList<Mistake>{
            val file = File(context.filesDir, FileUtil.NAME_FILE_LIST_WRONG_WORDS)
            val jsonArray = JSONArray(FileUtil.loadFromFile(file))
            val mistakes = arrayListOf<Mistake>()
            for(i in 0 until jsonArray.length()){
                val mistakeToAdd = fromJson(jsonArray.getJSONObject(i))
                if (mistakeToAdd != null) {
                    mistakes.add(mistakeToAdd)
                }else{
                    Log.e("Mistake", "Failed to add mistake")
                }
            }

            return mistakes
        }
    }
}
