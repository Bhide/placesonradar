package com.example.gpstracking;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidGPSTrackingActivity extends Activity {

	Button btnShowLocation = null;
	Button placesSearchButton = null;
	Spinner spinner1 = null;
	Animation rotation = null;
	ImageView progressbarImageView = null;
	TextView loadingTextView = null;
	RelativeLayout loadingContainer = null;

	Dialog dialog = null;
	TextView tv = null;
	TextView details = null;

	private Context _context = null;

	// GPSTracker class
	GPSTracker gps;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		_context = AndroidGPSTrackingActivity.this;

		btnShowLocation = (Button) findViewById(R.id.btnShowLocation);

		placesSearchButton = (Button) findViewById(R.id.placesSearchButton);

		spinner1 = (Spinner) findViewById(R.id.spinner1);

		loadingContainer = (RelativeLayout) findViewById(R.id.loadingContainer);
		progressbarImageView = (ImageView) findViewById(R.id.progressbarImageView);
		loadingTextView = (TextView) findViewById(R.id.loadingTextView);

		loadingContainer.setVisibility(View.GONE);
		loadingContainer.bringToFront();

		dialog = new Dialog(_context,
				android.R.style.Theme_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(true);
		dialog.setContentView(R.layout.address_dialog);
		
		tv = (TextView) dialog.findViewById(R.id.tv);
		details = (TextView) dialog.findViewById(R.id.details);

		

		rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_rotation);
		rotation.setRepeatCount(Animation.INFINITE);
		progressbarImageView.startAnimation(rotation);

		gps = new GPSTracker(AndroidGPSTrackingActivity.this);

		// show location button click event
		btnShowLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// create class object


				// check if GPS enabled		
				if(gps.canGetLocation()){
					loadingContainer.setVisibility(View.VISIBLE);
					loadingTextView.setText("Getting Current Location Address...");

					double latitude = gps.getLatitude();
					double longitude = gps.getLongitude();
					Location location = new Location("Reverse Geocoding");
					location.setLatitude(latitude);
					location.setLongitude(longitude);

					PlacesSearch placesSearch = PlacesSearch.getInstance();
					placesSearch.getAddress(location, new PlacesSearchListener() {

						@Override
						public void onSuccess(JSONObject jsonObject) {
							System.out.println("NEARBY PLACES JSON "+jsonObject.toString());
							try {
								JSONArray nearbyPlacesArray = jsonObject.getJSONArray("results");
								boolean isArrayEmpty = nearbyPlacesArray.length() > 0 == true ? false : true;
								if (!isArrayEmpty) {
									JSONObject formattedAddressObject = nearbyPlacesArray.getJSONObject(0);
									String formatted_address = formattedAddressObject.getString("formatted_address");
									System.out.println("FORMATTED ADDRESS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>."+formatted_address);
									showCustomDialog(formatted_address);
									loadingContainer.setVisibility(View.GONE);
								}
							} catch (JSONException e) {
								e.printStackTrace();
								loadingContainer.setVisibility(View.GONE);
							}
						}

						@Override
						public void onFailure(String error) {

						}
					});
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
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>SPINNER SELECTED POSTITION<<<<<<<<<<<<<<<<<<"+spinner1.getSelectedItemPosition());
				if (spinner1.getSelectedItemPosition() != 0) {
					loadingContainer.setVisibility(View.VISIBLE);
					PlacesSearch placesSearch = PlacesSearch.getInstance();
					Location location = new Location("Location");
					location.setLatitude(gps.getLatitude());
					location.setLongitude(gps.getLongitude());
					String type = spinner1.getSelectedItem().toString().trim();
					loadingTextView.setText("Searching for "+type);
					placesSearch.getPlaces(AppConstants.GOOGLE_CONSOLE_API_KEY, type, location, new PlacesSearchListener() {

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
								loadingContainer.setVisibility(View.GONE);
								placesNearList.clear();
								placesNearList = null;
							} catch (JSONException e) {
								e.printStackTrace();
								loadingContainer.setVisibility(View.GONE);
							}
						}

						@Override
						public void onFailure(String error) {
							loadingContainer.setVisibility(View.GONE);
							System.out.println("GET PLACES ERROR");
						}
					});
				}else{
					Toast.makeText(_context, "Please select the type...", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	protected void showCustomDialog(String address) {
		details.setText(address);
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		gps.stopUsingGPS();
		rotation.cancel();
		rotation = null;
		System.gc();
	}

}