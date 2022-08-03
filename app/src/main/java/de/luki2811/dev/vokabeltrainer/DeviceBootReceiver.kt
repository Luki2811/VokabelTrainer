package de.luki2811.dev.vokabeltrainer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*


class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settings = Settings(context)
        if (Objects.equals(intent.action, "android.intent.action.BOOT_COMPLETED")) {
            // on device boot complete, reset the alarm
            val alarmIntent = Intent(context, DeviceBootReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, settings.timeReminderStreak.hour)
            calendar.set(Calendar.MINUTE, settings.timeReminderStreak.minute)
            calendar.set(Calendar.SECOND, 0)
            val newC: Calendar = GregorianCalendar()
            newC.timeInMillis = Calendar.getInstance().timeInMillis

            if (calendar.after(newC)) {
                calendar.add(Calendar.HOUR, 1)
            }
            manager.setRepeating(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
        }
    }
}