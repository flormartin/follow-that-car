package de.tum.mw.ftm.followthatcar;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String TAG = MainActivity.class.getSimpleName();

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getFragmentManager().beginTransaction().add(R.id.container, new InputFragment()).commit();

        fab = findViewById(R.id.fab);
        //TODO: function of floating action button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFabIcon(true);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT);
                params.gravity = 0;
                try {
                    blankFragment.getView().setLayoutParams(params);
                }catch (NullPointerException e) {
                    Log.e(TAG, "onClick: ", e);
                }

            }
        });
    }

    /**
     * Sets the correct icon for the floating action button
     * @param isServiceRunning if the Service is running
     */
    private void setFabIcon(boolean isServiceRunning){
        if(isServiceRunning){
            //If service is running set the stop icon
            fab.setImageResource(R.drawable.ic_stop);
        }
        else {
            //If service is not running set the start icon
            fab.setImageResource(R.drawable.ic_start);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public void addFab(){
        FloatingActionButton floatingActionButton = new FloatingActionButton(this);
    }
}
