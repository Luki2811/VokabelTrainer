package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.os.Build
import io.github.g0dkar.qrcode.ErrorCorrectionLevel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Settings(var context: Context) {
    private val sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    var dailyObjectiveStreak: Int = sharedPreferences.getInt("dailyObjectiveStreak", 20)
    var readOutVocabularyGeneral: Boolean = sharedPreferences.getBoolean("readOutVocabularyGeneral", true)
    var useDynamicColors: Boolean = sharedPreferences.getBoolean("useDynamicColors", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    var reminderForStreak: Boolean = sharedPreferences.getBoolean("reminderForStreak", false)

    private val timeAsString = sharedPreferences.getString("reminderStreakTime", LocalTime.of(12,0).format(DateTimeFormatter.ofPattern("kk:mm")))!!
    var timeReminderStreak: LocalTime = LocalTime.of(
            timeAsString.replaceAfter(':', "").replace(":", "").toInt(),
            timeAsString.replaceBefore(':', "").replace(":", "").toInt()
        )

    var appLanguage: Locale = Locale(sharedPreferences.getString("appLanguage", Locale.ENGLISH.language)!!)
    var streakChartLengthInDays: Int = sharedPreferences.getInt("streakChartLengthInDays", 7)
    var increaseScreenBrightness: Boolean = sharedPreferences.getBoolean("increaseScreenBrightness", true)

    var correctionLevelQrCode: ErrorCorrectionLevel = when(sharedPreferences.getInt("correctionLevelQrCode", ErrorCorrectionLevel.M.value)){
        ErrorCorrectionLevel.L.value -> ErrorCorrectionLevel.L
        ErrorCorrectionLevel.M.value -> ErrorCorrectionLevel.M
        ErrorCorrectionLevel.Q.value -> ErrorCorrectionLevel.Q
        ErrorCorrectionLevel.H.value -> ErrorCorrectionLevel.H
        else -> ErrorCorrectionLevel.M
    }

    var suggestTranslation: Boolean = sharedPreferences.getBoolean("suggestTranslation", true)
    var allowShortFormInAnswer: Boolean = sharedPreferences.getBoolean("allowShortFormInAnswer", true)
    var alreadyShownStart: Boolean = sharedPreferences.getBoolean("alreadyShownStart", false)

    fun save(){
        with(sharedPreferences.edit()){
            putInt("dailyObjectiveStreak", dailyObjectiveStreak)
            putBoolean("readOutVocabularyGeneral", readOutVocabularyGeneral)
            putBoolean("useDynamicColors", useDynamicColors)
            putBoolean("reminderForStreak", reminderForStreak)
            putString("reminderStreakTime", timeReminderStreak.format(DateTimeFormatter.ofPattern("kk:mm")))
            putString("appLanguage", appLanguage.language)
            putInt("streakChartLengthInDays", streakChartLengthInDays)
            putBoolean("increaseScreenBrightness", increaseScreenBrightness)
            putInt("correctionLevelQrCode", correctionLevelQrCode.value)
            putBoolean("suggestTranslation", suggestTranslation)
            putBoolean("alreadyShownStart", alreadyShownStart)

            apply()
        }
    }
}