package mchehab.com.googlemapsinteractions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.v4.content.PermissionChecker
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.provider.Settings



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMyLocationButtonClickListener, LocationResultListener {

    private val ACTIVITY_REQUEST_CODE = 1000
    private val PERMISSION_REQUEST_CODE = 1000

    private val COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION

    private lateinit var googleMap: GoogleMap
    private lateinit var locationHandler: LocationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationHandler = LocationHandler(this, this, ACTIVITY_REQUEST_CODE, PERMISSION_REQUEST_CODE)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val map = supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        map.getMapAsync(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK)
                locationHandler.getUserLocation()
            else{
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Error")
                    .setMessage("Please enable location")
                    .setPositiveButton("Enable") { dialog, which ->
                        locationHandler.getUserLocation()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss()}
                    .create()
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE){
            var granted = true
            for (i in grantResults){
                if (grantResults[i] == PermissionChecker.PERMISSION_DENIED){
                    granted = false
                    break
                }
            }
            if (granted)
                locationHandler.getUserLocation()
            else{
                if (shouldShowRequestPermissionRationale(COARSE_LOCATION) || shouldShowRequestPermissionRationale(FINE_LOCATION)){
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Error")
                        .setMessage("You need to grant permissions in order to display location")
                        .setPositiveButton("Grant") {dialog, which ->
                            locationHandler.getUserLocation()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") {dialog, which -> dialog.dismiss() }
                        .create()
                        .show()
                }else{
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Error")
                        .setMessage("You need to grant permission to in order to display location")
                        .setPositiveButton("Ok") {dialog, which ->
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel") {dialog, which -> dialog.dismiss() }
                        .create()
                        .show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.setOnMapClickListener(this)
        this.googleMap.setOnMapLongClickListener(this)
        this.googleMap.setOnMarkerClickListener(this)
        this.googleMap.setOnMyLocationButtonClickListener(this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.terrain -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
            R.id.satellite -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.hybrid -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.none -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_NONE
            }
            R.id.showUserLocation -> {
                locationHandler.getUserLocation()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onMapClick(latLng: LatLng?) {
        latLng?.let {
            addMarker(it)
        }
    }

    override fun onMapLongClick(latLng: LatLng?) {
        latLng?.let {
            addMarker(it)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position), 500, object: GoogleMap.CancelableCallback {
                override fun onFinish() {
                    val buttonText =
                        if (marker.isDraggable) "Disable Drag"
                        else "Enable Drag"
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Drag Marker?")
                        .setPositiveButton(buttonText) { dialog, _ ->
                            marker.isDraggable = !marker.isDraggable
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss()}
                        .setCancelable(false)
                        .create()
                        .show()
                }
                override fun onCancel() {}
            })
            return true
        }
        return false
    }

    override fun onMyLocationButtonClick(): Boolean {
        return true
    }

    @SuppressLint("MissingPermission")
    override fun getLocation(location: Location) {
        googleMap.isMyLocationEnabled = true
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
    }

    private fun addMarker(latLng: LatLng){
        val marker = MarkerOptions().position(latLng)
        marker.title("Some Title")
        marker.draggable(false)
        googleMap.addMarker(marker)
    }
}