package de.tum.mw.ftm.followthatcar;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowFragment extends Fragment {

    TextView tvId;
    TextView tvPin;

    public ShowFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //create ID and PIN
        Random rand = new Random();
        int id = rand.nextInt(rand.nextInt(100000000) + 999999999); //neunstellig
        int pin = rand.nextInt(rand.nextInt(1000) + 9999); //vierstellig

        View view = inflater.inflate(R.layout.fragment_show, container, false);
        tvId = view.findViewById(R.id.show_id);
        tvId.setText(""+id);
        tvPin = view.findViewById(R.id.show_pin);
        tvId.setText(""+pin);


        // Inflate the layout for this fragment
        return view;
    }

}
