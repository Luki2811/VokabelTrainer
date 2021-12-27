package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONObject
import java.io.File

class Id(context: Context, var number: Int = 0) {
    init {
        if(number == 0) number = generateRandomNumber()
        // Regestrieren der ID ins Index
        val index: JSONObject =
            if (File(AppFile.NAME_FILE_INDEX_ID).exists())
                JSONObject(AppFile.loadFromFile(File(AppFile.NAME_FILE_INDEX_ID)))
            else JSONObject()
        index.getJSONArray("index").put(number)
        AppFile(AppFile.NAME_FILE_INDEX_ID).writeInFile(index.toString(), context)
    }

     private fun generateRandomNumber(): Int {
        var randomId: Int = (100000..999999).random()

        val index: JSONObject =
            if (File(AppFile.NAME_FILE_INDEX_ID).exists())
                JSONObject(AppFile.loadFromFile(File(AppFile.NAME_FILE_INDEX_ID)))
            else JSONObject()

        for(i in 0 until index.getJSONArray("index").length())
            if(index.getJSONArray("index").getInt(i) == this.number)
                randomId = generateRandomNumber()
        return randomId
    }

    fun deleteId(context: Context) {
        val index: JSONObject =
            if (File(AppFile.NAME_FILE_INDEX_ID).exists())
                JSONObject(AppFile.loadFromFile(File(AppFile.NAME_FILE_INDEX_ID)))
            else JSONObject()

        for (i in 0 until index.getJSONArray("index").length()) {
            if (index.getJSONArray("index").getInt(i) == this.number){
                index.getJSONArray("index").remove(i)
            }
        }
        AppFile(AppFile.NAME_FILE_INDEX_ID).writeInFile(index.toString(), context)
    }
}