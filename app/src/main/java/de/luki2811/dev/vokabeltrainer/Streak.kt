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

    /** var length = 0
    var xpReached = 0
        private set
    var xpGoal = 0
    var lastTimeReachedGoal: LocalDate = LocalDate.now()
    var lastTimeChecked: LocalDate = LocalDate.now()
    var isReachedToday = false
        private set

    fun addXP(value: Int) {
        val streakFile = File(context.filesDir, AppFile.NAME_FILE_STREAK)
        try {
            val streakData = JSONObject(AppFile.loadFromFile(streakFile))
            streakData.put("reachedInXPToday", value + xpReached)
            xpReached += value
            AppFile.writeInFile(streakData.toString(),streakFile)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    init {
        val streakFile = File(context.filesDir, AppFile.NAME_FILE_STREAK)
        val settings = Settings(context)
        if (streakFile.exists()) {
            try {
                val streakData = JSONObject(AppFile.loadFromFile(streakFile))
                length = streakData.getInt("lengthInDays")
                xpGoal = settings.dailyObjectiveStreak.removeSuffix("XP").toInt()
                xpReached = streakData.getInt("reachedInXPToday")
                lastTimeChecked = LocalDate.parse(streakData.getString("lastTimeChecked"))
                lastTimeReachedGoal = LocalDate.parse(streakData.getString("lastDayReachedGoal"))
                isReachedToday = streakData.getBoolean("reachedToday")

                // Check if streak is valid
                val today = LocalDate.now()
                if (lastTimeReachedGoal.isEqual(today.minusDays(1)) && !lastTimeChecked.isEqual(
                        today
                    )
                ) {
                    xpReached = 0
                    isReachedToday = false
                    lastTimeChecked = today
                } else if (!lastTimeChecked.isEqual(today)) {
                    length = 0
                    isReachedToday = false
                    xpReached = 0
                    lastTimeChecked = today
                }

                // Check if need to increase streak
                if (!isReachedToday) {
                    if (xpReached >= xpGoal) {
                        length += 1
                        lastTimeReachedGoal = today
                        isReachedToday = true
                    }
                }
                streakData.put("lengthInDays", length)
                streakData.put("reachedToday", isReachedToday)
                streakData.put("reachedInXPToday", xpReached)
                streakData.put("lastTimeChecked", lastTimeChecked)
                streakData.put("lastDayReachedGoal", lastTimeReachedGoal)
                AppFile.writeInFile(streakData.toString(), streakFile)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            try {
                val streakData = JSONObject()
                streakData.put("lengthInDays", 0)
                streakData.put("goalInXP", 50)
                streakData.put("reachedInXPToday", 0)
                val localDate = LocalDate.now()
                streakData.put("lastDayReachedGoal", LocalDate.of(2005, 11, 28))
                streakData.put("reachedToday", false)
                streakData.put("lastTimeChecked", localDate.toString())
                length = streakData.getInt("lengthInDays")
                xpGoal = streakData.getInt("goalInXP")
                xpReached = streakData.getInt("reachedInXPToday")
                isReachedToday = streakData.getBoolean("reachedToday")
                lastTimeChecked = LocalDate.parse(streakData.getString("lastTimeChecked"))
                lastTimeReachedGoal = LocalDate.parse(streakData.getString("lastDayReachedGoal"))
                AppFile.writeInFile(streakData.toString(), streakFile)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    } **/
}