package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class VocabularyGroup {

    var name: String
    var id: Id
    var languageNew: Language
    var languageKnown: Language
    lateinit var vocabulary: Array<VocabularyWord>

    private var context: Context

    constructor(name: String, languageNew: Language, languageKnown: Language, vocabulary: Array<VocabularyWord>, context: Context) {
        this.name = name
        this.id = Id(context)
        this.languageNew = languageNew
        this.languageKnown = languageKnown
        this.vocabulary = vocabulary
        this.context = context
    }

    constructor(jsonObj: JSONObject, name: String = "", context: Context, generateNewId: Boolean = false){
        if(name == "") {
            this.name = jsonObj.getString("name")
            this.id = if(generateNewId) Id(context) else Id(context, jsonObj.getInt("id"))
        }
        else {
            this.name = name
            this.id = Id(context)
        }

        try {
            this.languageKnown = Language(jsonObj.getInt("languageKnown"), context)
            this.languageNew = Language(jsonObj.getInt("languageNew"), context)

        }catch (e: JSONException){
            this.languageKnown = Language(0, context)
            this.languageNew = Language(0, context)
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

    fun addVocabularyFromVocabularyGroup(vocabularyGroup: VocabularyGroup){
        val vocabulary = ArrayList<VocabularyWord>()
        vocabulary.addAll(this.vocabulary)
        vocabulary.addAll(vocabularyGroup.vocabulary)
        this.vocabulary = vocabulary.toTypedArray()
    }

    fun refreshNameInIndex(){
        val index = JSONObject(AppFile.loadFromFile(File(context.filesDir ,AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS)))
        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                index.getJSONArray("index").getJSONObject(i).put("name", name)
        }
        AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(index.toString(),context)
    }

    fun saveInIndex(){
        if(File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).exists()){
            val index = JSONObject(AppFile.loadFromFile(File(context.filesDir ,AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(index.toString(),context)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(index.toString(),context)
        }
    }

    fun deleteFromIndex(){
        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).loadFromFile(context))
        val fieldToDelete = arrayListOf<Int>()

        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                fieldToDelete.add(i)
        }

        for(i in fieldToDelete){
            index.getJSONArray("index").remove(i)
        }

        AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(index.toString(),context)
    }


    fun getAsJson(): JSONObject{
        val jsonObj = JSONObject()
            .put("name", name)
            .put("id", id.number)
            .put("languageKnown", languageKnown.type)
            .put("languageNew", languageNew.type)
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

    fun setWordAtPos(pos: Int, voc: VocabularyWord) {
        vocabulary[pos] = voc
    }

    fun getWordAtPos(pos: Int): VocabularyWord {
        return vocabulary[pos]
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
            val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS)
            val indexAppFile = AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS)

            if (nameToCheck.length > 50)
                return 3

            if(nameToCheck.trim().isEmpty())
                return 4

            if (indexFile.exists()) {
                val indexVocabularyGroups = JSONObject(indexAppFile.loadFromFile(context)).getJSONArray("index")
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
