package de.luki2811.dev.vokabeltrainer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import java.util.*


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        val CHANNEL_ID = "streak"

        // Setup channel

        val name = context.getString(R.string.channel_name_streak)
        val descriptionText = context.getString(R.string.channel_description_streak)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val sendIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, sendIntent, PendingIntent.FLAG_IMMUTABLE)


        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "streak")
        builder.setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_baseline_streak_24)
            .setContentTitle(context.getString(R.string.streak))
            .setContentText("Hast du heute schon dein Tagesziel erreicht ??")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)


        val notificationId = 100

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }

        val nextNotifyTime: Calendar = Calendar.getInstance()
        nextNotifyTime.add(Calendar.DATE, 1)
    }


}