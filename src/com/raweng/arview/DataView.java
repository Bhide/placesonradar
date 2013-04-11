package com.raweng.arview;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gpstracking.R;
import com.raweng.arview.utils.Camera;
import com.raweng.arview.utils.PaintUtils;
import com.raweng.arview.utils.RadarLines;

public class DataView {

	public double METERSCONVERSIONMULTIPLE = 0.000621371;
	/*
	 * 
	 * */
	RelativeLayout[] locationMarkerView;
	ImageView[] subjectImageView;
	RelativeLayout.LayoutParams[] layoutParams;
	RelativeLayout.LayoutParams[] subjectImageViewParams;
	RelativeLayout.LayoutParams[] subjectTextViewParams;
	TextView[] locationTextView;
	int imageHeight;
	int imageWidth;
	/*
	 * 
	 * */

	ArrayList<Integer> matchIDs = new ArrayList<Integer>();
	ArrayList<Double> arrLat = new ArrayList<Double>();
	ArrayList<Double> arrLong = new ArrayList<Double>();
	ArrayList<Double> arrBearing = new ArrayList<Double>();
	public ArrayList<String> arrDisplayName = new ArrayList<String>();
	public ArrayList<Bitmap> arrBitmap = new ArrayList<Bitmap>();
	ArrayList<String> arrImageURL = new ArrayList<String>();
	ArrayList<Double> arrDistances = new ArrayList<Double>();
	public ArrayList<String> arrIDs = new ArrayList<String>();
	public ArrayList<String> arrDisplayNameShownNowOnScreen = new ArrayList<String>();
	private Activity activity = null;
	double currentLat;
	double currentLong;
	double largest;

	int[] nextXofText;

	float yPosition;
	float angleToShift;
	float yawPrevious;
	Location currentLocation = new Location("provider");
	Location destinedLocation = new Location("provider");
	/** is the view Inited? */
	boolean isInit = false;
	Context _context;

	public static int XPADDING = 0;
	public static int YPADDING = 0;

	/** width and height of the view*/
	int width, height;
	/** _NOT_ the android camera, the class that takes care of the transformation*/
	Camera cam;
	android.hardware.Camera camera;

	float yaw = 0;
	float pitch = 0;
	float roll = 0;

	DisplayMetrics displayMetrics;
	RadarView radarPoints;

	RadarLines lrl = new RadarLines();
	RadarLines rrl = new RadarLines();
	float rx = 10, ry = 20;
	public float addX = 0, addY = 0;
	public float degreetopixelWidth;
	public float degreetopixelHeight;
	public float pixelstodp;
	public float bearing;

	public int[][] coordinateArray;
	public float[] results = new float[1];
	public int[][] rectDimensionArray ;

	Bitmap imageIcon;
	public String MatchedPosition; 
	public boolean isBottomLayerVisible;

	private DecimalFormat df;

	public DataView(Context ctx) {
		this._context = ctx;
		YPADDING = (int)converToPix(40);

	}

	public boolean isInited() {
		return isInit;
	}

	public void init(int widthInit, int heightInit, android.hardware.Camera camera, DisplayMetrics displayMetrics, 
			ArrayList<HashMap<String, Object>> subjecthashmap, double currentLat, double currentLong, RelativeLayout rel) {
		int subjecthashmapLength = subjecthashmap.size(); 
		locationMarkerView = new RelativeLayout[subjecthashmapLength];
		layoutParams = new RelativeLayout.LayoutParams[subjecthashmapLength];
		subjectImageViewParams = new RelativeLayout.LayoutParams[subjecthashmapLength];
		subjectTextViewParams = new RelativeLayout.LayoutParams[subjecthashmapLength];
		subjectImageView = new ImageView[subjecthashmapLength];
		locationTextView = new TextView[subjecthashmapLength];
		nextXofText = new int[subjecthashmapLength];

		imageHeight = convertToPix(35);
		imageWidth = convertToPix(35);
		int subjectTextViewHeight = 0;
		subjectTextViewHeight = convertToPix(55);
		for(int i = 0; i < subjecthashmap.size(); i++){
			
			double lat = (Double) subjecthashmap.get(i).get("location_lat");
			double lon = (Double) subjecthashmap.get(i).get("location_long");
			String subname = (String) subjecthashmap.get(i).get("name");
			arrLat.add(lat);
			arrLong.add(lon);
			arrDisplayName.add(subname);
			arrIDs.add((String)subjecthashmap.get(i).get("id"));
			arrImageURL.add((String) subjecthashmap.get(i).get("icon"));
			arrBitmap.add((BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_subject_missing_medium)));

			layoutParams[i] = new RelativeLayout.LayoutParams(0, 0);
			subjectTextViewParams[i] = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, subjectTextViewHeight);

			subjectImageView[i] = new ImageView(_context);
			locationMarkerView[i] = new RelativeLayout(_context);
			locationTextView[i] = new TextView(_context);
			locationTextView[i].setText(checkTextToDisplay(subname));
			locationTextView[i].setTextColor(Color.WHITE);
			locationTextView[i].setTextSize(10);
			locationTextView[i].setMaxLines(3);
			subjectImageView[i].setBackgroundResource(R.drawable.icon);
			locationMarkerView[i].setId(i);
			subjectImageView[i].setId(i);
			locationTextView[i].setId(i);
			subjectImageViewParams[i] = new  RelativeLayout.LayoutParams(imageWidth, imageHeight);
			subjectImageViewParams[i].topMargin = convertToPix(15);
			subjectImageViewParams[i].leftMargin = convertToPix(5);
			subjectImageViewParams[i].addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			layoutParams[i].setMargins(displayMetrics.widthPixels/2, displayMetrics.heightPixels/2, 0, 0);
			subjectImageView[i].setLayoutParams(subjectImageViewParams[i]);


			locationMarkerView[i].setBackgroundResource(R.drawable.thoughtbubble);
			subjectTextViewParams[i].addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
			subjectTextViewParams[i].topMargin = convertToPix(15);
			subjectTextViewParams[i].leftMargin = convertToPix(45);
			System.out.println("HEIGHT :"+layoutParams[i].height+"WIDTH :"+layoutParams[i].width);
			locationMarkerView[i].setLayoutParams(layoutParams[i]);

			locationTextView[i].setLayoutParams(subjectTextViewParams[i]);

			locationMarkerView[i].addView(subjectImageView[i]);
			locationMarkerView[i].addView(locationTextView[i],subjectTextViewParams[i]);
			rel.addView(locationMarkerView[i], layoutParams[i]);

			subjectImageView[i].setClickable(false);
			locationTextView[i].setClickable(false);

			subjectImageView[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v.getId() != -1) {
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationMarkerView[v.getId()].getLayoutParams();
						Rect rect = new Rect(params.leftMargin, params.topMargin, params.leftMargin + params.width, params.topMargin + params.height);
						Rect compRect = new Rect();
						int index = 0;
						matchIDs.clear();
						for (RelativeLayout.LayoutParams layoutparams : layoutParams) {
							compRect.set(layoutparams.leftMargin, layoutparams.topMargin, 
									layoutparams.leftMargin + layoutparams.width, layoutparams.topMargin + layoutparams.height);
							if (compRect.intersect(rect)) {
								matchIDs.add(index);
							}
							index++;
						}

						if (matchIDs.size() > 1) {
							AlertDialog.Builder builder = new AlertDialog.Builder(_context);
							builder.setTitle("Subjects Near you");

							ListView modeList = new ListView(_context);
							NearbyPlacesList nearbyPlacesList = new NearbyPlacesList(matchIDs, _context);
							modeList.setAdapter(nearbyPlacesList);
							modeList.setOnItemClickListener(itemClickListener);
							builder.setView(modeList);
							final Dialog dialog = builder.create();

							dialog.show();
						}else{
//							openSubjectController(arrIDs.get(v.getId()));

						}

						locationMarkerView[v.getId()].bringToFront();
					}

				}
			});


			locationTextView[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if ((v.getId() != -1)) {
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationMarkerView[v.getId()].getLayoutParams();
						Rect rect = new Rect(params.leftMargin, params.topMargin, params.leftMargin + params.width, params.topMargin + params.height);
						Rect compRect = new Rect();
						int index = 0;
						matchIDs.clear();
						for (RelativeLayout.LayoutParams layoutparams : layoutParams) {
							compRect.set(layoutparams.leftMargin, layoutparams.topMargin, 
									layoutparams.leftMargin + layoutparams.width, layoutparams.topMargin + layoutparams.height);
							if (compRect.intersect(rect)) {
								matchIDs.add(index);
							}
							index++;
						}

						if (matchIDs.size() > 1) {
							AlertDialog.Builder builder = new AlertDialog.Builder(_context);
							builder.setTitle("Subjects Near you");

							ListView modeList = new ListView(_context);
							NearbyPlacesList nearbyPlacesList = new NearbyPlacesList(matchIDs, _context);
							modeList.setAdapter(nearbyPlacesList);
							modeList.setOnItemClickListener(itemClickListener);
							builder.setView(modeList);
							final Dialog dialog = builder.create();

							dialog.show();
						}else{
							openSubjectController(arrIDs.get(v.getId()));

						}

						locationMarkerView[v.getId()].bringToFront();
					}

				}
			});

			locationMarkerView[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v.getId() != -1) {
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationMarkerView[v.getId()].getLayoutParams();
						Rect rect = new Rect(params.leftMargin, params.topMargin, params.leftMargin + params.width, params.topMargin + params.height);
						Rect compRect = new Rect();
						int index = 0;
						matchIDs.clear();
						for (RelativeLayout.LayoutParams layoutparams : layoutParams) {
							compRect.set(layoutparams.leftMargin, layoutparams.topMargin, 
									layoutparams.leftMargin + layoutparams.width, layoutparams.topMargin + layoutparams.height);
							if (compRect.intersect(rect)) {
								matchIDs.add(index);
							}
							index++;
						}

						if (matchIDs.size() > 1) {
							AlertDialog.Builder builder = new AlertDialog.Builder(_context);
							builder.setTitle("Subjects Near you");

							ListView modeList = new ListView(_context);
							NearbyPlacesList nearbyPlacesList = new NearbyPlacesList(matchIDs, _context);
							modeList.setAdapter(nearbyPlacesList);
							modeList.setOnItemClickListener(itemClickListener);
							builder.setView(modeList);
							final Dialog dialog = builder.create();

							dialog.show();
						}else{
							openSubjectController(arrIDs.get(v.getId()));

						}

						locationMarkerView[v.getId()].bringToFront();
					}

				}
			});
		}


		if(subjecthashmap.size()>0){
			ARViewImageDownloader request = new ARViewImageDownloader(this._context);
			request.execute(arrImageURL);
			request.dataView = this;
		}
		this.currentLat = currentLat;
		this.currentLong = currentLong;
		nextXofText = new int[arrLat.size()];
		coordinateArray = new int[arrLat.size()][2];
		rectDimensionArray = new int[arrLat.size()][4];

		df = new DecimalFormat("###.##");

		for(int j=0; j < arrLat.size();j++){
			Location.distanceBetween(currentLat, currentLong, arrLat.get(j), arrLong.get(j), results);
			arrDistances.add((double) results[0]);
		}
		if(arrDistances.size()>0){
			largest = arrDistances.get(0);
			for(int i=1; i< arrDistances.size(); i++)
			{
				if(arrDistances.get(i) > largest)
					largest = arrDistances.get(i);

			}
			largest = largest / 1000;
		}else{
			largest = converToPix(10);
		}
		try {
			currentLocation.setLatitude(this.currentLat);
			currentLocation.setLongitude(this.currentLong);

			this.displayMetrics = displayMetrics;
			this.degreetopixelWidth = this.displayMetrics.widthPixels / camera.getParameters().getHorizontalViewAngle();
			this.degreetopixelHeight = this.displayMetrics.widthPixels / camera.getParameters().getVerticalViewAngle();
			radarPoints = new RadarView(this, arrLat, arrLong, largest, _context, currentLat, currentLong);


			if(bearing < 0)
				bearing  = 360 + bearing;

			for(int i = 0; i <arrLat.size();i++){
				destinedLocation.setLatitude(arrLat.get(i));
				destinedLocation.setLongitude(arrLong.get(i));
				bearing = currentLocation.bearingTo(destinedLocation);

				if(bearing < 0){
					bearing  = 360 + bearing;
				}
				arrBearing.add((double) bearing);
			}
			this.camera = camera;
			width = widthInit;
			height = heightInit;

			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

			lrl.set(0, -RadarView.RADIUS);
			lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
			lrl.add(rx + RadarView.RADIUS, ry + RadarView.RADIUS);
			rrl.set(0, -RadarView.RADIUS);
			rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
			rrl.add(rx + RadarView.RADIUS, ry + RadarView.RADIUS);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		isInit = true;
	}

	public void openSubjectController(String subjectID){

//		Intent subjectIntent = new Intent(_context, UISubjectWallScreen.class);
//		subjectIntent.putExtra("subjectId", subjectID);
//		_context.startActivity(subjectIntent);
//		activity = (Activity) _context;
//		//		activity.finish();
//		activity.overridePendingTransition(R.anim.hold, R.anim.up_from_bottom_rev);
	}

	public void draw(PaintUtils dw, float yaw, float pitch, float roll) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;

		// Draw Radar
		String	dirTxt = "";
		int bearing = (int) this.yaw; 
		int range = (int) (this.yaw / (360f / 16f));
		if (range == 15 || range == 0) dirTxt = "N"; 
		else if (range == 1 || range == 2) dirTxt = "NE"; 
		else if (range == 3 || range == 4) dirTxt = "E"; 
		else if (range == 5 || range == 6) dirTxt = "SE";
		else if (range == 7 || range == 8) dirTxt= "S"; 
		else if (range == 9 || range == 10) dirTxt = "SW"; 
		else if (range == 11 || range == 12) dirTxt = "W"; 
		else if (range == 13 || range == 14) dirTxt = "NW";

		try{
			radarPoints.view = this;
		}catch(Exception e){}

		dw.paintObj(radarPoints, rx+PaintUtils.XPADDING, ry+PaintUtils.YPADDING, -this.yaw, 1, this.yaw);
		dw.setFill(false);
		dw.setColor(Color.argb(100,220,0,0));
		dw.paintLine( lrl.x, lrl.y, rx+RadarView.RADIUS, ry+RadarView.RADIUS); 
		dw.paintLine( rrl.x, rrl.y, rx+RadarView.RADIUS, ry+RadarView.RADIUS);
		dw.setColor(Color.rgb(255,255,255));
		dw.setFontSize(12);
		radarText(dw, "" + bearing + ((char) 176) + " " + dirTxt, rx + RadarView.RADIUS, ry - 5, true, false, -1);
		drawTextBlock(dw);

	}

	void radarText(PaintUtils dw, String txt, float x, float y, boolean bg, boolean isLocationBlock, int count) {

		float padw = 4, padh = 2;
		float w = dw.getTextWidth(txt) + padw * 2;
		float h;
		int adjustments = 0;
		if(isLocationBlock){
			h = dw.getTextAsc() + dw.getTextDesc() + padh * 2+10;
		}else{
			h = dw.getTextAsc() + dw.getTextDesc() + padh * 2;
		}
		if (bg) {

			if(isLocationBlock){
				locationTextView[count].setText(checkTextToDisplay(arrDisplayName.get(count))+"\n"+df.format((arrDistances.get(count) * METERSCONVERSIONMULTIPLE))+" miles");
				subjectImageView[count].setImageBitmap(arrBitmap.get(count));
				if (count % 2 == 0) {
					adjustments = 50;
				}else{
					adjustments = 0;
				}
				layoutParams[count].setMargins((int)(x - w / 2 - 10), (int)(y - h / 2 - 10) - adjustments, 0, 0);
				layoutParams[count].height = convertToPix(80);
				layoutParams[count].width = convertToPix(100);
				locationMarkerView[count].setLayoutParams(layoutParams[count]);

			}else{
				dw.setColor(Color.rgb(0, 0, 0));
				dw.setFill(true);
				dw.paintRect((x - w / 2) + PaintUtils.XPADDING , (y - h / 2) + PaintUtils.YPADDING, w, h);
				pixelstodp = (padw + x - w / 2)/((displayMetrics.density)/160);
				dw.setColor(Color.rgb(255, 255, 255));
				dw.setFill(false);
				dw.paintText((padw + x -w/2)+PaintUtils.XPADDING, ((padh + dw.getTextAsc() + y - h / 2)) + PaintUtils.YPADDING,txt);     
			}
		}

	}

	String checkTextToDisplay(String str){
		if(str.length()>15){
			str = str.substring(0, 15)+"...";
		}
		return str;
	}

	void drawTextBlock(PaintUtils dw){
		int arrLatSize = arrLat.size();
		for(int i = 0; i<arrLatSize;i++){
			if(arrBearing.get(i)<0){

				if(this.pitch != 90){
					yPosition = (this.pitch - 90) * this.degreetopixelHeight+200;
				}else{
					yPosition = (float)this.height/2;
				}
				arrBearing.set(i, 360 - arrBearing.get(i));
				angleToShift = arrBearing.get(i).floatValue() - this.yaw;
				nextXofText[i] = (int)(angleToShift*degreetopixelWidth);
				yawPrevious = this.yaw;
				radarText(dw, arrDisplayName.get(i), nextXofText[i], yPosition, true, true, i);
				coordinateArray[i][0] =  nextXofText[i];
				coordinateArray[i][1] =   (int)yPosition;

			}else{
				angleToShift = (float)arrBearing.get(i).floatValue() - this.yaw;

				if(this.pitch != 90){
					yPosition = (this.pitch - 90) * this.degreetopixelHeight+200;
				}else{
					yPosition = (float)this.height/2;
				}


				nextXofText[i] = (int)((displayMetrics.widthPixels/2)+(angleToShift*degreetopixelWidth));
				if(Math.abs(coordinateArray[i][0] - nextXofText[i]) > 50){
					radarText(dw, arrDisplayName.get(i), (nextXofText[i]), yPosition, true, true, i);
					coordinateArray[i][0] =  (int)((displayMetrics.widthPixels/2)+(angleToShift*degreetopixelWidth));
					coordinateArray[i][1] =  (int)yPosition;
				}else{
					radarText(dw, arrDisplayName.get(i),coordinateArray[i][0],yPosition, true, true, i);
				}
			}
		}
	}

	public int convertToPix(int val){
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, _context.getResources().getDisplayMetrics());
		return (int)px;

	}

	public OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {

			openSubjectController(arrIDs.get(matchIDs.get(position)));

		}
	};

	public class NearbyPlacesList extends BaseAdapter{

		ArrayList<Integer> matchIDs = new ArrayList<Integer>();
		private LayoutInflater inflater;

		class ViewHolder {
			TextView txt_title;
			TextView txt_subTitle;
			ImageView image_thumbnail;
		}

		public NearbyPlacesList(ArrayList<Integer> matchID, Context context){
			matchIDs = matchID;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return matchIDs.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.arpoplist, null);
				holder = new ViewHolder();
				holder.txt_title = (TextView) convertView.findViewById(R.id.titleTextView);
				holder.txt_subTitle = (TextView) convertView.findViewById(R.id.nearByDistanceTextView);
				holder.image_thumbnail = (ImageView) convertView.findViewById(R.id.peopleImageView);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.txt_title.setText(arrDisplayName.get(matchIDs.get(position)));
			holder.txt_subTitle.setText(df.format(arrDistances.get(matchIDs.get(position)) * METERSCONVERSIONMULTIPLE)+" miles");
			holder.image_thumbnail.setImageBitmap(arrBitmap.get(matchIDs.get(position)));

			return convertView;
		}
	}
	
	public float converToPix(int val) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, _context.getResources().getDisplayMetrics());
		return px;
	}
}