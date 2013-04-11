package com.example.gpstracking;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class PlacesSearch {

	private static PlacesSearch placesSearch = null;
	private static AsyncHttpClient asyncHttpClient = null;
	public PlacesSearchListener placesSearchListener = null;
	
	public static PlacesSearch getInstance(){
		if (placesSearch == null) {
			placesSearch = new PlacesSearch();
			asyncHttpClient = new AsyncHttpClient();
		}
		
		return placesSearch;
	}
	
	public void getPlaces(String api_key, String type, Location location, PlacesSearchListener placesSearchListener){
		this.placesSearchListener = placesSearchListener;
		RequestParams params = new RequestParams();
		if (location != null) {
			params.put("location", String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));
			params.put("radius", AppConstants.RADIUS);
			params.put("types", type);
			params.put("sensor", "false");
			params.put("key", AppConstants.GOOGLE_CONSOLE_API_KEY);
			System.out.println("LOCATION+++++++++++++++++++++++++++++++++++++++++++++++++++ "+location);
			asyncHttpClient.get(AppConstants.NEARBY_SEARCH_URL, params, new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(String arg0) {
					super.onSuccess(arg0);
					System.out.println("GET PLACES SUCCESS "+arg0);
					try {
						JSONObject jsonObject = new JSONObject(arg0);
						PlacesSearch.this.placesSearchListener.onSuccess(jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
						PlacesSearch.this.placesSearchListener.onFailure(e.getMessage());
					}
				}

				@Override
				public void onFailure(Throwable arg0, String arg1) {
					super.onFailure(arg0, arg1);
					PlacesSearch.this.placesSearchListener.onFailure(arg1);
				}
				
			});
//			System.out.println("ASYNC REQUEST "+asyncHttpClient.get);
		}else{
			this.placesSearchListener.onFailure("Cannot find current location");
		}
		
	}
}
