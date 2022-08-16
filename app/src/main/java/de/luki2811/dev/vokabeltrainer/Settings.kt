package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class Settings(var context: Context) {
    val jsonObject = if (File(context.filesDir, AppFile.NAME_FILE_SETTINGS).exists()) JSONObject(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_SETTINGS))) else JSONObject()
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
    // TODO: Replace with appTheme = THEME_DYNAMIC_COLORS
    var useDynamicColors: Boolean = try {
        jsonObject.getBoolean("useDynamicColors")
    }catch (e: JSONException){
        e.printStackTrace()
        true
    }
    var reminderForStreak: Boolean = try {
        jsonObject.getBoolean("reminderForStreak")
    }catch (e: JSONException){
        e.printStackTrace()
        false
    }
    var timeReminderStreak: LocalTime = try {
        val timeAsString = jsonObject.getString("reminderStreakTime")
        LocalTime.of(
            timeAsString.replaceAfter(':', "").replace(":", "").toInt(),
            timeAsString.replaceBefore(':', "").replace(":", "").toInt()
        )
    }catch (e: JSONException){
        LocalTime.of(12,0)
    }
    var appLanguage: Locale = try {
        Locale(jsonObject.getString("appLanguage"))
    }catch (e: JSONException){
        Locale.ENGLISH
    }
    var numberOfExercisesToPracticeMistakes: Int = try {
        jsonObject.getInt("numberOfExercisesToPracticeMistakes")
    }catch (e:JSONException){
        5
    }
    var readOnlyNewWordsPracticeMistake: Boolean = try {
        jsonObject.getBoolean("readOnlyNewWordsPracticeMistake")
    }catch (e: JSONException){
        false
    }

    fun saveSettingsInFile(){
        val new = JSONObject()
            .put("dailyObjectiveStreak",dailyObjectiveStreak)
            .put("readOutVocabularyGeneral", readOutVocabularyGeneral)
            .put("useDynamicColors", useDynamicColors)
            .put("reminderForStreak", reminderForStreak)
            .put("reminderStreakTime", timeReminderStreak.format(DateTimeFormatter.ofPattern("kk:mm")))
            .put("appLanguage", appLanguage.language)
            .put("numberOfExercisesToPracticeMistakes", numberOfExercisesToPracticeMistakes)
            .put("readOnlyNewWordsPracticeMistake", readOnlyNewWordsPracticeMistake)
        Log.i("Settings", new.toString())
        AppFile.writeInFile(new.toString(), File(context.filesDir, AppFile.NAME_FILE_SETTINGS))
    }
}