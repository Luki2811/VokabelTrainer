package de.luki2811.dev.vokabeltrainer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.luki2811.dev.vokabeltrainer.ui.MainActivity

class NotificationWorker(val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {
    override fun doWork(): Result {
        val settings = Settings(context)
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(settings.reminderForStreak && notificationManager.areNotificationsEnabled()){
            val CHANNEL_ID = "streak"

            // Setup channel

            val name = context.getString(R.string.channel_name_streak)
            val descriptionText = context.getString(R.string.channel_description_streak)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val sendIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)

            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, sendIntent, PendingIntent.FLAG_IMMUTABLE)

            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "streak").apply {
                setAutoCancel(true)
                setSmallIcon(R.drawable.ic_baseline_streak_24)
                setContentTitle(context.getString(R.string.streak))
                setContentText(context.getString(R.string.notification_have_you_reached_your_streak))
                priority = NotificationCompat.PRIORITY_DEFAULT
                setContentIntent(pendingIntent)
            }

            val notificationId = 100

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }

            return Result.success()
        }else{
            return Result.failure()
        }

    }

}