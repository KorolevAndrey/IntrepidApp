package com.jenblight.intrepidapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jenblight.intrepidapp.net.ApiWrapper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jgblight on 16-01-02.
 */
public class TripSelector {

    private Context context;
    public JSONArray tripData;
    public List<String> tripNames = new ArrayList<String>();
    public int selectedTrip;
    private Spinner tripSpinner;


    public TripSelector(Context inContext) {
        context = inContext;

        SharedPreferences prefs = context.getSharedPreferences("Intrepid", Context.MODE_PRIVATE);

        if (prefs.contains("trips")){
            try {
                tripData = new JSONArray(prefs.getString("trips", ""));
                selectedTrip = tripData.getJSONObject(0).getInt("id");
            } catch (JSONException e) {
                tripData = new JSONArray();
                selectedTrip = 0;
                e.printStackTrace();
            }
        }
        else {
            tripData = new JSONArray();
            selectedTrip = 0;
        }
        // TODO: new trip option
        for (int i=0; i<tripData.length(); i++){
            try {
                tripNames.add(tripData.getJSONObject(i).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void createSpinner(Spinner inTripSpinner) {
        tripSpinner = inTripSpinner;
        tripSpinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, tripNames));
        tripSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    selectedTrip = tripData.getJSONObject(position).getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void updateTrips() {
        if (isOnline()){
            new getTrips().execute();
        }
        else {
            Toast.makeText(context, "Could not sync",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    class getTrips extends AsyncTask<Void,Void,JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... arg0) {
            ApiWrapper wrapper = new ApiWrapper();
            JSONArray trips = wrapper.getTrips(context);
            wrapper.close();
            return trips;
        }

        @Override
        protected void onPostExecute(JSONArray result){
            if (result != null){
                tripData = result;
                SharedPreferences prefs = context.getSharedPreferences("Intrepid", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("trips", tripData.toString());
                editor.commit();
            }
            else {
                Toast.makeText(context, "Could not update Trips", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
