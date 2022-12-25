package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class VocabularyGroup: Exportable {

    var name: String
    var id: Id
    var firstLanguage: Locale
    var secondLanguage: Locale
    var vocabulary = ArrayList<WordTranslation>()
    override val type = Exportable.TYPE_VOCABULARY_GROUP

    private var context: Context
    private val indexFile: File

    constructor(name: String, firstLanguage: Locale, secondLanguage: Locale, vocabulary: ArrayList<WordTranslation>, context: Context, id: Id? = null) {
        this.name = name
        this.id = id ?: Id.generate(context).apply { register(context) }
        this.secondLanguage = secondLanguage
        this.firstLanguage = firstLanguage
        this.vocabulary = vocabulary
        this.context = context
        this.indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)
    }

    constructor(json: JSONObject, context: Context, name: String = "", generateNewId: Boolean = false){
        indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        if(name == "") {
            this.name = json.getString("name")
            this.id = if(generateNewId) Id.generate(context).apply { register(context) } else Id(json.getInt("id"))
        }
        else {
            this.name = name
            this.id = Id.generate(context).apply { register(context) }
        }

        try {
            this.firstLanguage = try {
                Locale(json.getString("firstLanguage"))
            }catch (e: JSONException){
                Locale(json.getString("languageKnown"))
            }
            this.secondLanguage = try {
                Locale(json.getString("secondLanguage"))
            }catch (e: JSONException){
                Locale(json.getString("languageNew"))
            }

        }catch (e: JSONException){
            this.firstLanguage = Locale.GERMAN
            this.secondLanguage = Locale.ENGLISH
        }

        this.context = context

        for (i in 0 until json.getJSONArray("vocabulary").length()){
            vocabulary.add(WordTranslation.getVocabularyWord(json.getJSONArray("vocabulary").getJSONObject(i), firstLanguage, secondLanguage))
        }
    }

    fun export(): JSONObject {
        return getAsJson()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        return if (other is VocabularyGroup){
            this.getAsJson().toString() == other.getAsJson().toString()
        }else{
            false
        }
    }

    override fun hashCode(): Int {
        return getAsJson().hashCode()
    }

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
        return stringBuilder.apply { append("_voc.json") }.toString()
    }

    fun refreshNameInIndex(){
        val index = JSONObject(FileUtil.loadFromFile(File(context.filesDir ,FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)))
        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                index.getJSONArray("index").getJSONObject(i).put("name", name)
        }
        FileUtil.writeInFile(index.toString(),indexFile)
    }

    fun saveInIndex(){
        if(File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS).exists()){
            val index = JSONObject(FileUtil.loadFromFile(File(context.filesDir ,FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            FileUtil.writeInFile(index.toString(),indexFile)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            FileUtil.writeInFile(index.toString(),indexFile)
        }
    }

    fun deleteFromIndex(){
        val index = JSONObject(FileUtil.loadFromFile(indexFile))
        val fieldToDelete = arrayListOf<Int>()

        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                fieldToDelete.add(i)
        }

        for(i in fieldToDelete){
            index.getJSONArray("index").remove(i)
        }

        FileUtil.writeInFile(index.toString(),indexFile)
    }


    fun getAsJson(): JSONObject{
        val jsonArray = JSONArray()
        vocabulary.forEach { jsonArray.put(it.getJson(false)) }

        return JSONObject()
            .put("name", name)
            .put("id", id.number)
            .put("type", type)
            .put("firstLanguage", firstLanguage.language)
            .put("secondLanguage", secondLanguage.language)
            .put("vocabulary", jsonArray)
    }

    /**
     * Creates a file with the ID of VocabularyGroup
     */
    fun saveInFile() {
        if(id.number == 0){
            Log.e("Error", "Couldn't save vocabulary group \"${this.name}\", because ID is 0")
            return
        }
        var file = File(context.filesDir, "vocabularyGroups")
        file.mkdirs()
        file = File(file, id.number.toString() + ".json" )
        FileUtil.writeInFile(getAsJson().toString(), file)
    }

    fun resetLevels() {
        Log.i("VocabularyGroup", "Delete all levels of vocabularyGroup \"$name\" (${id.number})")
        vocabulary.forEach {
            it.level = 0
        }
        saveInFile()
    }

    companion object{

        const val MAX_LINES = 3
        const val MAX_CHARS = 50

        const val VALID = 0
        const val INVALID_TOO_MANY_CHARS = -1
        const val INVALID_TOO_MANY_LINES = -2
        const val INVALID_EMPTY = -3
        const val INVALID_NAME_ALREADY_USED = -4

        fun loadFromFileWithId(id: Id, context: Context): VocabularyGroup?{
            var file = File(context.filesDir, "vocabularyGroups")
            file.mkdirs()
            file = File(file, id.number.toString() + ".json" )
            return if(file.exists())
                VocabularyGroup(JSONObject(FileUtil.loadFromFile(file)), context = context)
            else null
        }


        fun isNameValid(context: Context, nameToCheck: String, ignoreName: String = ""): Int {
            val indexAppFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)

            if(nameToCheck.lines().size > MAX_LINES) {
                return INVALID_TOO_MANY_LINES
            }

            if (nameToCheck.length > MAX_CHARS)
                return INVALID_TOO_MANY_CHARS

            if(nameToCheck.trim().isEmpty())
                return INVALID_EMPTY

            if (indexAppFile.exists()) {
                val indexVocabularyGroups = JSONObject(FileUtil.loadFromFile(indexAppFile)).getJSONArray("index")
                for (i in 0 until indexVocabularyGroups.length()) {
                    if ((indexVocabularyGroups.getJSONObject(i).getString("name") == nameToCheck.trim()) && (nameToCheck.trim() != ignoreName)) {
                        return INVALID_NAME_ALREADY_USED
                    }
                }
            }
            return VALID
        }
    }
}
