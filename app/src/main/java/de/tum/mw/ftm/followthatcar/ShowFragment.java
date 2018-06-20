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
import java.util.stream.IntStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowFragment extends Fragment {

    TextView tvId;
    TextView tvPin;
    public String stringId, stringPin;

    public ShowFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_show, container, false);
        tvId = view.findViewById(R.id.show_id);
        stringId = String.valueOf(MainActivity.randId);
        tvId.setText(stringId.substring(0,3)+"  "+stringId.substring(3,6)
        +"  "+stringId.substring(6));
        tvPin = view.findViewById(R.id.show_pin);
        stringPin = String.valueOf(MainActivity.randPin);
        tvPin.setText(stringPin.substring(0,1)+" "
                        +stringPin.substring(1,2)+" "
                        +stringPin.substring(2,3)+" "
                        +stringPin.substring(3));


        // Inflate the layout for this fragment
        return view;
    }

}
