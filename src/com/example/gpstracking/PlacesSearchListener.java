package com.example.gpstracking;

import org.json.JSONObject;

public abstract class PlacesSearchListener {

	public abstract void onSuccess(JSONObject jsonObject);
	
	public abstract void onFailure(String error);
	
}
