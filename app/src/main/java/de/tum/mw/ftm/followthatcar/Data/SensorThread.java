package de.tum.mw.ftm.followthatcar.Data;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

public class SensorThread extends Thread {
    private static final String TAG = SensorThread.class.getSimpleName();

    public static final String LOCATION_BROADCAST = "de.tum.mw.ftm.LOCATION_BROADCAST";
    public static final String LOCATION_EXTRA = "LOCATION_EXTRA";

    private Handler handler;
    private Context context;

    private int gpsSamplingPeriod;      //milliseconds
    private int gpsRange;               //meter
    private boolean isGpsEnabled;
    private Location lastLocation;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    public SensorThread(Context context) {
        this.context = context;
        //Read settings from shared preferences
        isGpsEnabled = true;
        gpsSamplingPeriod = 10 * 1000;
        gpsRange = 1;
    }

    /**
     * The run method is automatically called if a thread is started
     */
    @Override
    public void run() {
        //Prepare the looper
        Looper.prepare();
        //Create the handler
        handler = new Handler();

        //Check if GPS is enabled
        if(isGpsEnabled){
            //Initialize the location provider
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            //Create a new location request
            final LocationRequest locationRequest = LocationRequest.create();
            //Set the sampling interval
            locationRequest.setInterval(gpsSamplingPeriod);
            locationRequest.setFastestInterval(gpsSamplingPeriod);
            //Set the minimum distance between updates
            locationRequest.setSmallestDisplacement(gpsRange);
            //Set priority
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            //Create a new location callback
            locationCallback = new LocationCallback(){
                /**
                 * This method is automatically called if a new location result is available
                 * @param locationResult The current location
                 */
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //The result could be null in rare cases
                    if (locationResult == null) {
                        //Simply return if there is no result
                        return;
                    }
                    //Get the last location
                    Location location = locationResult.getLastLocation();
                    //Set the current time for the location. There might be errors with the inbuilt date and time
                    location.setTime(new Date().getTime());
                    //TODO we can upload position here
                    //Update the tracks distance in case we have a previous location result
                    if(lastLocation != null){

                    }
                    //Update the last result
                    lastLocation = location;

                    //Prepare a new intent for the broadcast
                    Intent intent = new Intent();
                    //Set the action and fill the intent
                    intent.setAction(LOCATION_BROADCAST);
                    intent.putExtra(LOCATION_EXTRA, location);
                    //Send the broadcast
                    context.sendBroadcast(intent);
                }
            };

            //Check build version. If M or higher we have to check permission during runtime.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission to access the location is granted --> request location updates
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            }
            else {
                //If lower than M we can directly ask for location updates
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }

        //Execute the looper
        Looper.loop();
    }

    /**
     * Stop the thread
     */
    public void stopSensorThread(){
        //Quit the looper
        handler.getLooper().quit();

        //Check if GPS was enabled
        if (isGpsEnabled){
            //Again check the build version
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Remove location updates
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                }
            }
            else {
                //Remove location updates
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        }

        //Stop the thread
        this.interrupt();
    }


}
