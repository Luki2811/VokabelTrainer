package de.luki2811.dev.vokabeltrainer

import android.content.Context
import de.luki2811.dev.vokabeltrainer.AppFile.Companion.loadFromFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// Jede Vokabelgruppe bekommt eine id, mit der sie wiedererkannt wird, falls der Name geändert wird.
// Vokabelgruppen sind wie Lektionen, nur das sie NUR Vokabeln speichern können

class VocabularyGroup{

    var name: String
    var vocabulary: Array<VocabularyWord> = arrayOf()
    var id: Id

    constructor(name: String, vocabulary: Array<VocabularyWord>, context: Context){
        this.name = name
        this.id = Id(context)
        this.vocabulary = vocabulary
    }

    constructor(jsonObj: JSONObject, context: Context) {
        name = jsonObj.getString("name")
        id = Id(context,jsonObj.getInt("id"))
        for (i in 0..jsonObj.getJSONArray("vocabulary").length()){
            vocabulary[i] = VocabularyWord(
                jsonObj.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                jsonObj.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                jsonObj.getJSONArray("vocabulary").getJSONObject(i).getBoolean("ignoreCase"))
        }
    }

    fun saveInIndex(context: Context){
        if(File(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).exists()){
            val index = JSONObject(loadFromFile(File(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(index.toString(),context)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(index.toString(),context)
        }
    }


    fun getAsJson(): JSONObject{
        val jsonObj = JSONObject().put("name", name).put("id", id)
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

}


// TODO: Methode erstellen, damit Vokabelgruppen geteilt werden können



    // TODO: NFC teilen hinzufügen https://developer.android.com/training/beam-files
