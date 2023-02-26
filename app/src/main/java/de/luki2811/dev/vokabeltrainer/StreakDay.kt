package de.luki2811.dev.vokabeltrainer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Parcelize
data class StreakDay(var date: LocalDate, var xpGoal: Int, var xp: Int, var isFrozen: Boolean): Parcelable{

    fun toJSON(): JSONObject{
        return JSONObject().apply {
            put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            put("xpGoal", xpGoal)
            put("xp", xp)
            put("isFrozen", isFrozen)
        }
    }

    fun isDone(): Boolean = if(isFrozen) true else xp >= xpGoal

    companion object{
        fun loadFromJSON(json: JSONObject): StreakDay{
            return StreakDay(
                LocalDate.parse(json.getString("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                json.getInt("xpGoal"),
                json.getInt("xp"),
                json.getBoolean("isFrozen")
            )
        }
    }
}