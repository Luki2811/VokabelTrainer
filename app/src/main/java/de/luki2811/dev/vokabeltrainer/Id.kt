package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Id(var context: Context, var number: Int = 0) {
    private val indexFile: File = File(context.filesDir,FileUtil.NAME_FILE_INDEX_ID)

    init {
        if(number == 0){
            number = generateRandomNumber()

            // Register Id to index

            val index: JSONObject =
                if (indexFile.exists())
                    JSONObject(FileUtil.loadFromFile(indexFile))
                else JSONObject().put("index", JSONArray())
            index.getJSONArray("index").put(number)
            FileUtil.writeInFile(index.toString(), indexFile)
        }
    }

     private fun generateRandomNumber(): Int {
         var randomId: Int = (100000..999999).random()

         val index: JSONObject =
            if (indexFile.exists())
                JSONObject(FileUtil.loadFromFile(indexFile))
            else JSONObject().put("index", JSONArray())

         for(i in 0 until index.getJSONArray("index").length()){
             if(index.getJSONArray("index").getInt(i) == randomId) {
                 randomId = generateRandomNumber()
             }
         }

         return randomId
    }

    fun deleteId() {
        val index = JSONObject(FileUtil.loadFromFile(indexFile))
        var temp = -1
        for (i in 0 until index.getJSONArray("index").length()) {
            if (index.getJSONArray("index").getInt(i) == this.number){
                temp = i
            }
        }
        if(temp != -1)
            index.getJSONArray("index").remove(temp)
        number = 0
        FileUtil.writeInFile(index.toString(), indexFile)
    }
}