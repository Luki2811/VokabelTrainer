package de.luki2811.dev.vokabeltrainer

import android.content.Context
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class Lesson {
    // TODO: Bearbeiten der Lektionen, das sie VokabelGruppen speichern (nur ID) und nicht die Wörter

    lateinit var name: String
    lateinit var id: Id
    lateinit var languageKnow: Language
    lateinit var languageNew: Language
    lateinit var vocabularyGroupIds: Array<Int>

    constructor(name: String, languageKnow: Language, languageNew: Language, vocabularyGroupIds: Array<Int>, context: Context) {
        this.name = name
        this.id = Id(context)
        this.languageKnow = languageKnow
        this.languageNew = languageNew
        this.vocabularyGroupIds = vocabularyGroupIds
    }

    constructor(json: JSONObject, context: Context) {
        try {
            name = json.getString("name")
            id = Id(context, json.getInt("id"))
            languageKnow = Language(json.getInt("languageNative"))
            languageNew = Language(json.getInt("languageNew"))
            val groupIds = ArrayList<Int>()
            for(i in 0 until json.getJSONArray("vocabularyGroupIds").length())
               groupIds.add(i, json.getJSONArray("vocabularyGroupIds").getInt(i))
            vocabularyGroupIds = groupIds.toTypedArray()

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun saveInIndex(context: Context){
        if(File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS).exists()){
            val index = JSONObject(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)))
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            index.getJSONArray("index").put(index.getJSONArray("index").length(), toIndexJson)
            AppFile(AppFile.NAME_FILE_INDEX_LESSONS).writeInFile(index.toString(),context)
        }else{
            val toIndexJson = JSONObject().put("name", name).put("id", id.number)
            val index = JSONObject().put("index", JSONArray().put(0, toIndexJson))
            AppFile(AppFile.NAME_FILE_INDEX_LESSONS).writeInFile(index.toString(),context)
        }
    }

    fun deleteFromIndex(context: Context){
        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_LESSONS).loadFromFile(context))

        for(i in 0 until index.getJSONArray("index").length()){
            if(index.getJSONArray("index").getJSONObject(i).getInt("id") == id.number)
                index.getJSONArray("index").remove(i)
        }
        AppFile(AppFile.NAME_FILE_INDEX_LESSONS).writeInFile(index.toString(),context)
    }

    fun getAsJson(): JSONObject{
        val jsonObj = JSONObject()
            .put("name", name)
            .put("id", id.number)
            .put("languageNative", languageKnow.type)
            .put("languageNew", languageNew.type)
        val jsonArr = JSONArray()
        for(i in vocabularyGroupIds.indices){
            jsonArr.put(i, vocabularyGroupIds[i])
        }
        return jsonObj.put("vocabularyGroupIds", jsonArr)
    }

    companion object{
        fun isNameValid(context: Context, textInputEditText: TextInputEditText): Int {
            // val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
            // val indexDatei = AppFile(AppFile.NAME_FILE_INDEX_LESSONS)

            if (textInputEditText.text.toString().length > 50)
                return 3

            if(textInputEditText.text.toString().trim().isEmpty())
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

            if (indexFile.exists()) {

                val indexLessons =
                    JSONObject(indexDatei.loadFromFile(context)).getJSONArray("index")
                for (i in 0 until indexLessons.length()) {
                    if (indexLessons.getJSONObject(i).getString("name") == textInputEditText.text.toString().trim())
                        return 2

                }
            }**/
            return 0
        }
    }
}