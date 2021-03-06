package com.example.desktop.itrackmap;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class GPSListener implements LocationListener {

    public ITrackMapActivity iTrackMapActivity;
    public Location initialLocation;
    public Location prevLocation;
    public TextView distanceTV;
    public TextView speedTV;
    public ImageView driveIV;
    public ImageView walkIV;

    public int accuracyDepth;
    public int progressPercent;
    public boolean setInitMarker;
    public boolean isBetterLocation;
    public static final int ONE_MINUTE = 1000 * 60;
    public static final int ACCURACY_ATTEMPTS = 6;
    public final int MAX_PERCENT = 100;

    public GPSListener(ITrackMapActivity act, Location initLoc) {

        iTrackMapActivity = act;
        initialLocation = initLoc;
        accuracyDepth = 0;

        distanceTV = (TextView) iTrackMapActivity.findViewById(R.id.textDist);
        speedTV = (TextView) iTrackMapActivity.findViewById(R.id.textSpeed);
        driveIV = (ImageView) iTrackMapActivity.findViewById(R.id.imageDrive);
        walkIV = (ImageView) iTrackMapActivity.findViewById(R.id.imageWalk);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (accuracyDepth < ACCURACY_ATTEMPTS) {
            initialLocation = location;
            prevLocation = location;
            accuracyDepth = accuracyDepth + 1;
            progressPercent = accuracyDepth * (MAX_PERCENT / ACCURACY_ATTEMPTS);

            iTrackMapActivity.updateProgress(progressPercent, false);

            setInitMarker = true;

        } else if (initialLocation != null && location != null) {

            isBetterLocation = isBetterLocation(prevLocation, location);

            if (isBetterLocation) {
                prevLocation = location;
                iTrackMapActivity.updateMap(initialLocation, location, setInitMarker);
                updateInfo(initialLocation, location);
            }

            if (setInitMarker) {
                setInitMarker = false;
                iTrackMapActivity.updateProgress(progressPercent, true);
            }
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public boolean isBetterLocation(Location currentBestLocation, Location location) {

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
        boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 100;

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate) {
            return true;
        }
        return false;
    }

    public void updateInfo(Location initialLocation, Location location) {

        int speedKMH;
        String text;
        float distance;

        speedKMH = (int) ((location.getSpeed() * 3600) / 1000);
        text = speedKMH + " Km/H";
        speedTV.setText(text);

        distance = (int) initialLocation.distanceTo(location);
        text = distance + " m";
        distanceTV.setText(text);

        if (speedKMH == 0) {
            walkIV.setBackgroundColor(Color.TRANSPARENT);
            driveIV.setBackgroundColor(Color.TRANSPARENT);
        } else if (speedKMH <= 5) {
            walkIV.setBackgroundColor(Color.rgb(92, 190, 236));
            driveIV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            walkIV.setBackgroundColor(Color.TRANSPARENT);
            driveIV.setBackgroundColor(Color.rgb(92, 190, 236));
        }

    }

}
