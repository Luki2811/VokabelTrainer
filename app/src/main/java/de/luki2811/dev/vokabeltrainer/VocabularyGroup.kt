package de.luki2811.dev.vokabeltrainer

import android.content.Context
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// Jede Vokabelgruppe bekommt eine id, mit der sie wiedererkannt wird, falls der Name geändert wird.
// Vokabelgruppen sind wie Lektionen, nur das sie NUR Vokabeln speichern können

// TODO: Methode erstellen, damit Vokabelgruppen geteilt werden können
// TODO: NFC teilen hinzufügen https://developer.android.com/training/beam-files

class VocabularyGroup{

    var name: String
    var vocabulary: Array<VocabularyWord>
    var id: Id

    constructor(name: String, vocabulary: Array<VocabularyWord>, context: Context){
        this.name = name
        this.id = Id(context)
        this.vocabulary = vocabulary
    }

    constructor(jsonObj: JSONObject, context: Context) {
        name = jsonObj.getString("name")
        id = Id(context,jsonObj.getInt("id"))
        val vocabularyTemp = ArrayList<VocabularyWord>()
        for (i in 0 until jsonObj.getJSONArray("vocabulary").length()){
            vocabularyTemp.add(
                VocabularyWord(
                    jsonObj.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                    jsonObj.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                    jsonObj.getJSONArray("vocabulary").getJSONObject(i).getBoolean("ignoreCase")
                )
            )
        }
        vocabulary = vocabularyTemp.toTypedArray()
    }

    fun addVocabularyFromVocabularyGroup(vocabularyGroup: VocabularyGroup){
        val vocabulary = ArrayList<VocabularyWord>()
        vocabulary.addAll(this.vocabulary)
        vocabulary.addAll(vocabularyGroup.vocabulary)
        this.vocabulary = vocabulary.toTypedArray()
    }

    fun saveInIndex(context: Context){
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

    fun deleteFromIndex(context: Context){
        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).loadFromFile(context))

        var temp = -1
        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                temp = i
        }
        if(temp != -1)
            index.getJSONArray("index").remove(temp)

        AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(index.toString(),context)
    }


    fun getAsJson(): JSONObject{
        val jsonObj = JSONObject().put("name", name).put("id", id.number)
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

    companion object{

        fun loadFromFileWithId(id: Id, context: Context): VocabularyGroup?{
            var file = File(context.filesDir, "vocabularyGroups")
            file.mkdirs()
            file = File(file, id.number.toString() + ".json" )
            return if(file.exists())
                VocabularyGroup(JSONObject(AppFile.loadFromFile(file)), context)
            else null
        }


        fun isNameValid(context: Context, textInputEditText: TextInputEditText): Int {
            val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS)
            val indexDatei = AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS)

            if (textInputEditText.text.toString().length > 50)
                return 3

             if(textInputEditText.text.toString().trim().isEmpty())
                return 4
            /**
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

            if(AppFile.isAppFile(textInputEditText.text.toString().trim()))
                return 2
             **/
            if (indexFile.exists()) {
                val indexVocabularyGroups = JSONObject(indexDatei.loadFromFile(context)).getJSONArray("index")
                for (i in 0 until indexVocabularyGroups.length()) {
                    if (indexVocabularyGroups.getJSONObject(i).getString("name") == textInputEditText.text.toString().trim()) {
                        return 2
                    }
                }
            }
            return 0
        }
    }

}
