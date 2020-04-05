package com.example.mpdtest;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.ProgressDialog;

import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ProgressDialog prgDialog;
    private Button backButton;
    ArrayList markerPoints = new ArrayList();
    ArrayList<String> jLatLng = new ArrayList<>();
    private String MY_API_KEY = "AIzaSyA_8S1oBtQ7LBwexp7FgfhL6E2fcVN-9-4";
    private String CurrentIncidenturl = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    private String CurrentRoadworksurl = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private String PlannedRoadworksurl = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        if (savedInstanceState != null) {
            mapFragment.setRetainInstance(true);
            markerPoints = savedInstanceState.getStringArrayList("markerpoints");
        }
    }

    public void onClick(View v) {
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng glasgow = new LatLng(55.8668183, -4.2521489);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(glasgow, 8));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (markerPoints.size() > 1) {
                    markerPoints.clear();
                    mMap.clear();
                }

                // Adding new item to the ArrayList
                markerPoints.add(latLng);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLng);

                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    options.title("Start");
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    options.title("Destination");
                }

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }


            }

        });
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            //Storing data from web
            String data = "";

            //Fetching data
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            //Invokes thread for parsing data

            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DirectionsJSONParser parser = new DirectionsJSONParser();
                Log.d("ParserTask", parser.toString());

                //Starts Parsing Data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
            }
            return routes;
        }

        //Executes in UI thread, after parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            //Traversing all Routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                //Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                //Fetching all points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    String jCoordinates = lat + " " + lng;
                    jLatLng.add("latlng: " + jCoordinates);
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                //Adding all points in route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);
                Log.d("OnPostExecute", "OnPostExecute LineOptions Decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);

            } else {
                Log.d("OnPostExecute", "Without Polylines Drawn");
            }

            if (jLatLng != null) {
                startDataGather();
            } else {
                Log.d("jPoint", "jPoint is null");
            }

        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + MY_API_KEY;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            //Creating HTTP Connection
            urlConnection = (HttpURLConnection) url.openConnection();

            //Connecting to URL
            urlConnection.connect();

            //Reading Data
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void startDataGather() {
        prgDialog = new ProgressDialog(this);
        prgDialog.setTitle("Getting Incidents and Roadworks");
        prgDialog.setMessage("Loading...");
        prgDialog.show();

        new GetRWDataMaps(this, CurrentRoadworksurl).execute();
        new GetCIDataMaps(this, CurrentIncidenturl).execute();

    }


    public void callBackCIDataMaps(ArrayList<String> result) {
        ArrayList<String> ciResults = new ArrayList();
        String title = "";
        String coords = "";
        String[] splitCoords;
        Double latCoords = 0.0;
        Double lngCoords = 0.0;

        String jCoords = "";
        String[] splitJCoords;
        Double jLatCoords = 0.0;
        Double jLngCoords = 0.0;

        Double distBetw = 0.0;

        // For each result returned it checks for certain strings and will do actions if the strings are found
        for (String ciresults : result) {
            if (ciresults.contains("Incident")) {
                ciResults.add(ciresults);
                title = ciresults;
            } else if (ciresults.contains("Description")) {
                ciResults.add(ciresults);
            } else if (ciresults.contains("LatLng")) { // Finds LatLng string
                ciResults.add(ciresults);
                coords = ciresults; // Puts results in coords string
                splitCoords = coords.split("\\s+"); // Splits the coords string by whitespace
                latCoords = Double.parseDouble(splitCoords[1]);
                lngCoords = Double.parseDouble(splitCoords[2]); // Parses each split string into a double field

                for (String jResults : jLatLng) { // After doing above actions, for each result returned, do the following
                    jCoords = jResults; // Put jResults (JSON Route Data) into a string
                    splitJCoords = jCoords.split("\\s+"); // Split the string by whitespace
                    jLatCoords = Double.parseDouble(splitJCoords[1]);
                    jLngCoords = Double.parseDouble(splitJCoords[2]); // Parse the split string into a double
                    distBetw = distance(latCoords, lngCoords, jLatCoords, jLngCoords); // Check the distance between the two locations

                    if (distBetw < 0.1) { //If the incident are within 0.1 of a mile it will display a marker
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latCoords, lngCoords)).title(title));
                    }
                }

            } else if (ciresults.contains("Date")) {
                ciResults.add(ciresults);
            }
        }
        prgDialog.dismiss();
    }

    public void callBackRWDataMaps(ArrayList<String> result) {
        ArrayList<String> rwResults = new ArrayList();
        String title = "";
        String coords = "";
        String[] splitCoords;
        Double latCoords = 0.0;
        Double lngCoords = 0.0;

        String jCoords = "";
        String[] splitJCoords;
        Double jLatCoords = 0.0;
        Double jLngCoords = 0.0;

        double distBetw = 0.0;

        // For each result returned it checks for certain strings and will do actions if the strings are found
        for (String RWresults : result) {
            if (RWresults.contains("Roadworks")) {
                rwResults.add(RWresults);
                title = RWresults;
            } else if (RWresults.contains("Description")) {
                rwResults.add(RWresults);
            } else if (RWresults.contains("LatLng")) { // Finds LatLng string
                rwResults.add(RWresults);
                coords = RWresults; // Puts results in coords string
                splitCoords = coords.split("\\s+"); // Splits the coords string by whitespace
                latCoords = Double.parseDouble(splitCoords[1]);
                lngCoords = Double.parseDouble(splitCoords[2]); // Parses each split string into a double field

                for (String jResults : jLatLng) { // After doing above actions, for each result returned, do the following
                    jCoords = jResults; // Put jResults (JSON Route Data) into a string
                    splitJCoords = jCoords.split("\\s+"); // Split the string by whitespace
                    jLatCoords = Double.parseDouble(splitJCoords[1]);
                    jLngCoords = Double.parseDouble(splitJCoords[2]); // Parse the split string into a double
                    distBetw = distance(latCoords, lngCoords, jLatCoords, jLngCoords); // Check the distance between the two locations

                    if (distBetw < 0.1) { //If the roadworks are within 0.1 of a mile it will display a marker
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latCoords, lngCoords)).title(title));
                    }
                }

                } else if (RWresults.contains("Date")) {
                    rwResults.add(RWresults);
            }
        }
    }



    // calculates the distance between two locations in MILES
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometers

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("markerpoints", markerPoints);
    }
}

