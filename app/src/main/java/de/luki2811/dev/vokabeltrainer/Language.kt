package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class Language {
    var name: String
    var type: Int
    var isSpeakable: Boolean = false
    val index: JSONObject
    private val indexFile: File


    /**
     * To load a language
     */

    constructor(type: Int, context: Context) {
        this.type = type
        this.indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LANGUAGES)
        this.index = getLanguagesIndex(context)
        this.name = getNameFromType()
    }

    /**
     * To add a new language
     */

    constructor(name: String, context: Context) {
        this.name = name
        this.indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LANGUAGES)

        this.index = getLanguagesIndex(context)


        val listOfTypes = arrayListOf<Int>()

        for(i in 0..index.getJSONArray("languages").length()) {
            listOfTypes.add(index.getJSONArray("languages").getJSONObject(i).getInt("type"))
        }

        this.type = listOfTypes.maxOrNull() ?: -1

        index.put("languages", JSONArray(index.getJSONArray("languages").put(
                JSONObject()
                    .put("type", this.type)
                    .put("name", this.name)
        )))
        AppFile.writeInFile(index.toString(), indexFile)

    }

    fun refreshInIndex() {

        val index = JSONObject(AppFile.loadFromFile(indexFile))
        val indexArray = index.getJSONArray("languages")
        for(i in 0 until indexArray.length())
            if(type == indexArray.getJSONObject(i).getInt("type"))
                indexArray.getJSONObject(i).put("name", name)
        index.put("languages", indexArray)
        AppFile.writeInFile(index.toString(), indexFile)

    }

     private fun getNameFromType(): String{

         val indexArray = index.getJSONArray("languages")
         var end = "null"

         for(i in 0 until indexArray.length()) {
             if (type == indexArray.getJSONObject(i).getInt("type"))
                 end = indexArray.getJSONObject(i).getString("name")
         }
         return end
    }

    fun getShortName(): String? {
        return when(name.trim().lowercase(Locale.getDefault())) {
            "deutsch" -> "de"
            "englisch" -> "en"
            "französisch" -> "fr"
            "spanisch" -> "es"
            "russisch" -> "ru"
            "schwedisch" -> "sv"
            "norwegisch" -> "no"
            "chinesisch" -> "zh"
            "japanisch" -> "ja"
            "niederländisch" -> "nl"
            "dänisch" -> "da"
            "arabisch" -> "ar"
            "bulgarisch" -> "bg"
            "kroatisch" -> "cz"
            "finnisch" -> "fi"
            "koreanisch" -> "ko"
            "polnisch" -> "po"
            "portugisisch" -> "pt"
            "romänisch" -> "ro"
            "slovakisch" -> "sk"
            "slowenisch" -> "sl"
            "thailändisch" -> "th"
            "türkisch" -> "tr"
            "ukrainisch" -> "uk"
            "afrikanisch" -> "af"
            "tschechisch" -> "cs"

            else -> null
        }
    }


    companion object {

        const val DEFAULT_0 = "Deutsch"
        const val DEFAULT_1 = "Englisch"
        const val DEFAULT_2 = "Französisch"
        const val DEFAULT_3 = "Spanisch"
        const val DEFAULT_4 = "Russisch"
        const val DEFAULT_5 = "Schwedisch"
        const val DEFAULT_6 = "Norwegisch"
        const val DEFAULT_7 = "Chinesisch"
        const val DEFAULT_8 = "Japanisch"
        const val DEFAULT_9 = "Lateinisch"

        fun getLanguagesIndex(context: Context): JSONObject {
            val indexFile = File(context.filesDir, AppFile.NAME_FILE_INDEX_LANGUAGES)

            return try {
                JSONObject(AppFile.loadFromFile(indexFile))
            } catch (e: JSONException) {
                Log.e("Exception", "Couldn't load index from file")
                e.printStackTrace()
                JSONObject()
            }
        }

        fun getDefaultLanguageIndex(): JSONObject = JSONObject()
            .put("languages", JSONArray()
                .put(JSONObject().put("type", 0).put("name", DEFAULT_0))
                .put(JSONObject().put("type", 1).put("name", DEFAULT_1))
                .put(JSONObject().put("type", 2).put("name", DEFAULT_2))
                .put(JSONObject().put("type", 3).put("name", DEFAULT_3))
                .put(JSONObject().put("type", 4).put("name", DEFAULT_4))
                .put(JSONObject().put("type", 5).put("name", DEFAULT_5))
                .put(JSONObject().put("type", 6).put("name", DEFAULT_6))
                .put(JSONObject().put("type", 7).put("name", DEFAULT_7))
                .put(JSONObject().put("type", 8).put("name", DEFAULT_8))
                .put(JSONObject().put("type", 9).put("name", DEFAULT_9))
            )

    }
}