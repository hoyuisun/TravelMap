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
import android.view.View;
import android.view.View.OnClickListener;
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

public class Modify_stay extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
	
	private String uriAPI = "http://140.125.45.113/contest/post_mysql/modify_stay.php";
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
					Toast.makeText(Modify_stay.this, "上傳成功", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	
	private EditText name_tw;
	private EditText name_en;
	private EditText site_tw;
	private EditText site_en;
	private EditText phone;
	private EditText fax;
	private EditText website;
	private EditText email;
	private EditText lat;
	private EditText lng;
	private Button submit;
	private ImageButton geo;
	
	private Bundle data;
	private LocationClient mLocationClient;
	private static final LocationRequest REQUEST = LocationRequest.create()
		      .setInterval(5000)         // 5 seconds
		      .setFastestInterval(16)    // 16ms = 60fps
		      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_stay);
        data =this.getIntent().getExtras();
        name_tw = (EditText)findViewById(R.id.name_tw);
        name_en = (EditText)findViewById(R.id.name_en);
        site_tw = (EditText)findViewById(R.id.site_tw);
        site_en = (EditText)findViewById(R.id.site_en);
        phone = (EditText)findViewById(R.id.phone);
        fax = (EditText)findViewById(R.id.fax);
        website = (EditText)findViewById(R.id.website);
        email = (EditText)findViewById(R.id.email);
    	lat = (EditText)findViewById(R.id.lat);
    	lng = (EditText)findViewById(R.id.lng);;
    	geo = (ImageButton)findViewById(R.id.geo);
    	geo.setBackground(null);
    	geo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LatLng now = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
				lat.setText(Double.toString(now.latitude));
				lng.setText(Double.toString(now.longitude));
			}
    		
    	});
    	
        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Thread t = new Thread(new sendPostRunnable(data.getString("id"), name_tw.getText().toString(), name_en.getText().toString(), site_tw.getText().toString(), site_en.getText().toString(), 
						phone.getText().toString(), fax.getText().toString(), website.getText().toString(), email.getText().toString(), lat.getText().toString(), lng.getText().toString()));
				t.start();
				finish();
			}
        });
        set_Data();
	}
	
	public void set_Data(){
		name_tw.setText(data.getString("name_tw"));
		name_en.setText(data.getString("name_en"));
		site_tw.setText(data.getString("site_tw"));
		site_en.setText(data.getString("site_en"));
		phone.setText(data.getString("phone"));
		fax.setText(data.getString("fax"));
		website.setText(data.getString("website"));
		email.setText(data.getString("email"));
		lat.setText(data.getString("lat"));
		lng.setText(data.getString("lng"));
	}
	
	class sendPostRunnable implements Runnable {
		String id, name_tw, name_en, phone, fax, website, email, site_tw, site_en, lat, lng;
		// 建構子，設定要傳的字串
		public sendPostRunnable(String id, String name_tw, String name_en, String site_tw, String site_en, String phone, String fax, String website, String email, String lat, String lng) {
			this.id = id;
			this.name_tw = name_tw;
			this.name_en = name_en;
			this.site_tw = site_tw;
			this.site_en = site_en;
			this.phone = phone;
			this.fax = fax;
			this.website = website;
			this.email = email;
			this.lat = lat;
			this.lng = lng;
		}

		@Override
		public void run() {
			String result = sendPostDataToInternet(id, name_tw, name_en, site_tw, site_en, phone, fax, website, email, lat, lng);
			mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
		}

	}

	private String sendPostDataToInternet(String id, String name_tw, String name_en, String site_tw, String site_en, String phone, String fax, String website, String email, String lat, String lng) {

		/* 建立HTTP Post連線 */

		HttpPost httpRequest = new HttpPost(uriAPI);
		/*
		 * Post運作傳送變數必須用NameValuePair[]陣列儲存
		 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", id));
		params.add(new BasicNameValuePair("name_tw", name_tw));
		params.add(new BasicNameValuePair("name_en", name_en));
		params.add(new BasicNameValuePair("site_tw", site_tw));
		params.add(new BasicNameValuePair("site_en", site_en));
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("fax", fax));
		params.add(new BasicNameValuePair("website", website));
		params.add(new BasicNameValuePair("email", email));
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
