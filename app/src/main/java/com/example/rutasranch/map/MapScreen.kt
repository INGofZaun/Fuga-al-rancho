package com.example.rutasranch.map

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration
import android.content.Context
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MapScreen(context: Context, mapViewModel: MapViewModel = viewModel()) {
    val routePoints by mapViewModel.routePoints.observeAsState()

    AndroidView(factory = {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        val map = MapView(context)
        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        mapViewModel.setMapView(map)
        map.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        map
    }, update = {
        mapViewModel.drawRoute()
    })
}
