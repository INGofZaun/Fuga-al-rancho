package com.example.rutasranch

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.rutasranch.map.MapScreen

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadMap()
        } else {
            // Permiso denegado, puedes manejarlo aquí si quieres
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun loadMap() {
        setContent {
            MapScreen(context = this)
        }
    }
}
