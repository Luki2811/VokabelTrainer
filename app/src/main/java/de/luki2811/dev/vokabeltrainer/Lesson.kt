package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import de.luki2811.dev.vokabeltrainer.AppFile.Companion.isAppFile
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class Lesson {
    var name: String = ""
    var languageKnow: Language = Language(0)
    var languageNew: Language = Language(0)
    // TODO: Bearbeiten der Lektionen, das sie VokabelGruppen speichern (nur ID) und nicht die Wörter
    var vocs: Array<VocabularyWord> = arrayOf()
    var count = 0

    constructor(
        name: String = "",
        count: Int = 0,
        languageKnow: Language = Language(0),
        languageNew: Language = Language(0),
        vocs: Array<VocabularyWord> = arrayOf()
    ) {
        this.name = name
        this.languageKnow = languageKnow
        this.languageNew = languageNew
        this.vocs = vocs
        this.count = count
    }

    constructor(json: JSONObject) {
        try {
            name = json.getString("name")
            languageKnow = Language(json.getInt("languageNative"))
            languageNew = Language(json.getInt("languageNew"))
            vocs = Array(json.getJSONArray("vocabulary").length()){ VocabularyWord("","",false ) }
            for (i in 0 until json.getJSONArray("vocabulary").length()) {
                vocs[i] = VocabularyWord(
                    json.getJSONArray("vocabulary").getJSONObject(i).getString("native"),
                    json.getJSONArray("vocabulary").getJSONObject(i).getString("new"),
                    json.getJSONArray("vocabulary").getJSONObject(i).getBoolean("ignoreCase")
                )
            }
            count = json.getInt("count")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    val randomWord: VocabularyWord
        get() {
            val random = (Math.random() * count + 1).toInt()
            return vocs[random - 1]
        }

    fun getWordAtPos(pos: Int): VocabularyWord {
        return vocs[pos]
    }

    fun isNameValid(context: Context): Boolean{

        if(name.length >= 50){
            Toast.makeText(context, context.getString(R.string.err_name_too_long_max, 50), Toast.LENGTH_LONG).show()
            return false
        }

        if (name.contains("/") ||
            name.contains("<") ||
            name.contains(">") ||
            name.contains("\\") ||
            name.contains("|") ||
            name.contains("*") ||
            name.contains(":") ||
            name.contains("\"") ||
            name.contains("?")
        ) {
            Toast.makeText(context, context.getString(R.string.err_name_contains_wrong_letter), Toast.LENGTH_SHORT).show()
            return false
        }

        if (name.equals("streak", ignoreCase = true) ||
            name.equals("settings", ignoreCase = true) ||
            name.equals("indexLections", ignoreCase = true)
        ){
            Toast.makeText(context, context.getString(R.string.err_name_not_available), Toast.LENGTH_LONG).show()
            return false
        }

        try {
            val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
            val indexDatei = AppFile(indexFile.name)
            val indexJson = JSONObject(indexDatei.loadFromFile(context))
            val indexArray = indexJson.getJSONArray("index")

            for (i in 0 until indexArray.length()) {
                if (indexArray.getJSONObject(i).getString("name") == name){
                    Toast.makeText(context, context.getString(R.string.err_name_already_taken), Toast.LENGTH_SHORT).show()
                    return false
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return true
    }

    fun setWordAtPos(pos: Int, voc: VocabularyWord) {
        vocs[pos] = voc
    }

    val lessonAsJson: JSONObject?
        get() {
            val lektionAsJSON = JSONObject()
            return try {
                lektionAsJSON.put("name", name)
                lektionAsJSON.put("count", count)
                lektionAsJSON.put("languageNative", languageKnow.type)
                lektionAsJSON.put("languageNew", languageNew.type)
                val jsonArray = JSONArray()
                for (i in vocs.indices) {
                    val voc = JSONObject()
                    voc.put("ignoreCase", vocs[i].isIgnoreCase)
                    voc.put("new", vocs[i].newWord)
                    voc.put("native", vocs[i].knownWord)
                    jsonArray.put(voc)
                }
                lektionAsJSON.put("vocabulary", jsonArray)
                lektionAsJSON
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }

    companion object{
        fun isNameValid(context: Context, textInputEditText: TextInputEditText): Int {
            val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
            val indexDatei = AppFile(AppFile.NAME_FILE_INDEX_LESSONS)

            if (textInputEditText.text.toString().length > 50)
                return 3

            if(textInputEditText.text.toString().trim().isEmpty())
                return 4

            if(isAppFile(textInputEditText.text.toString().trim()))
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
            }
            return 0
        }
    }
}