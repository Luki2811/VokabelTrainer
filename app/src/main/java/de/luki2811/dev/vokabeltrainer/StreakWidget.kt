package de.luki2811.dev.vokabeltrainer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import de.luki2811.dev.vokabeltrainer.ui.MainActivity

/**
 * Implementation of App Widget functionality.
 */
class StreakWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, getUpdatedViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, context.packageName + StreakWidget::class.java))
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, getUpdatedViews(context))
        super.onReceive(context, intent)
    }

    companion object{
        fun getUpdatedViews(context: Context): RemoteViews{
            val streak = Streak(context)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context,0,Intent(context, MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            return RemoteViews(context.packageName, R.layout.streak_widget).apply {
                setTextViewText(R.id.widget_streak_text, "${streak.xpToday}/${streak.xpGoal}XP")
                setOnClickPendingIntent(R.id.widget_streak_layout, pendingIntent)
                if(streak.lengthInDay == 1)
                    setTextViewText(R.id.widgetStreakTextDown, context.getString(R.string.streak_in_day, 1))
                else
                    setTextViewText(R.id.widgetStreakTextDown, context.getString(R.string.streak_in_days, streak.lengthInDay))
                setInt(R.id.progressBarStreakWidget, "setMax", streak.xpGoal)
                setInt(R.id.progressBarStreakWidget, "setProgress", streak.xpToday)
            }
        }
    }
}