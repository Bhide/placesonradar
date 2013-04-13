package com.example.gpstracking;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AndroidGPSTrackingActivity extends Activity {
	
	Button btnShowLocation;
	Button placesSearchButton = null;
	Button showOnARView = null;
	
	// GPSTracker class
	GPSTracker gps;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        
        placesSearchButton = (Button) findViewById(R.id.placesSearchButton);
        
        showOnARView = (Button) findViewById(R.id.showOnARView);
        
        showOnARView.setEnabled(false);
        
        gps = new GPSTracker(AndroidGPSTrackingActivity.this);
        
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {		
				// create class object
		        

				// check if GPS enabled		
		        if(gps.canGetLocation()){
		        	
		        	double latitude = gps.getLatitude();
		        	double longitude = gps.getLongitude();
		        	
		        	// \n is for new line
		        	Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();	
		        }else{
		        	// can't get location
		        	// GPS or Network is not enabled
		        	// Ask user to enable GPS/network in settings
		        	gps.showSettingsAlert();
		        }
				
			}
		});
        
        placesSearchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				PlacesSearch placesSearch = PlacesSearch.getInstance();
				Location location = new Location("Location");
				location.setLatitude(gps.getLatitude());
				location.setLongitude(gps.getLongitude());
				
				placesSearch.getPlaces(AppConstants.GOOGLE_CONSOLE_API_KEY, "restaurant", location, new PlacesSearchListener() {
					
					@Override
					public void onSuccess(JSONObject jsonObject) {
						System.out.println("NEARBY PLACES JSON "+jsonObject.toString());
						try {
							JSONArray nearbyPlacesArray = jsonObject.getJSONArray("results");
							ArrayList<HashMap<String, Object>>placesNearList = new ArrayList<HashMap<String, Object>>();
							for (int placesCount = 0; placesCount < nearbyPlacesArray.length(); placesCount++) {
								JSONObject placesJsonObject = nearbyPlacesArray.getJSONObject(placesCount);
								HashMap<String, Object> hashmap = new HashMap<String, Object>();
								JSONObject locationObject = placesJsonObject.getJSONObject("geometry").getJSONObject("location");
								hashmap.put("id", placesJsonObject.getString("id"));
								hashmap.put("location_lat", Double.parseDouble(locationObject.getString("lat")));
								hashmap.put("location_long", Double.parseDouble(locationObject.getString("lng")));
								hashmap.put("reference", placesJsonObject.getString("reference"));
								hashmap.put("name", placesJsonObject.getString("name"));
								hashmap.put("vicinity", placesJsonObject.getString("vicinity") == "" ? " " :placesJsonObject.getString("vicinity"));
								hashmap.put("icon", placesJsonObject.getString("icon"));
								System.out.println("ADDING "+placesJsonObject.getString("name"));
								placesNearList.add(hashmap);
							}
							Intent arIntent = new Intent(AndroidGPSTrackingActivity.this, UIARView.class);
							arIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							arIntent.putExtra("currentLat", gps.getLatitude());
							arIntent.putExtra("currentLong", gps.getLongitude());
							arIntent.putExtra("placesNearList", placesNearList);
							startActivity(arIntent);
							placesNearList.clear();
							placesNearList = null;
						} catch (JSONException e) {
							e.printStackTrace();
						}
						showOnARView.setEnabled(true);
					}
					
					@Override
					public void onFailure(String error) {
						System.out.println("GET PLACES ERROR");
					}
				});
			}
		});
        
        showOnARView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
			}
		});
    }
    
}