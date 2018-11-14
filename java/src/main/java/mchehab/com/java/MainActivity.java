package mchehab.com.java;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements LocationResultListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private final int ACTIVITY_REQEUST_CODE = 1000;
    private final int PERMISSION_REQEUST_CODE = 1000;

    private LocationHandler locationHandler;

    private DrawerLayout drawerLayout;

    private SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;

    private final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int GRANTED = PackageManager.PERMISSION_GRANTED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupActionBar();
        setupMap();
        setupNavigationView();
        locationHandler = new LocationHandler(this, this, ACTIVITY_REQEUST_CODE, PERMISSION_REQEUST_CODE);
    }

    private void setupActionBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupMap(){
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        supportMapFragment.getMapAsync(this);
    }

    private void setupNavigationView(){
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions marker = new MarkerOptions()
                .title("Some Title")
                .draggable(false)
                .position(latLng)
                .alpha(0.5f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        googleMap.addMarker(marker);
    }

    private void enableLocationOnMap(){
        if (ActivityCompat.checkSelfPermission(this, FINE_LOCATION) != GRANTED
                && ActivityCompat.checkSelfPermission(this, COARSE_LOCATION) != GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQEUST_CODE) {
            if (resultCode == RESULT_OK) {
                locationHandler.getUserLocation();
                enableLocationOnMap();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Please enable location")
                        .setPositiveButton("Enable", (dialog, which) -> {
                            locationHandler.getUserLocation();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                        .setCancelable(false)
                        .create()
                        .show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQEUST_CODE) {
            boolean granted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PermissionChecker.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (!granted) {
                if (shouldShowRequestPermissionRationale(COARSE_LOCATION) || shouldShowRequestPermissionRationale(FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("You need to grant permissions in order to display location")
                            .setPositiveButton("Enable", (dialog, which) -> {

                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .create()
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("You need to manually enable location in the settings")
                            .setPositiveButton("Enable", (dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .create()
                            .show();
                }
            }
            locationHandler.getUserLocation();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setPositiveButton("Enable", ((dialog, which) -> {
                        locationHandler.getUserLocation();
                        dialog.dismiss();
                    }))
                    .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                    .setCancelable(false)
                    .create()
                    .show();
        }
    }

    @Override
    public void getLocation(Location location) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMapLongClickListener(this);
        this.googleMap.setOnMarkerClickListener(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.terrain:
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.satellite:
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.hybrid:
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.none:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.showUserLocation:
                enableLocationOnMap();
                locationHandler.getUserLocation();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        addMarker(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        addMarker(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == null)
            return false;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 500), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                String buttonText = (marker.isDraggable()) ? "Disable Drag" : "Enable Drag";
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Drag Marker?")
                        .setPositiveButton(buttonText, (dialog, which) -> {
                            marker.setDraggable(!marker.isDraggable());
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .create()
                        .show();
            }
            @Override
            public void onCancel() { }
        });
        return true;
    }
}