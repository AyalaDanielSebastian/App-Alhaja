package com.example.alhaja.data.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class Coordenadas(
    val latitud: Double,
    val longitud: Double
)

class ProveedorUbicacion(private val context: Context) {

    fun tienePermiso(): Boolean {
        val fino = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val aproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fino || aproximado
    }

    @SuppressLint("MissingPermission")
    suspend fun obtenerCoordenadas(): Coordenadas? {
        if (!tienePermiso()) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val proveedor = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return null
        }

        val reciente = manager.getLastKnownLocation(proveedor)
            ?: manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (reciente != null && System.currentTimeMillis() - reciente.time < 120_000) {
            return reciente.toCoordenadas()
        }

        return suspendCancellableCoroutine { continuacion ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(proveedor, null, context.mainExecutor) { ubicacion ->
                    if (continuacion.isActive) {
                        continuacion.resume(ubicacion?.toCoordenadas())
                    }
                }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(ubicacion: Location) {
                        manager.removeUpdates(this)
                        if (continuacion.isActive) {
                            continuacion.resume(ubicacion.toCoordenadas())
                        }
                    }
                }
                manager.requestLocationUpdates(proveedor, 0L, 0f, listener, Looper.getMainLooper())
                continuacion.invokeOnCancellation { manager.removeUpdates(listener) }
            }
        }
    }
}

private fun Location.toCoordenadas() = Coordenadas(latitude, longitude)
