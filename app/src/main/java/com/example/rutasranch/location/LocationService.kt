package com.example.rutasranch.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                    cont.resume(location)
                } else {
                    cont.resume(null)
                }
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }

    fun getAddressFromLocation(location: Location): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> =
                geocoder.getFromLocation(location.latitude, location.longitude, 1) ?: return "Dirección no disponible"
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                "${address.thoroughfare ?: ""} ${address.featureName ?: ""}, ${address.locality ?: ""}, ${address.adminArea ?: ""}, ${address.countryName ?: ""}"
            } else {
                "Dirección no disponible"
            }
        } catch (e: Exception) {
            "Error obteniendo dirección"
        }
    }
}
