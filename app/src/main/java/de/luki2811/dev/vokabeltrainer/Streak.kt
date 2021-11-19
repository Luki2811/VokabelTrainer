package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class Streak(var context: Context) {
    var length = 0
    var xpReached = 0
        private set
    var xpGoal = 0
    var lastTimeReachedGoal: LocalDate = LocalDate.now()
    var lastTimeChecked: LocalDate = LocalDate.now()
    var isReachedToday = false
    fun addXP(value: Int) {
        val streakDatei = Datei(Datei.NAME_FILE_STREAK)
        try {
            val streakData = JSONObject(streakDatei.loadFromFile(context))
            streakData.put("reachedInXPToday", value + xpReached)
            xpReached += value
            streakDatei.writeInFile(streakData.toString(), context)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    init {
        val streakFile = File(context.filesDir, Datei.NAME_FILE_STREAK)
        val streakDatei = Datei(Datei.NAME_FILE_STREAK)
        if (streakFile.exists()) {
            try {
                val streakData = JSONObject(streakDatei.loadFromFile(context))
                length = streakData.getInt("lengthInDays")
                xpGoal = streakData.getInt("goalInXP")
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
                streakDatei.writeInFile(streakData.toString(), context)
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
                streakDatei.writeInFile(streakData.toString(), context)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}