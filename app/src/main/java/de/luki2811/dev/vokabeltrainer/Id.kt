package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.io.File

@Parcelize
data class Id(var number: Int) : Parcelable {

    fun register(context: Context){
        val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_ID)
        val index = JSONObject(FileUtil.loadFromFile(indexFile))

        index.getJSONArray("index").put(number)
        FileUtil.writeInFile(index.toString(), indexFile)
    }

    fun unregister(context: Context) {
        val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_ID)
        val index = JSONObject(FileUtil.loadFromFile(indexFile))

        for (i in 0 until index.getJSONArray("index").length()) {
            if (index.getJSONArray("index").getInt(i) == this.number){
                index.getJSONArray("index").remove(i)
                FileUtil.writeInFile(index.toString(), indexFile)
                return
            }
        }
        Log.e("Id","Failed to unregister Id ($number)")
    }

    companion object{
        fun generate(context: Context): Id{
            val indexFile = File(context.filesDir, FileUtil.NAME_FILE_INDEX_ID)
            val index = JSONObject(FileUtil.loadFromFile(indexFile))
            val allIds = arrayListOf<Int>()
            for(i in 0 until index.getJSONArray("index").length()){
                allIds.add(index.getJSONArray("index").getInt(i))
            }

            val randomId: Int = (100000..999999).filter { !allIds.contains(it) }.random()

            return Id(randomId)
        }
    }
}