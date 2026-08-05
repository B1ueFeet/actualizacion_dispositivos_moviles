package com.bluefeet.antidesperdicio.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bluefeet.antidesperdicio.R

class ExpirationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val foodName = intent.getStringExtra(EXTRA_FOOD_NAME) ?: "Un producto"

        createNotificationChannel(context)
        showNotification(context, foodName)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertas de caducidad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para productos proximos a vencer"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, foodName: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.freshguard_icon
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_freshguard_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("Producto por vencer")
            .setContentText("FreshGuard: $foodName vence manana")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    companion object {
        const val CHANNEL_ID = "expiration_alerts"
        const val EXTRA_FOOD_NAME = "extra_food_name"
    }
}
