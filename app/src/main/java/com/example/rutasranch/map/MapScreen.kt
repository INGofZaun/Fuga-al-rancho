package com.example.rutasranch.map

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView

@Composable
fun MapScreen(context: Context, mapViewModel: MapViewModel = viewModel()) {
    val routePoints by mapViewModel.routePoints.observeAsState()
    var currentAddress by remember { mutableStateOf("Ubicación no disponible") }
    var destinationInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        // Mapa
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(factory = {
                Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                val map = MapView(context)
                map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                map.setBuiltInZoomControls(true)
                map.setMultiTouchControls(true)
                map.controller.setZoom(15.0)
                mapViewModel.setMapView(map, context)
                map.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                map
            }, update = {
                mapViewModel.drawRoute()
            })
        }

        // Buscar dirección
        OutlinedTextField(
            value = destinationInput,
            onValueChange = { destinationInput = it },
            label = { Text("Buscar dirección") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Button(
            onClick = {
                mapViewModel.searchAndRouteTo(context, destinationInput)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text("Buscar dirección")
        }

        Button(
            onClick = {
                mapViewModel.refreshLocation(context)
                CoroutineScope(Dispatchers.IO).launch {
                    val service = com.example.rutasranch.location.LocationService(context)
                    val loc = service.getCurrentLocation()
                    if (loc != null) {
                        val addr = service.getAddressFromLocation(loc)
                        withContext(Dispatchers.Main) {
                            currentAddress = addr
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text("Actualizar ubicación")
        }

        Text(
            text = "Dirección actual: $currentAddress",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
