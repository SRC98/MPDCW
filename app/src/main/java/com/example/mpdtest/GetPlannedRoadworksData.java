package com.example.mpdtest;

//Name - Shaun Cooper
// Matric Number - S1623587

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class GetPlannedRoadworksData extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
    private CurrentIncidentFragment activity;
    private String PlannedRoadworksurl;
    private XmlPullParserFactory xmlFactoryObject;
    private ProgressDialog prgDialog;

    public GetPlannedRoadworksData(CurrentIncidentFragment activity, String PlannedRoadworksurl) {
        this.activity = activity;
        this.PlannedRoadworksurl = PlannedRoadworksurl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        prgDialog = new ProgressDialog(activity.getContext());
        prgDialog.setTitle("Getting Planned Roadworks from XML");
        prgDialog.setMessage("Loading...");
        prgDialog.show();
    }

    @Override
    protected ArrayList<String> doInBackground(ArrayList<String>... params) {
        try {
            URL url = new URL(this.PlannedRoadworksurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000 /* in milliseconds */);
            connection.setConnectTimeout(15000 /* in milliseconds */);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            InputStream stream = connection.getInputStream();

            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();

            myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            myParser.setInput(stream, null);
            ArrayList<String> result = parseXML(myParser);
            stream.close();

            return result;


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AsyncTask", "exception");
            return null;
        }
    }

    public ArrayList<String> parseXML(XmlPullParser myParser) {

        int event;
        ArrayList<String> result = new ArrayList();
        boolean insideItem = false;

        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (myParser.getName().equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (myParser.getName().equalsIgnoreCase("title")) {
                        if (insideItem)
                            result.add("Road:  " + myParser.nextText());
                    } else if (myParser.getName().equalsIgnoreCase("description")) {
                        if (insideItem)
                            result.add("Description:  " + myParser.nextText());
                    } else if (myParser.getName().equalsIgnoreCase("pubdate")) {
                        if (insideItem)
                            result.add("Date:  " + myParser.nextText());
                    }
                } else if (event == XmlPullParser.END_TAG && myParser.getName().equalsIgnoreCase("item")) {
                    insideItem = false;
                }

                event = myParser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        //Call data back to Main thread
        prgDialog.dismiss();
        activity.callBackDataPW(result);
    }

}
