package com.example.gpstracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.gpstracking.R;
import com.raweng.arview.DataView;
import com.raweng.arview.utils.Compatibility;
import com.raweng.arview.utils.PaintUtils;

public class UIARView extends Activity implements SensorEventListener{

	private Context _context;
	WakeLock mWakeLock;
	CameraView cameraView;
	RadarMarkerView radarMarkerView;
	PaintUtils paintScreen;
	DataView dataView;
	boolean isInited = false;
	public static float azimuth;
	public static float pitch;
	public static float roll;
	public double latitudeprevious;
	public double longitude;
	String locationContext;
	String provider;
	LocationManager locationManager;
	DisplayMetrics displayMetrics;
	Camera camera;
	public int screenWidth;
	public int screenHeight;

	public static ViewFlipper horizontialListView;
	TextView titleTextView;
	public RelativeLayout upperLayerLayout;

	private float RTmp[] = new float[9];
	private float Rot[] = new float[9];
	private float I[] = new float[9];
	private float grav[] = new float[3];
	private float mag[] = new float[3];
	private float results[] = new float[3];
	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag;
	public ArrayList<HashMap<String, Object>> subjectNearList = new ArrayList<HashMap<String,Object>>();
	public double currentLat;
	public double currentLong;
	
	static final float ALPHA = 0.25f;
	protected float[] gravSensorVals;
	protected float[] magSensorVals;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
//		subjectNearList.clear();
//		subjectNearList = null;
//		
//		dataView = null;
		
		setResult(RESULT_OK);
		finish();
		overridePendingTransition(R.anim.hold, R.anim.up_from_bottom_rev);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

		subjectNearList = (ArrayList<HashMap<String, Object>>) getIntent().getExtras().get("placesNearList");
		
		_context = this;
		currentLat = (Double) getIntent().getExtras().get("currentLat");
		currentLong = (Double) getIntent().getExtras().get("currentLong");

		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");

		displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		screenHeight = displayMetrics.heightPixels;
		screenWidth = displayMetrics.widthPixels;

		FrameLayout headerFrameLayout = new FrameLayout(this);
		RelativeLayout headerRelativeLayout = new RelativeLayout(this);
		headerFrameLayout.setBackgroundResource(R.drawable.bitmap_chat_header);
		RelativeLayout.LayoutParams relaLayoutParams  = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		headerRelativeLayout.setLayoutParams(relaLayoutParams);
		
		upperLayerLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams upperLayerLayoutParams = new RelativeLayout.LayoutParams(android.widget.RelativeLayout.LayoutParams.FILL_PARENT, android.widget.RelativeLayout.LayoutParams.FILL_PARENT);
		upperLayerLayoutParams.topMargin = (int)converToPix(44);
		upperLayerLayout.setLayoutParams(upperLayerLayoutParams);
		upperLayerLayout.setBackgroundColor(Color.TRANSPARENT);
		
		cameraView = new CameraView(this);
		radarMarkerView = new RadarMarkerView(this, displayMetrics, subjectNearList, currentLat, currentLong, upperLayerLayout);

		

		Button cancelButton = new Button(this);
		RelativeLayout.LayoutParams buttonparams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
		buttonparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		buttonparams.addRule(RelativeLayout.CENTER_VERTICAL,RelativeLayout.TRUE);
		buttonparams.setMargins(10, 4, 0, 4);
		cancelButton.setLayoutParams(buttonparams);
		cancelButton.setText("  Cancel  ");
		cancelButton.setTextSize(12);
		cancelButton.setTypeface(null, Typeface.BOLD);
		cancelButton.setTextColor(getResources().getColor(android.R.color.white));
//		cancelButton.setBackgroundResource(R.drawable.selector_cancel_button);

		TextView titleTextView = new TextView(this);
		RelativeLayout.LayoutParams textparams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textparams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		textparams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		titleTextView.setLayoutParams(textparams);
		titleTextView.setText("Augmented  Reality  View");
		titleTextView.setTextColor(getResources().getColor(android.R.color.white));
		titleTextView.setTypeface(null, Typeface.BOLD);
		titleTextView.setTextSize(16);


		headerRelativeLayout.addView(cancelButton);
		headerRelativeLayout.addView(titleTextView);
		headerFrameLayout.addView(headerRelativeLayout);
		setContentView(cameraView);
		addContentView(radarMarkerView,  new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addContentView(headerFrameLayout, new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, (int)converToPix(44),Gravity.TOP));
		addContentView(upperLayerLayout, upperLayerLayoutParams);
		headerFrameLayout.bringToFront();
		
		if(!isInited){
			dataView = new DataView(UIARView.this);
			paintScreen = new PaintUtils();
			isInited = true;
		}

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.hold, R.anim.up_from_bottom_rev);
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		this.mWakeLock.release();
		sensorMgr.unregisterListener(this, sensorGrav);
		sensorMgr.unregisterListener(this, sensorMag);
		sensorMgr = null;
	}

	@Override
	protected void onResume() {

		super.onResume();
		this.mWakeLock.acquire();

		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

		sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensorGrav = sensors.get(0);
		}

		sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensors.size() > 0) {
			sensorMag = sensors.get(0);
		}

		sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
		sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);
	}


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent evt) {


		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			gravSensorVals = lowPass(evt.values.clone(), gravSensorVals);
			grav[0] = evt.values[0];
			grav[1] = evt.values[1];
			grav[2] = evt.values[2];

		} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magSensorVals = lowPass(evt.values.clone(), magSensorVals);
			mag[0] = evt.values[0];
			mag[1] = evt.values[1];
			mag[2] = evt.values[2];

		}
		
		if (gravSensorVals != null && magSensorVals != null) {
			SensorManager.getRotationMatrix(RTmp, I, gravSensorVals, magSensorVals);

			int rotation = Compatibility.getRotation(this);

			if (rotation == 1) {
				SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, Rot);
			} else {
				SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, Rot);
			}

			SensorManager.getOrientation(Rot, results);

			UIARView.azimuth = (float)(((results[0]*180)/Math.PI)+180);
			UIARView.pitch = (float)(((results[1]*180/Math.PI))+90);
			UIARView.roll = (float)(((results[2]*180/Math.PI)));

			radarMarkerView.postInvalidate();
		}
//		gravSensorVals = lowPass(grav, gravSensorVals);
//		magSensorVals = lowPass(mag, magSensorVals);

		
	}
	
	protected float[] lowPass( float[] input, float[] output ) {
	    if ( output == null ) return input;
	     
	    for ( int i=0; i<input.length; i++ ) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	}
	
	public float converToPix(int val) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
		return px;
	}
}



class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	UIARView arView;
	SurfaceHolder holder;
	Camera camera;

	public CameraView(Context context) {
		super(context);
		arView = (UIARView) context;

		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		try {
			Camera.Parameters parameters = camera.getParameters();
			try {
				List<Camera.Size> supportedSizes = null;
				supportedSizes = com.raweng.arview.utils.Compatibility.getSupportedPreviewSizes(parameters);

				Iterator<Camera.Size> itr = supportedSizes.iterator(); 
				while(itr.hasNext()) {
					Camera.Size element = itr.next(); 
					element.width -= w;
					element.height -= h;
				} 
				Collections.sort(supportedSizes, new ResolutionsOrder());
				parameters.setPreviewSize(w + supportedSizes.get(supportedSizes.size()-1).width, h + supportedSizes.get(supportedSizes.size()-1).height);
			} catch (Exception ex) {
				parameters.setPreviewSize(arView.screenWidth , arView.screenHeight);
			}

			camera.setParameters(parameters);
			camera.startPreview();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if (camera != null) {
				try {
					camera.stopPreview();
				} catch (Exception ignore) {
				}
				try {
					camera.release();
				} catch (Exception ignore) {
				}
				camera = null;
			}

			camera = Camera.open();
			arView.camera = camera;
			camera.setPreviewDisplay(holder);
		} catch (Exception ex) {
			try {
				if (camera != null) {
					try {
						camera.stopPreview();
					} catch (Exception ignore) {
					}
					try {
						camera.release();
					} catch (Exception ignore) {
					}
					camera = null;
				}
			} catch (Exception ignore) {

			}
		}		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			if (camera != null) {
				try {
					camera.stopPreview();
				} catch (Exception ignore) {
				}
				try {
					camera.release();
				} catch (Exception ignore) {
				}
				camera = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}

}
class RadarMarkerView extends View{

	UIARView arView;
	DisplayMetrics displayMetrics;
	private ArrayList<HashMap<String, Object>> subjectNearList;
	double currentLat;
	double currentLong;
	RelativeLayout upperLayoutView = null;


	public RadarMarkerView(Context context, DisplayMetrics displayMetrics, 
			ArrayList<HashMap<String, Object>> _subjectNearList, 
			double currentLat, double currentLong, RelativeLayout rel) {
		super(context);
		arView = (UIARView) context;
		this.displayMetrics = displayMetrics;
		subjectNearList = _subjectNearList;
		this.currentLat = currentLat;
		this.currentLong = currentLong;
		upperLayoutView = rel;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		arView.paintScreen.setWidth(canvas.getWidth());
		arView.paintScreen.setHeight(canvas.getHeight());
		arView.paintScreen.setCanvas(canvas);

		if (!arView.dataView.isInited()) {
			arView.dataView.init(arView.paintScreen.getWidth(), arView.paintScreen.getHeight(),
					arView.camera, displayMetrics,subjectNearList, this.currentLat, 
					this.currentLong,upperLayoutView);
		}

		arView.dataView.draw(arView.paintScreen, UIARView.azimuth, UIARView.pitch, UIARView.roll);

	}
}

class ResolutionsOrder implements java.util.Comparator<Camera.Size> {
	public int compare(Camera.Size left, Camera.Size right) {

		return Float.compare(left.width + left.height, right.width + right.height);
	}
}