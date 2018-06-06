package de.tum.mw.ftm.followthatcar;

import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private FloatingActionButton fab;
    private FrameLayout container;
    private boolean isServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getFragmentManager().beginTransaction().add(R.id.container, new DecisionFragment()).commit();
        container = findViewById(R.id.container);

        fab = findViewById(R.id.fab);
        //TODO: function of floating action button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceRunning) {
                    setFabIcon(true);
                    // Set view away
                    moveContainerAway();
                    isServiceRunning = true;
                } else {
                    setFabIcon(false);
                    // Set view away
                    getContainerBack();
                    isServiceRunning = false;
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        boolean show = getFragmentManager().findFragmentById(R.id.container) instanceof ShowFragment;
        boolean input = getFragmentManager().findFragmentById(R.id.container) instanceof InputFragment;
        int visible = container.getVisibility();
        if((show || input) && (visible==0)) {
            getFragmentManager().beginTransaction().replace(R.id.container, new DecisionFragment()).commit();
        }
        else{
            getContainerBack();
        }
    }
    //TODO stop floating action button when "BACK" is pressed
    private void moveContainerAway() {
        container.setVisibility(View.GONE);
    }

    private void getContainerBack() {
        container.setVisibility(View.VISIBLE);
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

    }

    public void addFab() {
        FloatingActionButton floatingActionButton = new FloatingActionButton(this);
    }
}
