package de.tum.mw.ftm.followthatcar;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.List;

import de.tum.mw.ftm.followthatcar.util.MySingleton;

public class MainActivity extends Activity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY = "AIzaSyAEzZuoJ4EooqZqnARsVsAeVbVZjixzPJQ";

    private FloatingActionButton fab;
    private FloatingActionButton userFab;
    private FrameLayout container;
    private boolean isServiceRunning = false;
    private GoogleMap googleMap;

    private enum FragmentNow{IDLE, FOLLOW_ME, FOLLOW_OTHER};
    private FragmentNow fragmentNow;

    private String lat = "";
    private String lng = "";

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
                    if(getFragmentManager().findFragmentById(R.id.container) instanceof InputFragment) {
                        //TODO check valid input first
                        Log.d(TAG, "onClick: is InputFragment");
                        loginId();
                        //TODO if error, retry
                        //TODO start recording user's position and upload to server
                    }else if(getFragmentManager().findFragmentById(R.id.container) instanceof ShowFragment) {
                        Log.d(TAG, "onClick: is ShowFragment");
                        registerId();
                        //TODO if error, regenerate id
                        //TODO wait for response
                    }

                } else {
                    // Set view away
                    getContainerBack();
                }

            }
        });

        userFab = findViewById(R.id.user);
        userFab.setAlpha(0.75f);

        //enable cookie
        CookieHandler.setDefault(new CookieManager());
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
        this.googleMap = googleMap;
        Log.d(TAG, "onMapReady: ready");
    }

    public void addFab() {
        FloatingActionButton floatingActionButton = new FloatingActionButton(this);
    }

    public void registerId(){
        String url = "https://followmeapp.azurewebsites.net/register.php";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", "800000014");
            jsonObject.put("password", "1234");
        }catch (JSONException e){
            Log.d(TAG, "registerId: Exception: " + e.toString());
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        //TODO process response
                        if(response.has("0")){
                            try {
                                JSONObject jsonObject1 = response.getJSONObject("0");
                                if(jsonObject1.getString("error").equals("false")){
                                    //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    // Set view away
                                    moveContainerAway();
                                }else{
                                    Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onResponse: " + jsonObject1.getString("errorMsg"));
                                }
                            }catch(JSONException e){
                                Log.d(TAG, "onResponse: error response: " + e.toString());
                            }
                        }
//                        try {
//                            //String shortUrl = response.getString("url");
//                            Log.d(TAG, "onResponse: " + response.toString());
//                        }catch (JSONException e){
//                            Log.d(TAG, "onResponse: error response: " + e.toString());
//                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onResponse: error detected" + error.toString());
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void loginId(){
        String url = "https://followmeapp.azurewebsites.net/login.php";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", "800000012");
            jsonObject.put("password", "1234");
        }catch (JSONException e){
            Log.d(TAG, "registerId: Exception: " + e.toString());
        }

        //TODO get and save cookie
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        // process response
                        try {
                            JSONObject jsonObject1 = response.getJSONObject("0");
                            if(jsonObject1.getString("error").equals("false")){
                                //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                // Set view away
                                moveContainerAway();
                            }else{
                                Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onResponse: " + jsonObject1.getString("errorMsg"));
                                //TODO regenerate id
                            }
                        }catch(JSONException e){
                            Log.d(TAG, "onResponse: error response: " + e.toString());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onResponse: error detected" + error.toString());
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //TODO upload position in thread
    public void uploadPos(){
        String url = "https://followmeapp.azurewebsites.net/upload.php";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", "800000013");
            jsonObject.put("lat", "1234");
            jsonObject.put("lng", "1234");
        }catch (JSONException e){
            Log.d(TAG, "registerId: Exception: " + e.toString());
        }

        //TODO get and save cookie
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        //TODO process response
//                        try {
//                            //String shortUrl = response.getString("url");
//                            Log.d(TAG, "onResponse: " + response.toString());
//                        }catch (JSONException e){
//                            Log.d(TAG, "onResponse: error response: " + e.toString());
//                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d(TAG, "onResponse: error detected" + error.toString());
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void downloadPos(){
        String url = "https://followmeapp.azurewebsites.net/download.php";

        //TODO get and save cookie
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        //TODO process response
                        if(response.has("0")){
                            try {
                                JSONObject jsonObject1 = response.getJSONObject("0");
                                if(jsonObject1.getString("error").equals("false")){
                                    //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    lat = jsonObject1.getString("lat");
                                    lng = jsonObject1.getString("lng");
                                }else{
                                    Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onResponse: " + jsonObject1.getString("errorMsg"));
                                }
                            }catch(JSONException e){
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

    public void getGoogleMapPoly(List<LatLng> startEnd){
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + startEnd.get(1).latitude
                + ","
                + startEnd.get(1).longitude
                + "&destination=" +
                startEnd.get(2).latitude
                + ","
                + startEnd.get(2).longitude
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
                        if(response.has("status")){
                            try {
                                if(response.getString("status").equals("OK")) {
                                    JSONObject jsonObject1 = response.getJSONArray("routes").getJSONObject(0);
                                    JSONObject polylines = jsonObject1.getJSONObject("overview_polyline");
                                    if (jsonObject1.has("points")) {
                                        //Toast.makeText(getApplicationContext(), jsonObject1.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                                        List<LatLng> points = PolyUtil.decode(polylines.getString("points"));
                                        startInput(points);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "error while getting route", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onResponse: error while getting route");
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(), "can't connect to google map", Toast.LENGTH_SHORT).show();
                                }
                            }catch(JSONException e){
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

    public void startInput(List<LatLng> points){

    }
}
