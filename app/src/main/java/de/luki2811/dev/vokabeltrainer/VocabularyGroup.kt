package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class VocabularyGroup {

    var name: String
    var id: Id
    var firstLanguage: Locale
    var secondLanguage: Locale
    var vocabulary = ArrayList<VocabularyWord>()

    private var context: Context
    private val indexFile: File

    constructor(name: String, firstLanguage: Locale, secondLanguage: Locale, vocabulary: ArrayList<VocabularyWord>, context: Context, id: Id? = null) {
        this.name = name
        this.id = id ?: Id(context)
        this.secondLanguage = secondLanguage
        this.firstLanguage = firstLanguage
        this.vocabulary = vocabulary
        this.context = context
        this.indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)
    }

    constructor(json: JSONObject, context: Context, name: String = "", generateNewId: Boolean = false){
        indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        if(name == "") {
            this.name = json.getString("name")
            this.id = if(generateNewId) Id(context) else Id(context, json.getInt("id"))
        }
        else {
            this.name = name
            this.id = Id(context)
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
            vocabulary.add(VocabularyWord.getVocabularyWord(json.getJSONArray("vocabulary").getJSONObject(i), firstLanguage, secondLanguage))
        }
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
        val index = JSONObject(AppFile.loadFromFile(File(context.filesDir ,AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)))
        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                index.getJSONArray("index").getJSONObject(i).put("name", name)
        }
        AppFile.writeInFile(index.toString(),indexFile)
    }

    fun saveInIndex(){
        if(File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS).exists()){
            val index = JSONObject(AppFile.loadFromFile(File(context.filesDir ,AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            AppFile.writeInFile(index.toString(),indexFile)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            AppFile.writeInFile(index.toString(),indexFile)
        }
    }

    fun deleteFromIndex(){
        val index = JSONObject(AppFile.loadFromFile(indexFile))
        val fieldToDelete = arrayListOf<Int>()

        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                fieldToDelete.add(i)
        }

        for(i in fieldToDelete){
            index.getJSONArray("index").remove(i)
        }

        AppFile.writeInFile(index.toString(),indexFile)
    }


    fun getAsJson(): JSONObject{
        val jsonArray = JSONArray()
        vocabulary.forEach { jsonArray.put(it.getJson(false)) }

        return JSONObject()
            .put("name", name)
            .put("id", id.number)
            .put("type", AppFile.TYPE_FILE_VOCABULARY_GROUP)
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
        AppFile.writeInFile(getAsJson().toString(), file)
    }

    fun resetLevels() {
        Log.i("VocabularyGroup", "Delete all levels of vocabularyGroup \"$name\" (${id.number})")
        vocabulary.forEach {
            it.level = 0
        }
        saveInFile()
    }

    companion object{
        fun loadFromFileWithId(id: Id, context: Context): VocabularyGroup?{
            var file = File(context.filesDir, "vocabularyGroups")
            file.mkdirs()
            file = File(file, id.number.toString() + ".json" )
            return if(file.exists())
                VocabularyGroup(JSONObject(AppFile.loadFromFile(file)), context = context)
            else null
        }


        fun isNameValid(context: Context, nameToCheck: String, ignoreName: String = ""): Int {
            val indexAppFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)

            if (nameToCheck.length > 50)
                return 3

            if(nameToCheck.trim().isEmpty())
                return 4

            if (indexAppFile.exists()) {
                val indexVocabularyGroups = JSONObject(AppFile.loadFromFile(indexAppFile)).getJSONArray("index")
                for (i in 0 until indexVocabularyGroups.length()) {
                    if ((indexVocabularyGroups.getJSONObject(i).getString("name") == nameToCheck.trim()) && (nameToCheck.trim() != ignoreName)) {
                        return 2
                    }
                }
            }
            return 0
        }
    }

}
