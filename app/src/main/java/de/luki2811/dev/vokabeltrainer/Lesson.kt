package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import de.luki2811.dev.vokabeltrainer.Exercise.Companion.TYPE_CHOOSE_OF_THREE_WORDS
import de.luki2811.dev.vokabeltrainer.Exercise.Companion.TYPE_MATCH_FIVE_WORDS
import de.luki2811.dev.vokabeltrainer.Exercise.Companion.TYPE_TRANSLATE_TEXT
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class Lesson {
    lateinit var name: String
    lateinit var id: Id
    lateinit var vocabularyGroupIds: Array<Int>
    var alreadyUsedWords = arrayListOf<VocabularyWord>()
    var typesOfLesson: ArrayList<Int> = arrayListOf()
    var settingReadOutBoth: Boolean = true
    var askForSecondWords: Boolean = false
    var isFavorite: Boolean = false
    var numberOfExercises = 10
    var askForAllWords: Boolean = false
    private val context: Context

    constructor(
        name: String,
        vocabularyGroupIds: Array<Int>,
        context: Context,
        settingReadOutBoth: Boolean = true,
        askOnlyNewWords: Boolean = false,
        typesOfLesson: ArrayList<Int> = arrayListOf(1,2,3),
        isFavorite: Boolean = false,
        numberOfExercises: Int = 10,
        alreadyUsedWords: ArrayList<VocabularyWord> = arrayListOf(),
    ) {
        this.name = name
        this.id = Id(context)
        this.vocabularyGroupIds = vocabularyGroupIds
        this.settingReadOutBoth = settingReadOutBoth
        this.askForSecondWords = askOnlyNewWords
        this.context = context
        this.typesOfLesson = typesOfLesson
        this.isFavorite = isFavorite
        this.numberOfExercises = numberOfExercises
        this.alreadyUsedWords = alreadyUsedWords
    }

    constructor(json: JSONObject, context: Context) {
        this.context = context
        try {
            name = json.getString("name")
            id = Id(context, json.getInt("id"))
            val groupIds = ArrayList<Int>()
            for(i in 0 until json.getJSONArray("vocabularyGroupIds").length())
               groupIds.add(i, json.getJSONArray("vocabularyGroupIds").getInt(i))
            vocabularyGroupIds = groupIds.toTypedArray()
            settingReadOutBoth = json.getJSONObject("settings").getBoolean("readOutBoth")
            askForSecondWords = try {
                json.getJSONObject("settings").getBoolean("askOnlyNewWords")
            }catch (e: JSONException){
                e.printStackTrace()
                false
            }

            askForAllWords = try {
                json.getJSONObject("settings").getBoolean("askForAllWords")
            }catch (e: JSONException){
                false
            }

            try {
                if(json.getJSONObject("settings").getBoolean("useType1"))
                    typesOfLesson.add(TYPE_TRANSLATE_TEXT)
                if(json.getJSONObject("settings").getBoolean("useType2"))
                    typesOfLesson.add(TYPE_CHOOSE_OF_THREE_WORDS)
                if(json.getJSONObject("settings").getBoolean("useType3"))
                    typesOfLesson.add(TYPE_MATCH_FIVE_WORDS)
            }catch (e: JSONException){
                e.printStackTrace()
                typesOfLesson = arrayListOf(1,2,3)
            }

            isFavorite = try {
                json.getJSONObject("settings").getBoolean("favorite")
            }catch (e: JSONException){
                e.printStackTrace()
                false
            }

            numberOfExercises = try {
                json.getJSONObject("settings").getInt("numberOfExercises")
            }catch (e: JSONException){
                Log.w("Lesson","No value numberOfExercises in $name (${id.number}) => set default" )
                10
            }

            alreadyUsedWords = try {
                val list = arrayListOf<VocabularyWord>()
                val array = json.getJSONArray("alreadyUsedWords")
                for(i in 0 until array.length()){
                    list.add(VocabularyWord(array.getJSONObject(i)))
                }
                list
            }catch (e: JSONException){
                arrayListOf()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * @return converted name as file name
     */
    fun getShareFileName(): String{
        val stringBuilder = StringBuilder()
        for(i in name.indices){
            if(name[i] == '/' || name[i] == ' ' || name[i] == '\\' || name[i] == '\"' || name[i] == '|' || name[i] == '*' || name[i] == '?' ||
                name[i] == '<' || name[i] == '>' || name[i] == ':' || name[i] == '+' || name[i] == '[' || name[i] == ']' || name[i] == '\'' ||
                name[i] == ':' || name[i] == ';' || name[i] == '.'){
                stringBuilder.append("_")
            }else
                stringBuilder.append(name[i].lowercase())
        }
        return stringBuilder.apply { append("_les.json") }.toString()
    }

    /**
     * Saves a lesson with name and ID in the index
     */
    fun saveInIndex(){
        val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
        if(indexFile.exists()){
            val index = JSONObject(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            AppFile.writeInFile(index.toString(),indexFile)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            AppFile.writeInFile(index.toString(),indexFile)
        }
    }

    /**
     * Delete the lesson's ID and name from the index
     */

    fun deleteFromIndex(){
        val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
        val index = JSONObject(AppFile.loadFromFile(indexFile))
        var temp = -1
        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                temp = i
        }
        if(temp != -1)
            index.getJSONArray("index").remove(temp)
        AppFile.writeInFile(index.toString(),indexFile)
    }

    /**
     * Get a lesson as JSONObject
     */

    fun getAsJson(): JSONObject{
        val jsonObj = JSONObject()
            .put("name", name)
            .put("id", id.number)
        val jsonArr = JSONArray()
        for(i in vocabularyGroupIds.indices){
            jsonArr.put(i, vocabularyGroupIds[i])
        }
        jsonObj.put("vocabularyGroupIds", jsonArr)
        val listAsString = arrayListOf<JSONObject>()
        alreadyUsedWords.forEach {
            listAsString.add(it.getJson())
        }
        jsonObj.put("alreadyUsedWords", JSONArray(listAsString))
        jsonObj.put("settings",
            JSONObject()
                .put("readOutBoth", settingReadOutBoth)
                .put("askOnlyNewWords", askForSecondWords)
                .put("useType1", typesOfLesson.contains(TYPE_TRANSLATE_TEXT))
                .put("useType2", typesOfLesson.contains(TYPE_CHOOSE_OF_THREE_WORDS))
                .put("useType3", typesOfLesson.contains(TYPE_MATCH_FIVE_WORDS))
                .put("favorite", isFavorite)
                .put("numberOfExercises", numberOfExercises)
                .put("askForAllWords", askForAllWords)
        )
        return jsonObj
    }

    /**
     * Methode to SHARE a lesson
     * This also loads and share the vocabulary groups as JSON
     * Without Id, alreadyUsedWords, isFavorite and vocabularyGroupIds
     */

    fun export(): JSONObject{
        val vocabularyInOneJson = JSONArray()
        val vocabularyGroups = loadVocabularyGroups()
        for (i in vocabularyGroups){
            vocabularyInOneJson.put(i.getAsJson())
        }

        return JSONObject()
            .put("name",this.name)
            .put("type", AppFile.TYPE_FILE_LESSON)
            .put("settings",
            JSONObject()
                .put("readOutBoth", settingReadOutBoth)
                .put("askOnlyNewWords", askForSecondWords)
                .put("useType1", typesOfLesson.contains(TYPE_TRANSLATE_TEXT))
                .put("useType2", typesOfLesson.contains(TYPE_CHOOSE_OF_THREE_WORDS))
                .put("useType3", typesOfLesson.contains(TYPE_MATCH_FIVE_WORDS))
                .put("numberOfExercises", numberOfExercises)
                .put("askForAllWords", askForAllWords)
            )
            .put("vocabularyGroups", vocabularyInOneJson)
    }

    /**
     * Load vocabulary groups
     */

    fun loadVocabularyGroups(): ArrayList<VocabularyGroup>{
        val vocabularyGroups: ArrayList<VocabularyGroup> = arrayListOf()
        for(i in this.vocabularyGroupIds){
            VocabularyGroup.loadFromFileWithId(Id(context,i), context)?.let {
                vocabularyGroups.add(it)
            }
        }
        return vocabularyGroups
    }

    /**
     * Saves the lesson in a file with ID
     */

    fun saveInFile() {
        var file = File(context.filesDir, "lessons")
        file.mkdirs()
        file = File(file, id.number.toString() + ".json" )
        AppFile.writeInFile(getAsJson().toString(), file)
    }

    companion object{
        /**
         * Checks, if a lesson has a valid name
         */
        fun isNameValid(context: Context, name: String): Int {
            val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)

            if (name.length > 50)
                return 3

            if(name.trim().isEmpty())
                return 4

            /** if(isAppFile(textInputEditText.text.toString().trim()))
                return 2

            if (textInputEditText.text.toString().trim().contains("/") ||
                textInputEditText.text.toString().trim().contains("<") ||
                textInputEditText.text.toString().trim().contains(">") ||
                textInputEditText.text.toString().trim().contains("\\") ||
                textInputEditText.text.toString().trim().contains("|") ||
                textInputEditText.text.toString().trim().contains("*") ||
                textInputEditText.text.toString().trim().contains(":") ||
                textInputEditText.text.toString().trim().contains("\"") ||
                textInputEditText.text.toString().trim().contains("?")
            ) return 1

             **/

            if (indexFile.exists()) {

                val indexLessons =
                    JSONObject(AppFile.loadFromFile(indexFile)).getJSONArray("index")
                for (i in 0 until indexLessons.length()) {
                    if (indexLessons.getJSONObject(i).getString("name") == name.trim())
                        return 2
                }
            }
            return 0
        }
    }
}