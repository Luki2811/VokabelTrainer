package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate

class Streak(private val context: Context, val days: ArrayList<StreakDay> = arrayListOf()) {

    init {
        val json = try {
             JSONArray(FileUtil.loadFromFile(File(context.filesDir, FileUtil.NAME_FILE_STREAK)))
        }catch (e: FileNotFoundException){
            JSONArray().apply {
                put(StreakDay(LocalDate.now(), Settings(context).dailyObjectiveStreak, 0, false).toJSON())
            }
        }

        for (i in 0 until json.length()){
            days.add(StreakDay.loadFromJSON(json.getJSONObject(i)))
            days.sortBy { it.date }
        }
        fillEmptyDays()
        saveInFile()
    }

    fun getCurrentLengthInDays(): Int{
        var currentDay = getCurrentStreakDay()
        var length = 0
        if(currentDay.isDone()){
            while (currentDay.isDone()){
                length += 1
                currentDay = days.find { currentDay.date.minusDays(1) == it.date }?: return length
            }
        }else{
            currentDay = days.find { it.date == currentDay.date.minusDays(1) }?: return 0
            while (currentDay.isDone()){
                length += 1
                currentDay = days.find { currentDay.date.minusDays(1) == it.date }?: return length
            }
        }
        return length
    }

    private fun fillEmptyDays(){
        val currentStreakGoal = Settings(context).dailyObjectiveStreak
        var posDate = LocalDate.now()
        while (posDate.isBefore(days.maxBy { it.date }.date)){
            if(days.none { it.date == posDate }){
                days.add(StreakDay(posDate, currentStreakGoal, 0, false))
                days.sortBy { it.date }
            }

            posDate = posDate.minusDays(1)
        }
    }

    fun getCurrentStreakDay(): StreakDay = days.find { it.date.isEqual(LocalDate.now()) }?: StreakDay(LocalDate.now(), Settings(context).dailyObjectiveStreak, 0, false)

    fun updateStreakDay(streakDay: StreakDay){
        val day = days.find { it.date == streakDay.date }
        if(day != null){
            days.removeIf { it.date == streakDay.date }
            days.add(streakDay)
        }else{
            days.add(streakDay)
        }
        saveInFile()
    }

    fun saveInFile(){
        val json = JSONArray()
        days.forEach { json.put(it.toJSON()) }
        FileUtil.writeInFile(
            json.toString(),
            File(context.filesDir, FileUtil.NAME_FILE_STREAK)
        )
        Log.i(LOG_TAG, "Successfully saved Streak in File")
    }

    companion object{

        const val LOG_TAG = "Streak"

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

        fun getRandomStreak(durationInDays: Int, allDaysDone: Boolean, goal: Int): JSONArray{
            var posDate = LocalDate.now()
            val json = JSONArray()
            for(i in 0 until durationInDays){
                if(allDaysDone){
                    json.put(StreakDay(posDate, goal, (goal..goal+100).random(), false ).toJSON())
                }else{
                    json.put(StreakDay(posDate, goal, (0..200).random(), false).toJSON())
                }

                posDate = posDate.minusDays(1)
            }
            return json
        }
    }
}