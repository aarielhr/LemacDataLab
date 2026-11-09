package com.example.lemacdatalab.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (Intent.ACTION_BOOT_COMPLETED == action || Intent.ACTION_MY_PACKAGE_REPLACED == action) {
            // Re-programar la alarma tras reiniciar el teléfono si están habilitadas
            if (NotificationHelper.estanHabilitadas(context)) {
                val hora = NotificationHelper.getHora(context)
                val minuto = NotificationHelper.getMinuto(context)
                NotificationHelper.programarRecordatorioDiario(context, hora, minuto)
            }
        } else {
            // Enviar notificación genérica del recordatorio diario
            if (NotificationHelper.estanHabilitadas(context)) {
                NotificationHelper.enviarNotificacionGenerica(context)
            }
        }
    }
}
