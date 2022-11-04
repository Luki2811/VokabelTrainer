package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.os.Parcelable
import android.util.Log
import de.luki2811.dev.vokabeltrainer.Exercise.Companion.TYPE_CHOOSE_OF_THREE_WORDS
import de.luki2811.dev.vokabeltrainer.Exercise.Companion.TYPE_MATCH_FIVE_WORDS
import de.luki2811.dev.vokabeltrainer.Exercise.Companion.TYPE_TRANSLATE_TEXT
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

@Parcelize
data class Lesson(var name: String,
                  var id: Id,
                  var vocabularyGroupIds: ArrayList<Int>,
                  var typesOfExercises: ArrayList<Int> = arrayListOf(TYPE_TRANSLATE_TEXT, TYPE_CHOOSE_OF_THREE_WORDS, TYPE_MATCH_FIVE_WORDS),
                  var readOut: ArrayList<Boolean>,
                  var askForAllWords: Boolean,
                  var askForSecondWordsOnly: Boolean,
                  var isFavorite: Boolean = false,
                  var numberOfExercises: Int): Exportable, Parcelable {

    @IgnoredOnParcel
    override val type: Int = Exportable.TYPE_LESSON

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
    fun saveInIndex(context: Context){
        val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_LESSONS)
        if(indexFile.exists()){
            val index = JSONObject(FileUtil.loadFromFile(File(context.filesDir, FileUtil.NAME_FILE_INDEX_LESSONS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            FileUtil.writeInFile(index.toString(),indexFile)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            FileUtil.writeInFile(index.toString(),indexFile)
        }
    }

    /**
     * Delete the lesson's ID and name from the index
     */

    fun deleteFromIndex(context: Context){
        val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_LESSONS)
        val index = JSONObject(FileUtil.loadFromFile(indexFile))
        var temp = -1
        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                temp = i
        }
        if(temp != -1)
            index.getJSONArray("index").remove(temp)
        FileUtil.writeInFile(index.toString(),indexFile)
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
        jsonObj.put("alreadyUsedWords", JSONArray(listAsString))
        jsonObj.put("settings",
            JSONObject()
                .put("readOutFirstWords", readOut[0])
                .put("readOutSecondWords", readOut[1])
                .put("askOnlyNewWords", askForSecondWordsOnly)
                .put("useType1", typesOfExercises.contains(TYPE_TRANSLATE_TEXT))
                .put("useType2", typesOfExercises.contains(TYPE_CHOOSE_OF_THREE_WORDS))
                .put("useType3", typesOfExercises.contains(TYPE_MATCH_FIVE_WORDS))
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

    fun export(context: Context): JSONObject {
        val vocabularyInOneJson = JSONArray()
        val vocabularyGroups = loadVocabularyGroups(context)
        for (i in vocabularyGroups){
            vocabularyInOneJson.put(i.getAsJson())
        }

        return JSONObject()
            .put("name",this.name)
            .put("type", type)
            .put("settings",
            JSONObject()
                .put("readOutBoth", readOut)
                .put("askOnlyNewWords", askForSecondWordsOnly)
                .put("useType1", typesOfExercises.contains(TYPE_TRANSLATE_TEXT))
                .put("useType2", typesOfExercises.contains(TYPE_CHOOSE_OF_THREE_WORDS))
                .put("useType3", typesOfExercises.contains(TYPE_MATCH_FIVE_WORDS))
                .put("numberOfExercises", numberOfExercises)
                .put("askForAllWords", askForAllWords)
            )
            .put("vocabularyGroups", vocabularyInOneJson)

    }

    /**
     * Load vocabulary groups
     */

    fun loadVocabularyGroups(context: Context): ArrayList<VocabularyGroup>{
        val vocabularyGroups: ArrayList<VocabularyGroup> = arrayListOf()
        for(i in this.vocabularyGroupIds){
            VocabularyGroup.loadFromFileWithId(Id(i), context)?.let {
                vocabularyGroups.add(it)
            }
        }
        return vocabularyGroups
    }

    /**
     * Saves the lesson in a file with ID
     */

    fun saveInFile(context: Context) {
        var file = File(context.filesDir, "lessons")
        file.mkdirs()
        file = File(file, id.number.toString() + ".json" )
        FileUtil.writeInFile(getAsJson().toString(), file)
    }

    companion object{

        const val MAX_LINES = 3
        const val MAX_CHARS = 50

        const val VALID = 0
        const val INVALID_TOO_MANY_CHARS = -1
        const val INVALID_TOO_MANY_LINES = -2
        const val INVALID_EMPTY = -3
        const val INVALID_NAME_ALREADY_USED = -4

        /**
         * Create a lesson from a JSONObject
         * @return
         */

        fun fromJSON(json: JSONObject, context: Context, registerId: Boolean): Lesson?{
            try {
                val name = json.getString("name")
                val id = Id(json.getInt("id"))
                if(registerId) id.register(context)
                val groupIds = ArrayList<Int>()
                for(i in 0 until json.getJSONArray("vocabularyGroupIds").length())
                    groupIds.add(i, json.getJSONArray("vocabularyGroupIds").getInt(i))

                val readOut = try {
                    if(json.getJSONObject("settings").getBoolean("readOutBoth")) arrayListOf(false, true) else arrayListOf(true, true)
                }catch (e: JSONException){
                    val tempArr = arrayListOf<Boolean>()
                    tempArr.add(0, json.getJSONObject("settings").getBoolean("readOutFirstWords"))
                    tempArr.add(1, json.getJSONObject("settings").getBoolean("readOutSecondWords"))
                    tempArr
                }
                val askForSecondWordsOnly = try {
                    json.getJSONObject("settings").getBoolean("askOnlyNewWords")
                }catch (e: JSONException){
                    e.printStackTrace()
                    false
                }

                val askForAllWords = try {
                    json.getJSONObject("settings").getBoolean("askForAllWords")
                }catch (e: JSONException){
                    false
                }

                var typesOfLesson = ArrayList<Int>()

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

                val isFavorite = try {
                    json.getJSONObject("settings").getBoolean("favorite")
                }catch (e: JSONException){
                    e.printStackTrace()
                    false
                }

                val numberOfExercises = try {
                    json.getJSONObject("settings").getInt("numberOfExercises")
                }catch (e: JSONException){
                    Log.w("Lesson","No value numberOfExercises in $name (${id.number}) => set default" )
                    10
                }

                return Lesson(name, id, groupIds, typesOfLesson, readOut, askForAllWords, askForSecondWordsOnly, isFavorite, numberOfExercises)

            } catch (e: JSONException) {
                e.printStackTrace()
                return null
            }


        }

        /**
         * Checks, if a lesson has a valid name
         */
        fun isNameValid(context: Context, name: String): Int {
            val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_LESSONS)

            if (name.length > MAX_CHARS)
                return INVALID_TOO_MANY_CHARS

            if(name.lines().size > MAX_LINES)
                return INVALID_TOO_MANY_LINES

            if(name.trim().isEmpty())
                return INVALID_EMPTY

            if (indexFile.exists()) {
                val indexLessons =
                    JSONObject(FileUtil.loadFromFile(indexFile)).getJSONArray("index")
                for (i in 0 until indexLessons.length()) {
                    if (indexLessons.getJSONObject(i).getString("name") == name.trim())
                        return INVALID_NAME_ALREADY_USED
                }
            }
            return VALID
        }
    }
}