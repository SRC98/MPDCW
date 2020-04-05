package com.example.mpdtest;

//Name - Shaun Cooper
// Matric Number - S1623587

import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private Button startButton;
    private Button mapButton;
    // Traffic Scotland URLs
    //private String CurrentRoadworksurl = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    //private String PlannedRoadworksurl = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    //private String CurrentIncidenturl = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        mapButton = (Button)findViewById(R.id.mapButton);
        mapButton.setOnClickListener(this);

    }

    public void onClick(View aview) {
        if (aview.getId() == R.id.startButton) {
            loadFragment(new CurrentIncidentFragment());
        } else if (aview.getId() == R.id.mapButton) {
            launchMapActivity();
        }
    }

    private void launchMapActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void loadFragment(Fragment fragment) {
        // create a FragmentManager
        FragmentManager fm = getFragmentManager();
        // create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        // replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.framelayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }

} // End of MainActivity