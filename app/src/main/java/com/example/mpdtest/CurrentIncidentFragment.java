package com.example.mpdtest;

//Name - Shaun Cooper
// Matric Number - S1623587

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;

public class CurrentIncidentFragment extends Fragment implements View.OnClickListener {

    private View view;
    private TextView DynamText;
    private ListView List;
    private EditText searchBar;
    private ArrayList<String> savedResults = new ArrayList<>();
    private ArrayList<String> searchedResults = new ArrayList<>();
    private Button CIButton;
    private Button rdWrkResultsButton;
    private Button plndRdWrkResultsButton;
    private Button searchButton;
    private String CurrentIncidenturl = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    private String CurrentRoadworksurl =  "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private String PlannedRoadworksurl = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the Fragment Layout
        view = inflater.inflate(R.layout.fragment_ci, container, false);

        //Text View to dynamically change text
        DynamText = (TextView) view.findViewById(R.id.DynamText);

        //Getting search bar
        searchBar = (EditText) view.findViewById(R.id.searchBar);
        searchBar.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        //Getting search button and adding click listener
        searchButton = (Button) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

        //Get List View
        List = (ListView) view.findViewById(R.id.List);

        //Get Current Incidents Button and add click listener
        CIButton = (Button) view.findViewById(R.id.CIResults);
        CIButton.setOnClickListener(this);

        //Get Current Roadworks Button and add Click Listener
        rdWrkResultsButton = (Button) view.findViewById(R.id.rdWrkResults);
        rdWrkResultsButton.setOnClickListener(this);

        //Get Planned Roadworks Button and Click Listener
        plndRdWrkResultsButton = (Button) view.findViewById(R.id.plndRdWrkResults);
        plndRdWrkResultsButton.setOnClickListener(this);


        if (savedInstanceState != null) {
            DynamText.setText(savedInstanceState.getCharSequence("DynamText"));
            savedResults = savedInstanceState.getStringArrayList("savedResults");

            ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, savedResults);
            List.setAdapter(adapter);
        }

        return view;
    }

    public void onClick(View v) {

        // A way to differentiate between what button is clicked and what each button click does
        if (v.getId() == R.id.CIResults)  {
            new GetCurrentIncidentsData(this, CurrentIncidenturl).execute();
            addCurrentIncidentText();
            showSearchBar();
        } else if (v.getId() == R.id.rdWrkResults) {
            new GetCurrentRoadworksData(this, CurrentRoadworksurl).execute();
            addCurrentRoadworksText();
            showSearchBar();
        } else if (v.getId() == R.id.plndRdWrkResults) {
            new GetPlannedRoadworksData(this, PlannedRoadworksurl).execute();
            addPlannedRoadworksText();
            showSearchBar();
        } else if (v.getId() == R.id.searchButton) {
            searchList();
        }
    }

    public void showSearchBar() {
        searchButton.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.VISIBLE);
    }

    public void searchList() {

        searchedResults.clear();

        for (int i = 0; i < savedResults.size(); i++) {

            if (savedResults.get(i).contains(searchBar.getText())) {
                if (!searchedResults.contains(savedResults.get(i))) {

                    searchedResults.add(savedResults.get(i));
                    searchedResults.add(savedResults.get(i+1));
                    searchedResults.add(savedResults.get(i+2));

                }
            }
        }

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, searchedResults);
        List.setAdapter(adapter);
    }


    //A data call back for Current Incidents
    public void callBackDataCI(ArrayList<String> result) {
        if (result.isEmpty()) {
            result.add("No Current Incidents");
        }

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, result);
        List.setAdapter(adapter);

        //Using clear function as it is faster than remove all function
        savedResults.clear(); // Clears the savedResults array list before adding to it to prevent multiple sets of data in the array list
        for (String CIresults : result) { //For each result will save it to the arrayList allowing it to be saved for instance states
            if (CIresults.contains("Road")) {
                savedResults.add("Incident: " + CIresults);
            } else if (CIresults.contains("Description")) {
                savedResults.add("Description" + CIresults);
            } else if (CIresults.contains("Date")) {
                savedResults.add("Date: " + CIresults);
            }
        }
    }

    //A data call back for Current Roadworks
    public void callBackDataRW(ArrayList<String> result) {

        String descString = "";
        String[] splitDescString;
        //Using clear function as it is faster than remove all function
        savedResults.clear(); // Clears the savedResults array list before adding to it to prevent multiple sets of data in the array list
        for (String RWresults : result) { //For each result will save it to the arrayList allowing it to be saved for instance states
            if (RWresults.contains("Road")) {
                savedResults.add(RWresults);
            } else if (RWresults.contains("Description")) {
                descString = RWresults;
                splitDescString = descString.split("<br />");
                savedResults.add(splitDescString[0]);
                savedResults.add(splitDescString[1]);
            }
        }
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, savedResults);
        List.setAdapter(adapter);
    }

    //A data call back for Planned Roadworks
    public void callBackDataPW(ArrayList<String> result) {

        String descString = "";
        String[] splitDescString;
        //Using clear function as it is faster than remove all function
        savedResults.clear(); // Clears the savedResults array list before adding to it to prevent multiple sets of data in the array list
        for (String PWresults : result) { //For each result will save it to the arrayList allowing it to be saved for instance states
            if (PWresults.contains("Road")) {
                savedResults.add(PWresults);
            } else if (PWresults.contains("Description")) {
                descString = PWresults;
                splitDescString = descString.split("<br />");
                savedResults.add(splitDescString[0]);
                savedResults.add(splitDescString[1]);
                savedResults.add("Info: " + splitDescString[2]);
            }
        }
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, savedResults);
        List.setAdapter(adapter);
    }

    //Adds Current Incidents title above list
    public void addCurrentIncidentText() {
        DynamText.setText("");
        DynamText.setText("Current Incidents");
    }

    //Adds Current Roadworks title above list
    public void addCurrentRoadworksText() {
        DynamText.setText("");
        DynamText.setText("Current Roadworks");
    }

    //Adds Planned Roadworks title above list
    public void addPlannedRoadworksText() {
        DynamText.setText("");
        DynamText.setText("Planned Roadworks");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence("DynamText", DynamText.getText());
        outState.putStringArrayList("savedResults", savedResults);
    }
}

