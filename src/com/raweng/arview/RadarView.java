package com.raweng.arview;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.TypedValue;
import android.widget.Toast;

import com.example.gpstracking.AppConstants;
import com.raweng.arview.utils.PaintUtils;


public class RadarView{
	/** The screen */
	public DataView view;
	/** The radar's range */
	float range;
	/** Radius in pixel on screen */
	public static float RADIUS = 40;
	/** Position on screen */
	static float originX = 0 , originY = 0;
	/** Color */
	static int radarColor = Color.argb(100, 220, 0, 0);
	Location currentLocation = new Location("provider");
	Location destinedLocation = new Location("provider");
	ArrayList<Double> arrLat = new ArrayList<Double>();
	ArrayList<Double> arrLong = new ArrayList<Double>();
	Context _context;

	double largest;
	float  yaw = 0;
	public float x = 0;
	public float y = 0;
	public float z = 0;
	private double currentLat;
	private double currentLong;
	public float circleOriginX;
	public float circleOriginY;
	private float mscale;
	

	public RadarView(DataView dataView, ArrayList<Double> arrLat, ArrayList<Double> arrLong, double largest, Context context, double currentLat, double currentLong){
		this.arrLat = arrLat;
		this.arrLong = arrLong;
		this._context = context;
		this.largest = largest;
		this.currentLat = currentLat;
		this.currentLong = currentLong;
		calculateMetrics();
	}

	public void calculateMetrics(){
		circleOriginX = originX + RADIUS;
		circleOriginY = originY + RADIUS;
		
		range = (float)convertToPix(10) * 50;
		mscale = range / convertToPix((int)RADIUS);
	}

	public void paint(PaintUtils dw, float yaw) {

//		circleOriginX = originX + RADIUS;
//		circleOriginY = originY + RADIUS;
		this.yaw = yaw;
//		range = (float) convertToPix(largest*1000);		/** Draw the radar */
//		Toast.makeText(_context, "RANGE ??"+largest, Toast.LENGTH_SHORT).show();
		dw.setFill(true);
		dw.setColor(radarColor);
		dw.paintCircle(originX + RADIUS, originY + RADIUS, RADIUS);

		/** put the markers in it */
//		float scale = range / convertToPix((int)RADIUS);
		/**
		 *  Your current location coordinate here.
		 * */
		currentLocation.setLatitude(this.currentLat);
		currentLocation.setLongitude(this.currentLong);

		
		for(int i = 0; i <this.arrLat.size();i++){
			destinedLocation.setLatitude(arrLat.get(i));
			destinedLocation.setLongitude(arrLong.get(i));
			convLocToVec(currentLocation, destinedLocation);
			float x = this.x / mscale;
			float y = this.z / mscale;

			
			if (x * x + y * y < RADIUS * RADIUS) {
				dw.setFill(true);
				dw.setColor(Color.rgb(255, 255, 255));
				dw.paintRect(x + RADIUS, y + RADIUS, 2, 2);
			}
		}
	}

	/** Width on screen */
	public float getWidth() {
		return RADIUS * 2;
	}

	/** Height on screen */
	public float getHeight() {
		return RADIUS * 2;
	}


	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void convLocToVec(Location source, Location destination) {
		float[] z = new float[1];
		
		z[0] = 0;
		Location.distanceBetween(source.getLatitude(), source.getLongitude(), destination
				.getLatitude(), source.getLongitude(), z);
		float[] x = new float[1];
		Location.distanceBetween(source.getLatitude(), source.getLongitude(), source
				.getLatitude(), destination.getLongitude(), x);
		if (source.getLatitude() < destination.getLatitude())
			z[0] *= -1;
		if (source.getLongitude() > destination.getLongitude())
			x[0] *= -1;

		set(x[0], (float) 0, z[0]);
	}
	
	public int convertToPix(int val){
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, _context.getResources().getDisplayMetrics());
		return (int)px;
		 
	}
	
}