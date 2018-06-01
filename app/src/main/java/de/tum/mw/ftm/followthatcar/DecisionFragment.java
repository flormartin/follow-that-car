package de.tum.mw.ftm.followthatcar;


import android.app.AlertDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class DecisionFragment extends Fragment {

    private Button btnMe;
    private Button btnOther;

    public DecisionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_decision, container, false);

        btnMe = view.findViewById(R.id.followme_btn);
        btnOther = view.findViewById(R.id.followother_btn);

        btnMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.container,new ShowFragment()).commit();
                setFabOn();
            }
        });

        btnOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.container,new InputFragment()).commit();
                setFabOn();
            }
        });

        return view;
    }

    private void setFabOn(){
        getActivity().findViewById(R.id.fab).setVisibility(View.VISIBLE);
    }
}
