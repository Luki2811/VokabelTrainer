package de.luki2811.dev.vokabeltrainer

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Streak(val context: Context) {
    var lengthInDay = 0
        private set
    var xpGoal = 0
        private set
    var xpToday = 0

    // Set last date to 0
    var allDaysXp = arrayListOf<Pair<LocalDate, Int>>()
        private set

    init {
        try {
            val streakData = JSONArray(FileUtil.loadFromFile(File(context.filesDir, FileUtil.NAME_FILE_STREAK)))
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
        xpGoal = Settings(context).dailyObjectiveStreak

        var streakData = JSONArray(FileUtil.loadFromFile(File(context.filesDir, FileUtil.NAME_FILE_STREAK)))
        val dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        lengthInDay = if(xpToday >= xpGoal) streakData.length() else streakData.length()-1

        // Check if streak is still ok
        var deleteOld = true
        val dateYesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        for(i in 0 until streakData.length()){
            if(streakData.getJSONObject(i).getString("date").equals(dateYesterday)){
                deleteOld = streakData.getJSONObject(i).getInt("xp") < streakData.getJSONObject(i).getInt("goal")
            }
        }

        if(deleteOld) { streakData = JSONArray() }

        // Save data in date-object if exist already
        for(i in 0 until streakData.length()){
            val data = streakData.getJSONObject(i)
            if(data.getString("date").equals(dateToday)){
                streakData.getJSONObject(i).put("xp", xpToday)
                streakData.getJSONObject(i).put("goal", xpGoal)
                FileUtil.writeInFile(streakData.toString(), File(context.filesDir, FileUtil.NAME_FILE_STREAK))
                setMap()
                return
            }
        }

        // Create new date-object if it doesn't exist
        streakData.put(JSONObject().put("date", dateToday).put("xp", xpToday).put("goal", xpGoal))
        FileUtil.writeInFile(streakData.toString(), File(context.filesDir, FileUtil.NAME_FILE_STREAK))
        setMap()

    }

    private fun setMap(){
        val streakData = JSONArray(FileUtil.loadFromFile(File(context.filesDir, FileUtil.NAME_FILE_STREAK)))

        for(i in 0 until streakData.length()){
            val date = LocalDate.parse(streakData.getJSONObject(i).getString("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val xp = streakData.getJSONObject(i).getInt("xp")
            allDaysXp.add(date to xp)
        }

        allDaysXp.sortWith(compareByDescending { it.first })
    }

    companion object{
        fun getStreakGoals() = arrayListOf(
            "10XP",
            "20XP",
            "30XP",
            "40XP",
            "50XP",
            "60XP",
            "70XP",
            "80XP",
            "90XP",
            "100XP",
            "110XP",
            "120XP",
            "130XP",
            "140XP",
            "150XP",
            "160XP",
            "170XP",
            "180XP",
            "190XP",
            "200XP",
            "210XP",
            "220XP",
            "230XP",
            "240XP",
            "250XP",
            "260XP",
            "270XP",
            "280XP",
            "290XP",
            "300XP"
        )

        fun getRandomStreak(durationInDays: Int): JSONArray{
            val json = JSONArray()

            if(durationInDays == 0){
                val dayJson = JSONObject()
                    .put("date", LocalDate.now())
                    .put("xp", 0)
                    .put("goal", 50)
                return json.put(dayJson)
            }

            for(i in 0 until durationInDays){
                val dayJson = JSONObject()
                    .put("date", LocalDate.now().minusDays(i.toLong()))
                    .put("xp", (10..150).random())
                    .put("goal", 10)
                json.put(dayJson)
            }

            return json
        }
    }
}