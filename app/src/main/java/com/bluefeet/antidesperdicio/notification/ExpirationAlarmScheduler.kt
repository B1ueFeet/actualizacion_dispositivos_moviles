package com.bluefeet.antidesperdicio.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bluefeet.antidesperdicio.data.local.Food

class ExpirationAlarmScheduler(
    private val context: Context
) {

    fun schedule(food: Food) {
        val alarmTime = food.expirationDate - ONE_DAY_IN_MILLIS

        if (alarmTime <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, ExpirationAlarmReceiver::class.java).apply {
            putExtra(ExpirationAlarmReceiver.EXTRA_FOOD_NAME, food.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            food.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
            return
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    }

    fun scheduleDebug(foodName: String) {
        val alarmTime = System.currentTimeMillis() + 5_000L

        val intent = Intent(context, ExpirationAlarmReceiver::class.java).apply {
            putExtra(ExpirationAlarmReceiver.EXTRA_FOOD_NAME, foodName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEBUG_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )
    }

    fun cancel(food: Food) {
        val intent = Intent(context, ExpirationAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            food.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val DEBUG_REQUEST_CODE = 999_999
    }
}