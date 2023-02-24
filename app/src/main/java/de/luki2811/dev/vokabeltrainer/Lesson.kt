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
                  var vocabularyGroups: ArrayList<VocabularyGroup>,
                  var typesOfExercises: ArrayList<Int> = arrayListOf(TYPE_TRANSLATE_TEXT, TYPE_CHOOSE_OF_THREE_WORDS, TYPE_MATCH_FIVE_WORDS),
                  var readOut: ArrayList<Pair<Int, Boolean>>,
                  var askForAllWords: Boolean,
                  var isOnlyMainWordAskedAsAnswer: Boolean,
                  var isFavorite: Boolean = false,
                  var numberOfExercises: Int,
                  var typesOfWordToPractice: ArrayList<Int> = arrayListOf(VocabularyWord.TYPE_ANTONYM, VocabularyWord.TYPE_SYNONYM, VocabularyWord.TYPE_TRANSLATION, VocabularyWord.TYPE_WORD_FAMILY)): Exportable, Parcelable {

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
     * Methode to get the JSON of this lesson
     * @param toExport If true it also loads and share the vocabulary groups as JSON without ID, isFavorite and vocabularyGroupIds
     * @return JSONObject of lesson
     */

    fun getAsJson(toExport: Boolean = false, context: Context? = null): JSONObject{
        return JSONObject().apply {
            put("name", name)

            if(toExport && context != null) {
                put("type", type)
                put("vocabularyGroups", JSONArray().apply {
                    for (i in this@Lesson.vocabularyGroups){
                        this.put(i.getAsJson())
                    }
                })
            }else {
                put("id", id.number)
                put("vocabularyGroupIds", JSONArray().apply {
                    for(i in 0 until vocabularyGroups.size){
                        this.put(i, vocabularyGroups[i].id.number)
                    }
                })
            }

            put("settings", JSONObject().apply {
                put("readOutFirstWords", readOut.contains(Pair(READ_OTHER_LANGUAGE, true)))
                put("readOutSecondWords", readOut.contains(Pair(READ_MAIN_LANGUAGE, true)))
                put("askOnlyNewWords", isOnlyMainWordAskedAsAnswer)
                put("useType1", typesOfExercises.contains(TYPE_TRANSLATE_TEXT))
                put("useType2", typesOfExercises.contains(TYPE_CHOOSE_OF_THREE_WORDS))
                put("useType3", typesOfExercises.contains(TYPE_MATCH_FIVE_WORDS))
                if(!toExport){
                    put("favorite", isFavorite)
                }
                put("numberOfExercises", numberOfExercises)
                put("askForAllWords", askForAllWords)
                put("typesOfWordToPractice", JSONArray().apply {
                    if(typesOfWordToPractice.contains(VocabularyWord.TYPE_TRANSLATION))
                        put(VocabularyWord.TYPE_TRANSLATION)
                    if(typesOfWordToPractice.contains(VocabularyWord.TYPE_SYNONYM))
                        put(VocabularyWord.TYPE_SYNONYM)
                    if(typesOfWordToPractice.contains(VocabularyWord.TYPE_ANTONYM))
                        put(VocabularyWord.TYPE_ANTONYM)
                    if(typesOfWordToPractice.contains(VocabularyWord.TYPE_WORD_FAMILY))
                        put(VocabularyWord.TYPE_WORD_FAMILY)
                })
            })
        }
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

        const val READ_MAIN_LANGUAGE = 10
        const val READ_OTHER_LANGUAGE = 11

        /**
         * Create a lesson from a JSONObject
         * @return
         */

         fun fromJSON(json: JSONObject, context: Context, generateNewId: Boolean, alternativeIdsOfVocGroups: ArrayList<Id> = arrayListOf()): Lesson?{
            try {
                val name = json.getString("name")
                val id = if(generateNewId) {
                    Id.generate(context).apply { register(context) }
                } else{
                    Id(json.getInt("id"))
                }
                val groups: ArrayList<VocabularyGroup> = if(alternativeIdsOfVocGroups.isEmpty()){
                    val list = ArrayList<VocabularyGroup>()
                    for(i in 0 until json.getJSONArray("vocabularyGroupIds").length()){
                        val group = VocabularyGroup.loadFromFileWithId(Id(json.getJSONArray("vocabularyGroupIds").getInt(i)), context)
                        if(group != null)
                            list.add(group)
                        else
                            Log.e("Lesson","Failed to add vocGroup")
                    }
                    list
                }else{
                    val list = ArrayList<VocabularyGroup>()
                    for (i in alternativeIdsOfVocGroups){
                        list.add(VocabularyGroup.loadFromFileWithId(i, context)!!)
                    }
                    list
                }


                val readOut: ArrayList<Pair<Int, Boolean>> = try {
                    if(json.getJSONObject("settings").getBoolean("readOutBoth")) arrayListOf(
                        READ_MAIN_LANGUAGE to true, READ_OTHER_LANGUAGE to false) else arrayListOf(READ_MAIN_LANGUAGE to true, READ_OTHER_LANGUAGE to true)
                }catch (e: JSONException){
                    val tempArr = arrayListOf<Pair<Int, Boolean>>()
                    tempArr.add(READ_OTHER_LANGUAGE to json.getJSONObject("settings").getBoolean("readOutFirstWords"))
                    tempArr.add(READ_MAIN_LANGUAGE to json.getJSONObject("settings").getBoolean("readOutSecondWords"))
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

                val typesOfWordToPractice = try {
                    val array = arrayListOf<Int>()
                    for (i in 0 until json.getJSONObject("settings").getJSONArray("typesOfWordToPractice").length()){
                        array.add(json.getJSONObject("settings").getJSONArray("typesOfWordToPractice").getInt(i))
                    }
                    array
                }catch (e: JSONException){
                    arrayListOf(VocabularyWord.TYPE_TRANSLATION, VocabularyWord.TYPE_SYNONYM, VocabularyWord.TYPE_ANTONYM, VocabularyWord.TYPE_WORD_FAMILY)
                }

                return Lesson(name, id, groups, typesOfLesson, readOut, askForAllWords, askForSecondWordsOnly, isFavorite, numberOfExercises, typesOfWordToPractice)

            } catch (e: JSONException) {
                e.printStackTrace()
                return null
            }


        }

        fun loadAllLessons(context: Context): ArrayList<Lesson>{
            val indexAsJson = JSONObject(FileUtil.loadFromFile(File(context.filesDir, FileUtil.NAME_FILE_INDEX_LESSONS)))
            val allLessons = ArrayList<Lesson>()

            try {
                for(i in 0 until indexAsJson.getJSONArray("index").length()){
                    var file = File(context.filesDir, "lessons")
                    file.mkdirs()
                    file = File(file, indexAsJson.getJSONArray("index").getJSONObject(i).getInt("id").toString() + ".json" )
                    val jsonOfVocGroup = JSONObject(FileUtil.loadFromFile(file))
                    allLessons.add(fromJSON(jsonOfVocGroup, context, false)!!)
                }
            }catch (e: JSONException){
                e.printStackTrace()
            }
            return allLessons
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