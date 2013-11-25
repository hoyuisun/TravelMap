package edu.yuntech.androidmap;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import edu.yuntech.androidmap.Modify_view.sendPostRunnable;

public class Modify_restaurant extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
	
	private String uriAPI = "http://140.125.45.113/contest/post_mysql/modify_restaurant.php";
	/** 「要更新版面」的訊息代碼 */
	protected static final int REFRESH_DATA = 0x00000001;

	/** 建立UI Thread使用的Handler，來接收其他Thread來的訊息 */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 顯示網路上抓取的資料
			case REFRESH_DATA:
				String result = null;
				if (msg.obj instanceof String)
					result = (String) msg.obj;
				if (result != null)
					// 印出網路回傳的文字
					Toast.makeText(Modify_restaurant.this, "上傳成功", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	
	private EditText name_tw;
	private EditText site_tw;
	private EditText people;
	private EditText phone;
	private EditText lat;
	private EditText lng;
	private ImageButton submit;
	private ImageButton geo;
	
	private Bundle data;
	private LocationClient mLocationClient;
	private static final LocationRequest REQUEST = LocationRequest.create()
		      .setInterval(5000)         // 5 seconds
		      .setFastestInterval(16)    // 16ms = 60fps
		      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_restaurant);
        data =this.getIntent().getExtras();
        name_tw = (EditText)findViewById(R.id.name);
        site_tw = (EditText)findViewById(R.id.site);
        phone = (EditText)findViewById(R.id.phone);
        people = (EditText)findViewById(R.id.people);
    	lat = (EditText)findViewById(R.id.lat);
    	lng = (EditText)findViewById(R.id.lng);;
    	geo = (ImageButton)findViewById(R.id.geo);
    	geo.setBackgroundDrawable(null);
    	geo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LatLng now = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
				lat.setText(Double.toString(now.latitude));
				lng.setText(Double.toString(now.longitude));
			}
    		
    	});
    	
        submit = (ImageButton)findViewById(R.id.submit);
        submit.setBackgroundDrawable(null);
        submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Thread t = new Thread(new sendPostRunnable(data.getString("id"), name_tw.getText().toString(), people.getText().toString(), site_tw.getText().toString(), 
						phone.getText().toString(), lat.getText().toString(), lng.getText().toString()));
				t.start();
				finish();
			}
        });
        submit.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)  //按下的時候改變背景及顏色
					submit.setImageResource(R.drawable.submit_down);
				else
					submit.setImageResource(R.drawable.submit);
				return false;
			}
        	
        });
        set_Data();
	}
	
	public void set_Data(){
		name_tw.setText(data.getString("name_tw"));
		people.setText(data.getString("people"));
		site_tw.setText(data.getString("site_tw"));
		phone.setText(data.getString("phone"));
		lat.setText(data.getString("lat"));
		lng.setText(data.getString("lng"));
	}
	
	class sendPostRunnable implements Runnable {
		String id, name_tw, people, phone, site_tw, lat, lng;
		// 建構子，設定要傳的字串
		public sendPostRunnable(String id, String name_tw, String people, String site_tw, String phone, String lat, String lng) {
			this.id = id;
			this.name_tw = name_tw;
			this.people = people;
			this.site_tw = site_tw;
			this.phone = phone;
			this.lat = lat;
			this.lng = lng;
		}

		@Override
		public void run() {
			String result = sendPostDataToInternet(id, name_tw, people, site_tw, phone, lat, lng);
			mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
		}

	}

	private String sendPostDataToInternet(String id, String name_tw, String people, String site_tw, String phone, String lat, String lng) {

		/* 建立HTTP Post連線 */

		HttpPost httpRequest = new HttpPost(uriAPI);
		/*
		 * Post運作傳送變數必須用NameValuePair[]陣列儲存
		 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", id));
		params.add(new BasicNameValuePair("name", name_tw));
		params.add(new BasicNameValuePair("people", people));
		params.add(new BasicNameValuePair("site", site_tw));
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("lat", lat));
		params.add(new BasicNameValuePair("lng", lng));

		try {
			/* 發出HTTP request */

			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			/* 取得HTTP response */
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
			/* 若狀態碼為200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 取出回應字串 */
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				// 回傳回應字串
				return strResult;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
	    if (mLocationClient != null) {
	    	mLocationClient.disconnect();
	    }
    }
	
	private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
          mLocationClient = new LocationClient(
              getApplicationContext(),
              this,  // ConnectionCallbacks
              this); // OnConnectionFailedListener
        }
      }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
