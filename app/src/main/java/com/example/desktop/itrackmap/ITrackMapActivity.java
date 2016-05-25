package com.example.desktop.itrackmap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ITrackMapActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public LocationManager manager;
    public GPSListener listener;
    public Location initialLocation;
    public Location mostUpdateLocation;
    public LatLng latLng;
    public String coordinates;

    private static final float ZOOM_LEVEL = 16;
    private static final int REQUEST_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itrack_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initialLocation = getInitLocation();

        listener = new GPSListener(ITrackMapActivity.this, initialLocation, this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (initialLocation != null) {
            latLng = new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Israel"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) break;
                else break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.removeUpdates(listener);
        }
    }

    public Location getInitLocation() {

        Location location = null;

        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } else {
            Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(locIntent, REQUEST_LOCATION);
        }
        return location;
    }

    public void updateMap(Location initialLoc, Location mostUpdatedLoc) {
        initialLocation = initialLoc;
        mostUpdateLocation = mostUpdatedLoc;

        if (mostUpdateLocation != null) {
            latLng = new LatLng(mostUpdateLocation.getLatitude(), mostUpdateLocation.getLongitude());
            mMap.clear();
            coordinates = "(" + mostUpdateLocation.getLatitude() + ";" + mostUpdateLocation.getLongitude() + ")";
            mMap.addMarker(new MarkerOptions().position(latLng).title(coordinates));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        }
    }

}
