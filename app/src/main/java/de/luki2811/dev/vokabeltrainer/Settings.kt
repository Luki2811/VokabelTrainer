package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class Settings(var context: Context) {
    val jsonObject = JSONObject(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_SETTINGS)))
    var dailyObjectiveStreak: String = try {
        jsonObject.getString("dailyObjectiveStreak")
    }catch (e: JSONException){
        e.printStackTrace()
        "20XP"
    }
    var readOutVocabularyGeneral: Boolean = try {
        jsonObject.getBoolean("readOutVocabularyGeneral")
    }catch (e: JSONException){
        e.printStackTrace()
        true
    }


    fun saveSettingsInFile(){
        val new = JSONObject()
            .put("dailyObjectiveStreak",dailyObjectiveStreak)
            .put("readOutVocabularyGeneral", readOutVocabularyGeneral)

        AppFile.writeInFile(new.toString(), File(context.filesDir, AppFile.NAME_FILE_SETTINGS))
    }
}