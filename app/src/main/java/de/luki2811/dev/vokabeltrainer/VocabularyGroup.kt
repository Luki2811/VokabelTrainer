package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class VocabularyGroup {

    var name: String
    var id: Id
    var languageNew: Locale
    var languageKnown: Locale
    lateinit var vocabulary: Array<VocabularyWord>

    private var context: Context
    private val indexFile: File

    constructor(name: String, languageNew: Locale, languageKnown: Locale, vocabulary: Array<VocabularyWord>, context: Context) {
        this.name = name
        this.id = Id(context)
        this.languageNew = languageNew
        this.languageKnown = languageKnown
        this.vocabulary = vocabulary
        this.context = context
        this.indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)
    }

    constructor(jsonObj: JSONObject, name: String = "", context: Context, generateNewId: Boolean = false){
        indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        if(name == "") {
            this.name = jsonObj.getString("name")
            this.id = if(generateNewId) Id(context) else Id(context, jsonObj.getInt("id"))
        }
        else {
            this.name = name
            this.id = Id(context)
        }

        try {
            this.languageKnown = Locale(jsonObj.getString("languageKnown"))
            this.languageNew = Locale(jsonObj.getString("languageNew"))

        }catch (e: JSONException){
            this.languageKnown = Locale.GERMAN
            this.languageNew = Locale.ENGLISH
        }

        this.context = context



        try {
            val vocabularyTemp = ArrayList<VocabularyWord>()
            for (i in 0 until jsonObj.getJSONArray("vocabulary").length()) {
                vocabularyTemp.add(
                    VocabularyWord(
                        jsonObj.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                        this.languageKnown,
                        jsonObj.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                        this.languageNew,
                        jsonObj.getJSONArray("vocabulary").getJSONObject(i).getBoolean("ignoreCase")
                    )
                )
                vocabulary = vocabularyTemp.toTypedArray()
            }
        }catch (e: JSONException){
            vocabulary = arrayOf()
        }
    }

    /** fun addVocabularyFromVocabularyGroup(vocabularyGroup: VocabularyGroup){
        val vocabulary = ArrayList<VocabularyWord>()
        vocabulary.addAll(this.vocabulary)
        vocabulary.addAll(vocabularyGroup.vocabulary)
        this.vocabulary = vocabulary.toTypedArray()
    } **/

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
        val jsonObj = JSONObject()
            .put("name", name)
            .put("id", id.number)
            .put("type", AppFile.TYPE_FILE_VOCABULARY_GROUP)
            .put("languageKnown", languageKnown.language)
            .put("languageNew", languageNew.language)
        val jsonArray = JSONArray()
        for (i in vocabulary.indices) {
            val voc = JSONObject()
            voc.put("ignoreCase", vocabulary[i].isIgnoreCase)
            voc.put("new", vocabulary[i].newWord)
            voc.put("native", vocabulary[i].knownWord)
            jsonArray.put(voc)
        }
        return jsonObj.put("vocabulary", jsonArray)
    }

    fun getRandomWord(): VocabularyWord {
        val random = (Math.random() * vocabulary.size + 1).toInt()
        return vocabulary[random-1]
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
