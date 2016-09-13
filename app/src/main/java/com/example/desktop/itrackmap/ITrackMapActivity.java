package com.example.desktop.itrackmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ITrackMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float ZOOM_LEVEL = 17;
    private static final int REQUEST_LOCATION = 0;
    public final int MAX_PERCENT = 100;
    public int progressPercent;

    public GoogleMap mMap;
    public LocationManager manager;
    public GPSListener listener;
    public Location initialLocation;
    public Location mostUpdateLocation;
    public LatLng latLng;
    public ProgressDialog progress;
    public MarkerOptions initMarker;
    public MarkerOptions currMarker;
    public Handler progressBarHandler;

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

        progressBarHandler = new Handler();

        progress = new ProgressDialog(ITrackMapActivity.this);
        progress.setMessage("Setting GPS Location ...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setCancelable(true);
        progress.setMax(MAX_PERCENT);
        progress.setProgress(0);
        progress.show();

        listener = new GPSListener(ITrackMapActivity.this, initialLocation);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (initialLocation != null) {
            latLng = new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude());
            initMarker = new MarkerOptions().position(latLng).title("Israel");
            mMap.addMarker(initMarker);
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
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
            manager.removeUpdates(listener);
        }
    }

    public Location getInitLocation() {

        Location location = null;

        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION);
                }
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } else {
            Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(locIntent, REQUEST_LOCATION);
        }
        return location;
    }

    public void updateMap(Location initialLoc, Location mostUpdatedLoc, boolean setInitMarker) {

        initialLocation = initialLoc;
        mostUpdateLocation = mostUpdatedLoc;

        if (setInitMarker && mostUpdateLocation != null && initialLocation != null) {

            mMap.clear();

            latLng = new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude());
            initMarker = new MarkerOptions().position(latLng).title("Start Location");
            initMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mMap.addMarker(initMarker);

            latLng = new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude());
            currMarker = new MarkerOptions().position(latLng).title("Current Location");
            currMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(currMarker);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));

        } else if (!setInitMarker && mostUpdateLocation != null && initialLocation != null) {

            mMap.clear();

            latLng = new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude());
            initMarker.position(latLng).title("Start Location");
            initMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mMap.addMarker(initMarker);

            latLng = new LatLng(mostUpdateLocation.getLatitude(), mostUpdateLocation.getLongitude());
            currMarker.position(latLng).title("Current Location");
            currMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(currMarker);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        }
    }

    public void updateProgress(Integer percent, Boolean dismiss) {

        progressPercent = percent;

        if (dismiss) {
            progress.dismiss();
        } else {
            progressBarHandler.post(new Runnable() {
                public void run() {
                    progress.setProgress(progressPercent);
                }
            });
        }
    }
}
