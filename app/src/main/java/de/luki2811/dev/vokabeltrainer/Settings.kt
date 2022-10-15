package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.os.Build
import io.github.g0dkar.qrcode.ErrorCorrectionLevel
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class Settings(var context: Context) {
    val jsonObject = if (File(context.filesDir, AppFile.NAME_FILE_SETTINGS).exists()) JSONObject(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_SETTINGS))) else JSONObject()
    var dailyObjectiveStreak: Int = try {
        jsonObject.getInt("dailyObjectiveStreak")
    }catch (e: JSONException){
        e.printStackTrace()
        20
    }
    var readOutVocabularyGeneral: Boolean = try {
        jsonObject.getBoolean("readOutVocabularyGeneral")
    }catch (e: JSONException){
        e.printStackTrace()
        true
    }
    var useDynamicColors: Boolean = try {
        jsonObject.getBoolean("useDynamicColors")
    }catch (e: JSONException){
        e.printStackTrace()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
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
    var streakChartLengthInDays: Int = try {
        jsonObject.getInt("streakChartLengthInDays")
    }catch (e: JSONException){
        7
    }
    var increaseScreenBrightness: Boolean = try {
        jsonObject.getBoolean("increaseScreenBrightness")
    }catch (e: JSONException){
        true
    }
    var correctionLevelQrCode: ErrorCorrectionLevel = try {
        when(jsonObject.getInt("correctionLevelQrCode")){
            ErrorCorrectionLevel.L.value -> ErrorCorrectionLevel.L
            ErrorCorrectionLevel.M.value -> ErrorCorrectionLevel.M
            ErrorCorrectionLevel.Q.value -> ErrorCorrectionLevel.Q
            ErrorCorrectionLevel.H.value -> ErrorCorrectionLevel.H
            else -> ErrorCorrectionLevel.M
        }
    }catch (e: JSONException){
        ErrorCorrectionLevel.M
    }
    var suggestTranslation: Boolean = try {
        jsonObject.getBoolean("suggestTranslation")
    }catch (e: JSONException){
        true
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
            .put("streakChartLengthInDays", streakChartLengthInDays)
            .put("increaseScreenBrightness", increaseScreenBrightness)
            .put("correctionLevelQrCode", correctionLevelQrCode.value)
        AppFile.writeInFile(new.toString(), File(context.filesDir, AppFile.NAME_FILE_SETTINGS))
    }
}