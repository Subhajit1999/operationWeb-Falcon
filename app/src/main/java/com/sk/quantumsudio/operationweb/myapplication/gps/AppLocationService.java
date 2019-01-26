package com.sk.quantumsudio.operationweb.myapplication.gps;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.sk.quantumsudio.operationweb.myapplication.R;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class AppLocationService extends Service implements LocationListener{
    static public final int REQUEST_LOCATION_PERMISSION = 1;
    protected LocationManager locationManager;
    Location location;

    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
    private static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 2;

    public AppLocationService(Context context) {
        locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
    }

    public Location getLocation(String provider, Activity activity) {  //error
        if (locationManager.isProviderEnabled(provider)) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                EasyPermissions.requestPermissions(
                        new PermissionRequest.Builder(activity, REQUEST_LOCATION_PERMISSION, perms)
                                .setPositiveButtonText("Ok")
                                .setNegativeButtonText("Cancel")
                                .build());
                //return TODO;
            } else {
                locationManager.requestLocationUpdates(provider,
                        MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(provider);
                    //return location;
                }
            }
        }
        return location;
    }
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
