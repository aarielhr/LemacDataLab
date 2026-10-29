package com.example.lemacdatalab.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object LocationHelper {

    fun tienePermisosUbicacion(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual(
        context: Context,
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!tienePermisosUbicacion(context)) {
            onError("No se otorgaron los permisos de ubicación GPS.")
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onSuccess(location.latitude, location.longitude)
                    } else {
                        // Request single fresh location fix
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { freshLocation: Location? ->
                                if (freshLocation != null) {
                                    onSuccess(freshLocation.latitude, freshLocation.longitude)
                                } else {
                                    onError("No se pudo obtener la señal GPS actual. Asegúrate de tener la ubicación activada en tu dispositivo.")
                                }
                            }
                            .addOnFailureListener { e ->
                                onError("Error al obtener señal GPS: ${e.localizedMessage ?: "desconocido"}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    onError("Error de GPS: ${e.localizedMessage ?: "desconocido"}")
                }
        } catch (e: Exception) {
            onError("Excepción al acceder al GPS: ${e.localizedMessage ?: "desconocido"}")
        }
    }
}
