package com.example.rutasranch.map

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rutasranch.location.LocationService
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.util.*

class MapViewModel : ViewModel() {

    private val client = OkHttpClient()
    private var mapView: MapView? = null

    private val _routePoints = MutableLiveData<List<GeoPoint>>()
    val routePoints: LiveData<List<GeoPoint>> = _routePoints

    private var currentDestination: GeoPoint = GeoPoint(20.083833, -101.442056)

    fun setMapView(map: MapView, context: Context) {
        this.mapView = map
        getUserLocationAndRoute(context)
    }

    fun refreshLocation(context: Context) {
        getUserLocationAndRoute(context)
    }

    fun searchAndRouteTo(context: Context, address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocationName(address, 1)
                if (!results.isNullOrEmpty()) {
                    val location = results[0]
                    currentDestination = GeoPoint(location.latitude, location.longitude)
                    getUserLocationAndRoute(context)
                } else {
                    Log.e("BUSQUEDA", "No se encontró la dirección: $address")
                }
            } catch (e: Exception) {
                Log.e("BUSQUEDA", "Error en geocoder: ${e.message}")
            }
        }
    }

    private fun getUserLocationAndRoute(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val locationService = LocationService(context)
            val location = locationService.getCurrentLocation()

            val startLocation = if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                GeoPoint(location.latitude, location.longitude)
            } else {
                GeoPoint(19.427025, -99.167665) // fallback
            }

            withContext(Dispatchers.Main) {
                mapView?.controller?.setCenter(startLocation)
            }

            fetchRoute(startLocation, currentDestination)
        }
    }

    private fun fetchRoute(start: GeoPoint, end: GeoPoint) {
        val url =
            "https://api.openrouteservice.org/v2/directions/driving-car?start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "5b3ce3597851110001cf6248336d736cc3b34981ada4207270a1eef3")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            try {
                val json = JSONObject(responseBody)

                if (!json.has("features")) {
                    Log.e("API", "Respuesta inválida (sin 'features'): $responseBody")
                    return@launch
                }

                val coordinates = json
                    .getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                Log.d("RUTA", "Total puntos de ruta: ${coordinates.length()}")

                val points = mutableListOf<GeoPoint>()
                for (i in 0 until coordinates.length()) {
                    val coord = coordinates.getJSONArray(i)
                    points.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
                }

                withContext(Dispatchers.Main) {
                    _routePoints.value = points
                    drawRoute()
                }

            } catch (e: Exception) {
                Log.e("API", "Error parseando la ruta: ${e.message}")
            }
        }
    }

    fun drawRoute() {
        val polyline = Polyline().apply {
            routePoints.value?.let { setPoints(it) }
            outlinePaint.strokeWidth = 8f
        }
        mapView?.overlays?.clear()
        mapView?.overlays?.add(polyline)
        mapView?.invalidate()
    }
}
