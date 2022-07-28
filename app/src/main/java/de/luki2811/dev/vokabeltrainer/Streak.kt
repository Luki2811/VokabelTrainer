package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Streak(val context: Context) {
    var isReachedToday = false
        private set
    var lengthInDay = 0
        private set
    var xpGoal = 0
        private set
    var xpToday = 0

    init {
        try {
            val streakData = JSONArray(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_STREAK)))
            val dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            for(i in 0 until streakData.length()){
                val data = streakData.getJSONObject(i)
                if(data.getString("date").equals(dateToday.toString())){
                    xpToday = data.getInt("xp")
                }
            }
        }catch (e: JSONException){
            e.printStackTrace()
        }
        refresh()
    }

    fun refresh(){
        xpGoal = JSONObject(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_SETTINGS))).getString("dailyObjectiveStreak").removeSuffix("XP").toInt()

        var streakData = JSONArray(AppFile.loadFromFile(File(context.filesDir, AppFile.NAME_FILE_STREAK)))
        val dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        isReachedToday = xpToday >= xpGoal

        lengthInDay = if(isReachedToday) streakData.length() else streakData.length()-1

        // Check if streak is still ok
        var deleteOld = true
        val dateYesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        for(i in 0 until streakData.length()){
            val data = streakData.getJSONObject(i)
            if(data.getString("date").equals(dateYesterday)){
                deleteOld = data.getInt("xp") < data.getInt("goal")
            }
        }
        if(deleteOld) {
            Log.i("Streak","Deleted streak")
            streakData = JSONArray()
        }

        // Save data in date-object if exist already
        for(i in 0 until streakData.length()){
            val data = streakData.getJSONObject(i)
            if(data.getString("date").equals(dateToday)){
                streakData.getJSONObject(i).put("xp", xpToday)
                streakData.getJSONObject(i).put("goal", xpGoal)
                AppFile.writeInFile(streakData.toString(), File(context.filesDir, AppFile.NAME_FILE_STREAK))
                return
            }
        }

        // Create new date-object if it doesn't exist
        streakData.put(JSONObject().put("date", dateToday).put("xp", xpToday).put("goal", xpGoal))
        AppFile.writeInFile(streakData.toString(), File(context.filesDir, AppFile.NAME_FILE_STREAK))
    }
}