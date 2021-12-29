package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Id(var context: Context, var number: Int = 0) {
    init {
        if(number == 0){
            number = generateRandomNumber()

            // Regestrieren der ID ins Index
            val index: JSONObject =
                if (File(context.filesDir,AppFile.NAME_FILE_INDEX_ID).exists())
                    JSONObject(AppFile(AppFile.NAME_FILE_INDEX_ID).loadFromFile(context))
                else JSONObject().put("index", JSONArray())
            index.getJSONArray("index").put(number)
            println(index.toString())
            AppFile(AppFile.NAME_FILE_INDEX_ID).writeInFile(index.toString(), context)
        }
    }

     private fun generateRandomNumber(): Int {
        var randomId: Int = (100000..999999).random()

        val index: JSONObject =
            if (File(context.filesDir,AppFile.NAME_FILE_INDEX_ID).exists())
                JSONObject(AppFile(AppFile.NAME_FILE_INDEX_ID).loadFromFile(context))
            else JSONObject().put("index", JSONArray())

        for(i in 0 until index.getJSONArray("index").length())
            if(index.getJSONArray("index").getInt(i) == this.number)
                randomId = generateRandomNumber()
        return randomId
    }

    fun deleteId() {
        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_ID).loadFromFile(context))

        for (i in 0 until index.getJSONArray("index").length()) {
            if (index.getJSONArray("index").getInt(i) == this.number){
                index.getJSONArray("index").remove(i)
            }
        }
        number = 0
        AppFile(AppFile.NAME_FILE_INDEX_ID).writeInFile(index.toString(), context)
    }
}