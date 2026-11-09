package com.example.lemacdatalab.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lemacdatalab.MainActivity
import com.example.lemacdatalab.R
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_ID = "lemac_recordatorio_channel"
    private const val PREFS_NAME = "lemac_notif_prefs"
    private const val KEY_HABILITADAS = "key_notif_habilitadas"
    private const val KEY_HORA = "key_notif_hora"
    private const val KEY_MINUTO = "key_notif_minuto"
    private const val ALARM_REQUEST_CODE = 1001
    private const val NOTIFICATION_ID = 2001

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios Diarios"
            val descriptionText = "Recordatorio genérico para registrar tu autorreporte del día"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun guardarConfiguracion(context: Context, habilitadas: Boolean, hora: Int, minuto: Int) {
        prefs(context).edit()
            .putBoolean(KEY_HABILITADAS, habilitadas)
            .putInt(KEY_HORA, hora)
            .putInt(KEY_MINUTO, minuto)
            .apply()

        if (habilitadas) {
            programarRecordatorioDiario(context, hora, minuto)
        } else {
            cancelarRecordatorioDiario(context)
        }
    }

    fun estanHabilitadas(context: Context): Boolean = prefs(context).getBoolean(KEY_HABILITADAS, false)
    fun getHora(context: Context): Int = prefs(context).getInt(KEY_HORA, 20)
    fun getMinuto(context: Context): Int = prefs(context).getInt(KEY_MINUTO, 0)

    fun programarRecordatorioDiario(context: Context, hora: Int, minuto: Int) {
        crearCanalNotificaciones(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Si la hora ya pasó hoy, se programa para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelarRecordatorioDiario(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun enviarNotificacionGenerica(context: Context) {
        crearCanalNotificaciones(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Texto totalmente genérico sin datos sensibles ni síntomas (cumpliendo norma)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Lemac DataLab")
            .setContentText("Tienes un recordatorio pendiente")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
