package com.raweng.arview;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.gpstracking.R;
import com.raweng.arview.DataView;

public class ARViewImageDownloader extends AsyncTask<Object, Object, Object> {

    Context _context;
    ArrayList<String> arrImageURL;
    ArrayList<Bitmap> arrImageBitmap = new ArrayList<Bitmap>();
    Bitmap bitmap;
    public DataView dataView;
    URL URL;
    HttpURLConnection conn;
    InputStream is;

    public ARViewImageDownloader(Context context){
	this._context = context;
    }

    @Override
    protected Object doInBackground(Object... params) {
	arrImageURL = (ArrayList<String>) params[0];
	downloadImage(arrImageURL);
	return null;
    }

    public void downloadImage(ArrayList<String> arrImageURL){
	for(int i = 0;i<arrImageURL.size();i++){
	    try {
		if(arrImageURL.get(i) != null){
		    URL = new URL(arrImageURL.get(i).trim());
		    conn = (HttpURLConnection)URL.openConnection();
		    conn.setDoInput(true);
		    conn.connect();
		    is = conn.getInputStream();
		    bitmap = BitmapFactory.decodeStream(is);
		    if(bitmap != null){
			dataView.arrBitmap.set(i, bitmap);
		    }else{
			dataView.arrBitmap.set(i, BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_subject_missing_medium));
		    }
		}else{
		    dataView.arrBitmap.set(i, BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_subject_missing_medium));
		}

	    } catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    catch(Exception e){
		e.printStackTrace();
	    }

	}
    }

}
