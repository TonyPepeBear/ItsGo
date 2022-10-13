package com.tonypepe.itsgo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationServices: FusedLocationProviderClient
    private val model: MainViewModel by viewModels()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationServices = LocationServices.getFusedLocationProviderClient(this)
        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                locationServices.lastLocation.addOnSuccessListener {
                    model.flyToLocation.postValue(Point.fromLngLat(it.longitude, it.latitude))
                }
            }
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_setting
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}