package de.tum.mw.ftm.followthatcar;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import de.tum.mw.ftm.followthatcar.Data.SensorThread;
import de.tum.mw.ftm.followthatcar.util.MySingleton;

public class MainActivity extends Activity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY = "AIzaSyAEzZuoJ4EooqZqnARsVsAeVbVZjixzPJQ";

    private FloatingActionButton fab;
    private FloatingActionButton userFab;
    private FrameLayout container;
    private boolean isServiceRunning = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private GoogleMap map;

    private enum FragmentNow{IDLE, FOLLOW_ME, FOLLOW_OTHER};
    private FragmentNow fragmentNow;

    // manual generate position of Garching Forschungszenturm
//    private String lat = "48.262514";
//    private String lng = "11.667160";
    private double leaderLat = 0.0;
    private double leaderLng = 0.0;
    private double lat = 48.262514;
    private double lng = 11.667160;
    private LocationCallback locationCallback;

    private SensorThread sensorThread;
    private BroadcastReceiver locationReceiver;
    private long lastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getFragmentManager().beginTransaction().add(R.id.container, new DecisionFragment()).commit();
        fragmentNow = FragmentNow.IDLE;
        container = findViewById(R.id.container);

        fab = findViewById(R.id.fab);
        //TODO: function of floating action button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceRunning) {
                    // check availability
                    if (getFragmentManager().findFragmentById(R.id.container) instanceof InputFragment) {
                        //TODO check valid input first
                        Log.d(TAG, "onClick: is InputFragment");
                        loginId();
                        //TODO if error, retry
//                        List<LatLng> points = new ArrayList<>();
//                        points.add(new LatLng(48.264671, 11.671391));
//                        points.add(new LatLng(48.398625, 11.723476));
//                        getGoogleMapPoly(points);
                        //TODO start recording user's position and upload to server
                    } else if (getFragmentManager().findFragmentById(R.id.container) instanceof ShowFragment) {
                        Log.d(TAG, "onClick: is ShowFragment");
                        registerId();
                        //TODO if error, regenerate id
                        //TODO wait for response
                        //uploadPos();

                    }
                } else {
                    // Set view away
                    stopThreads();
                    //getContainerBack();
                }

            }
        });

        userFab = findViewById(R.id.user);
        userFab.setAlpha(0.75f);

        //enable cookie
        CookieHandler.setDefault(new CookieManager());

        lastUpdateTime = new Date().getTime();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        boolean show = getFragmentManager().findFragmentById(R.id.container) instanceof ShowFragment;
        boolean input = getFragmentManager().findFragmentById(R.id.container) instanceof InputFragment;
        int visible = container.getVisibility();
        if ((show || input) && (visible == 0)) {
            getFragmentManager().beginTransaction().replace(R.id.container, new DecisionFragment()).commit();
            // stop floating action button when "BACK" is pressed
            fab.setVisibility(View.INVISIBLE);
        } else {
            stopThreads();
            //getContainerBack();
        }
    }

    private void moveContainerAway() {
        setFabIcon(true);
        container.setVisibility(View.GONE);
        isServiceRunning = true;
    }

    private void getContainerBack() {
        setFabIcon(false);
        container.setVisibility(View.VISIBLE);
        isServiceRunning = false;
    }

    /**
     * Sets the correct icon for the floating action button
     *
     * @param isServiceRunning if the Service is running
     */
    private void setFabIcon(boolean isServiceRunning) {
        if (isServiceRunning) {
            //If service is running set the stop icon
            fab.setImageResource(R.drawable.ic_stop);
        } else {
            //If service is not running set the start icon
            fab.setImageResource(R.drawable.ic_start);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        Log.d(TAG, "onMapReady: Map is ready");
        enableMyLocation();
    }

    private void enableMyLocation() {
        //Check build version. If M or higher we have to check permission during runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing. -> request it
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else if (map != null) {
                // Access to the location has been granted to the app. -> enable my location
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.setPadding(0, 32, 0, 0);
            }
        } else if (map != null) {
            // Access to the location has been granted to the app. -> enable my location
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            locationCallback = new LocationCallback(){
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

                    lat=location.getLatitude();
                    lng=location.getLongitude();
                    Log.d(TAG, "onLocationResult: " + lat + " " + lng);
                }
            };
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Check if it is the right request code
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            //Check if it is the permission we have asked for and if it has been granted
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //If permission was granted enable location
                enableMyLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check if the receiver is already initialized
        if(locationReceiver == null){
            //Initialize a new receiver
            locationReceiver = new BroadcastReceiver() {
                /**
                 * Is automatically called if a new broadcast is receiverd
                 * @param context The context
                 * @param intent The intent with the content
                 */
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Check if we received the correct broadcast
                    if(intent.getAction().equals(SensorThread.LOCATION_BROADCAST)){
                        Log.d(TAG, "Broadcast received");
                        //Get the location information from the intent
                        Location location = intent.getParcelableExtra(SensorThread.LOCATION_EXTRA);
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        if(getFragmentManager().findFragmentById(R.id.container) instanceof ShowFragment)
                            //upload position
                            uploadPos(location);
                        else if(getFragmentManager().findFragmentById(R.id.container) instanceof InputFragment){
                            if(location.getTime() - lastUpdateTime > 1000 * 10){
                                downloadPos();
                                lastUpdateTime = location.getTime();
                            }
                        }
                        //Adjust the zoom and position of the map
                        int zoom = (17 - Math.round(location.getSpeed() / 8f));
                        if(map != null){
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
                        }
                    }
                }
            };
        }
        //Create a new filter such that we only receive the desired broadcasts
        IntentFilter intentFilter = new IntentFilter(SensorThread.LOCATION_BROADCAST);
        //Register the broadcast
        registerReceiver(locationReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(locationReceiver != null){
            //Unregister receiver
            unregisterReceiver(locationReceiver);
            locationReceiver = null;
        }
    }

    public void registerId() {
        String url = "https://followmeapp.azurewebsites.net/register.php";

        TextView txId = findViewById(R.id.show_id);
        TextView txPin = findViewById(R.id.show_pin);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", txId.getText());
            jsonObject.put("password", txPin.getText());
        } catch (JSONException e) {
            Log.d(TAG, "registerId: Exception: " + e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        // process response
                        if (response.has("0")) {
                            try {
                                JSONObject jsonObject1 = response.getJSONObject("0");
                                if (jsonObject1.getString("error").equals("false")) {
                                    //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    // Set view away
                                    startLeaderThread();
                                    moveContainerAway();
                                } else {
                                    Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onResponse: " + jsonObject1.getString("errorMsg"));
                                }
                            } catch (JSONException e) {
                                Log.d(TAG, "onResponse: error response: " + e.toString());
                            }
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onResponse: error detected" + error.toString());
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void startLeaderThread(){
        sensorThread = new SensorThread(getApplicationContext());
        sensorThread.start();
    }

    public void loginId() {
        String url = "https://followmeapp.azurewebsites.net/login.php";

        EditText etId = findViewById(R.id.input_id);
        EditText etPin = findViewById(R.id.input_pin);
        Log.d(TAG, "registerId: Exception: " + etId.getText() + " " + etPin.getText());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", etId.getText());
            jsonObject.put("password", etPin.getText());
        } catch (JSONException e) {
            Log.d(TAG, "registerId: Exception: " + e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        // process response
                        try {
                            JSONObject jsonObject1 = response.getJSONObject("0");
                            if (jsonObject1.getString("error").equals("false")) {
                                //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                // Set view away
                                startLeaderThread();
                                moveContainerAway();
                            } else {
                                Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onResponse: " + jsonObject1.getString("errorMsg"));
                                //TODO regenerate id
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "onResponse: error response: " + e.toString());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onResponse: error detected" + error.toString());
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //TODO upload position in thread
    public void uploadPos(Location location) {
        String url = "https://followmeapp.azurewebsites.net/upload.php";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", "800000017");
            jsonObject.put("lat", location.getLatitude());
            jsonObject.put("lng", location.getLongitude());
            jsonObject.put("time", new Timestamp(new Date().getTime()).toString());
        } catch (JSONException e) {
            Log.d(TAG, "registerId: Exception: " + e.toString());
        }

        // get and save cookie
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        // process response
                        try {
                            JSONObject jsonObject1 = response.getJSONObject("0");
                            if (jsonObject1.getString("error").equals("false")) {

                            } else {
                                Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onResponse: " + jsonObject1.getString("errorMsg"));
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "onResponse: error response: " + e.toString());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onResponse: error detected" + error.toString());
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void downloadPos() {
        String url = "https://followmeapp.azurewebsites.net/download.php";

        // prepare json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        // process response
                        if (response.has("0")) {
                            try {
                                JSONObject jsonObject1 = response.getJSONObject("0");
                                if (jsonObject1.getString("error").equals("false")) {
                                    //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    leaderLat = jsonObject1.getDouble("lat");
                                    leaderLng = jsonObject1.getDouble("lng");
                                    // ask for route
                                    List<LatLng> points = new ArrayList<>();
                                    points.add(new LatLng(lat, lng));
                                    points.add(new LatLng(leaderLat, leaderLng));
                                    getGoogleMapPoly(points);
                                }else{
                                    Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onResponse: " + jsonObject1.getString("errorMsg"));
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onResponse: error response: " + e.toString());
                            }
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onErrorResponse: error detected" + error.toString());
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void getGoogleMapPoly(List<LatLng> startEnd) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + startEnd.get(0).latitude
                + ","
                + startEnd.get(0).longitude
                + "&destination=" +
                startEnd.get(1).latitude
                + ","
                + startEnd.get(1).longitude
                + "&mode="
                + "driving"
                + "&key="
                + API_KEY;

        // new json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        // process response
                        if (response.has("status")) {
                            try {
                                if (response.getString("status").equals("OK")) {
                                    JSONObject jsonObject1 = response.getJSONArray("routes").getJSONObject(0);
                                    JSONObject polylines = jsonObject1.getJSONObject("overview_polyline");
                                    if (polylines.has("points")) {
                                        //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                        List<LatLng> points = PolyUtil.decode(polylines.getString("points"));
                                        startInput(points);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "error while getting route", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onResponse: error while getting route");
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "can't connect to google map", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.d(TAG, "onResponse: error response: " + e.toString());
                            }
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onErrorResponse: error detected" + error.toString());
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void startInput(List<LatLng> points) {
        if (map != null) {
            //Clear the map from marker and lines
            map.clear();

            //Move camera to points and add marker for start and end point in case we have recorded points
            if (points.size() > 0) {
                //Configure the linestyle and add points
                PolylineOptions options = new PolylineOptions()
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true);
                options.addAll(points);

                //Add line to map
                map.addPolyline(options);

                //Add marker to map
                map.addMarker(new MarkerOptions().position(points.get(0))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("Start"));
                map.addMarker(new MarkerOptions().position(points.get(points.size() - 1))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title("Ende"));

                //Move camera to the start position
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 14));
            }
        }

    }

    public void stopThreads(){
        if(sensorThread != null){
            sensorThread.stopSensorThread();
            try{
                sensorThread.join(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        getContainerBack();
    }
}
