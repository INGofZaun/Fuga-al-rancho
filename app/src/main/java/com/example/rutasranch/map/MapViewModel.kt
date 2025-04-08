package com.example.rutasranch.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

class MapViewModel : ViewModel() {

    private val client = OkHttpClient()
    private var mapView: MapView? = null

    private val _routePoints = MutableLiveData<List<GeoPoint>>()
    val routePoints: LiveData<List<GeoPoint>> = _routePoints

    private val homeLocation = GeoPoint(19.432608, -99.133209) // CDMX como ejemplo

    fun setMapView(map: MapView) {
        this.mapView = map
        getUserLocationAndRoute()
    }

    private fun getUserLocationAndRoute() {
        // Ejemplo: ubicación actual hardcodeada
        val currentLocation = GeoPoint(19.427025, -99.167665) // Cercano a la casa
        fetchRoute(currentLocation, homeLocation)
    }

    private fun fetchRoute(start: GeoPoint, end: GeoPoint) {
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=TU_API_KEY&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"
        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
            val coordinates = json.getJSONArray("features")
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val points = mutableListOf<GeoPoint>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                points.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
            }

            withContext(Dispatchers.Main) {
                _routePoints.value = points
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
