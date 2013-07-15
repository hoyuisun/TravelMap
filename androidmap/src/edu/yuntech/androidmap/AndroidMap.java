package edu.yuntech.androidmap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class AndroidMap extends Activity implements OnMapClickListener, OnInfoWindowClickListener, OnMapLongClickListener, OnMarkerClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
	
    private GoogleMap map;
    private LocationClient mLocationClient;
    private TextView mTapTextView;
    private Button btn;
    private Spinner spin;
    private String id[] = new String[1000];
    private int index;
    private List<LatLng> _points = new ArrayList<LatLng>();
    
    private String[] list = {"景點","民宿","餐廳","醫院"};
    private String [] mapping = {"view", "stay", "restaurant", "hospital"};
    private ArrayAdapter<String> listAdapter;
    String lat[] = new String[1000];
	String lng[] = new String[1000];
	LatLng center = new LatLng(24.730870310199286, 121.76321268081665);
	
	private static final LocationRequest REQUEST = LocationRequest.create()
		      .setInterval(5000)         // 5 seconds
		      .setFastestInterval(16)    // 16ms = 60fps
		      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
         *此段必要!! 
         *用來抓取mysql資訊, 拿掉無法抓取
         */
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()  
        .detectDiskReads()  
        .detectDiskWrites()  
        .detectNetwork()  
        .penaltyLog()  
        .build());  
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()  
        .detectLeakedSqlLiteObjects()   
        .penaltyLog()  
        .penaltyDeath()  
        .build()); 
        
        /*
         * android map地圖設定，與中心點之設定
         */
        setUpMapIfNeeded();
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13.0f));
        
        btn = (Button)findViewById(R.id.get_record);
        
        //下拉式選單
        spin = (Spinner)findViewById(R.id.spinner1);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
        spin.setAdapter(listAdapter);
        spin.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
            	map.clear();
                String select = "SELECT * FROM " + mapping[position];
                index = position;
                switch (position){
                case 0:
                	getView(select);
                	break;
                case 1:
                	getStay(select);
                	break;
                case 2:
                	getRestaurant(select);
                	break;
                case 3:
                	getHospital(select);
                	break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
               // TODO Auto-generated method stub
            }
        });
        
    }
    
    //抓取民宿資料庫資料
    public void getStay(String select){
    	try {
            String result = DBConnector.executeQuery(select);
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            String name_tw, name_en, phone, fax, website, email, site_tw, site_en, lat, lng;
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
            	JSONObject jsonData = jsonArray.getJSONObject(i);
                name_tw = jsonData.getString("中文名稱");
                name_en = jsonData.getString("英文名稱");
                phone = jsonData.getString("聯絡電話");
                fax = jsonData.getString("傳真");
                website = jsonData.getString("網址");
                email = jsonData.getString("EMAIL");
                site_tw = jsonData.getString("中文地址");
                site_en = jsonData.getString("英文地址");
                lat = jsonData.getString("lat");
                lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(lat), Double.valueOf(lng)))
	            .draggable(true)
	            .snippet("連絡電話: " + phone + "\n傳真: " + fax + "\n網址: " + website + "\nEMAIL: " + email + "\n中文地址: " + site_tw + "\n英文地址: " + site_en)
	            .title(name_tw));
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
    //抓取醫院資料庫資料
    public void getHospital(String select){
    	try {
            String result = DBConnector.executeQuery(select);
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            String name_tw, phone, site_tw, lat, lng;
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
            	JSONObject jsonData = jsonArray.getJSONObject(i);
                name_tw = jsonData.getString("醫院名稱");
                phone = jsonData.getString("電話");
                site_tw = jsonData.getString("地址");
                lat = jsonData.getString("lat");
                lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(lat), Double.valueOf(lng)))
	            .draggable(true)
	            .snippet("連絡電話: " + phone + "\n中文地址: " + site_tw)
	            .title(name_tw));
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
    //抓取景點資料庫資料
    public void getView(String select){
    	try {
            String result = DBConnector.executeQuery(select);
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            String name_tw, phone, site_tw, lat, lng;
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
            	JSONObject jsonData = jsonArray.getJSONObject(i);
                name_tw = jsonData.getString("地點");
                phone = jsonData.getString("電話");
                site_tw = jsonData.getString("地址");
                lat = jsonData.getString("lat");
                lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(lat), Double.valueOf(lng)))
	            .draggable(true)
	            .snippet("連絡電話: " + phone + "\n中文地址: " + site_tw)
	            .icon(BitmapDescriptorFactory.defaultMarker(120))
	            .title(name_tw));
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
    //抓取餐廳資料庫資料
    public void getRestaurant(String select){
    	try {
            String result = DBConnector.executeQuery(select);
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            String name_tw, people, phone, site_tw, lat, lng;
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
            	JSONObject jsonData = jsonArray.getJSONObject(i);
                name_tw = jsonData.getString("餐廳名稱");
                people = jsonData.getString("負責人");
                phone = jsonData.getString("電話");
                site_tw = jsonData.getString("地址");
                lat = jsonData.getString("lat");
                lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(lat), Double.valueOf(lng)))
	            .draggable(true)
	            .snippet("連絡電話: " + phone + "\n負責人: " + people + "\n中文地址: " + site_tw)
	            .icon(BitmapDescriptorFactory.defaultMarker(210))
	            .title(name_tw));
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
    //規劃路徑，將點放進List中
    public List<LatLng> GetDirection(LatLng position){
		String result = null;
			try {
				LatLng now = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
				
	            String route= "http://map.google.com/maps/api/directions/json?origin=" +
	            		now.latitude + "," + now.longitude +"&destination=" + position.latitude + "," + position.longitude + "&language=en&sensor=true";
	        	HttpClient httpClient = new DefaultHttpClient();
	            HttpPost httpPost = new HttpPost(route);
	            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
	            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            HttpEntity httpEntity = httpResponse.getEntity();
	            InputStream inputStream = httpEntity.getContent();
	            
	            BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
	            StringBuilder builder = new StringBuilder();
	            String line = null;
	            while((line = bufReader.readLine()) != null) {
	                builder.append(line + "\n");
	            }
	            inputStream.close();
	            result = builder.toString();
	            JSONObject jsonObject = new JSONObject(result);
	            JSONArray routeObject = jsonObject.getJSONArray("routes");
                String polyline = routeObject.getJSONObject(0).getJSONObject("overview_polyline").getString("points");

                if (polyline.length() > 0){
                	decodePolylines(polyline);
                }
	            
	        } catch(Exception e) {
	             //Log.e("log_tag", e.toString());
	        	Toast.makeText(AndroidMap.this, "規劃路線失敗，請重新執行!", 5).show();
	        }
			return _points;
	}
    
    /*
     * 折線解碼演算法，解析JSON中的points
     */
    private void decodePolylines(String poly){
        int len = poly.length();
        int index = 0;
        double lat = 0;
        double lng = 0;

        while (index < len){
            int b, shift = 0, result = 0;
            do{
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(lat / 1E5, lng / 1E5);
            _points.add(p);
        }
        Route();
    }
    
    //用PolyLine畫地圖，顯示路徑
    public void Route(){
    	Polyline line;
    	for(int i = 1; i < _points.size(); i++){
    		line = map.addPolyline(new PolylineOptions()
            .add(_points.get(i-1), _points.get(i))
            .width(7)
            .color(Color.GREEN));
    	}
    }
    
    /*
     * 客製化marker介面
     */
    class CustomInfoWindowAdapter implements InfoWindowAdapter {

        // These a both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".

        @Override
        public View getInfoWindow(Marker marker) {
        	View mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
        	View mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        	render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {
            int badge=R.drawable.badge_qld;
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
            //if (marker.equals(mBrisbane)) {
            switch (index){
            case 0:
                badge = R.drawable.landscape;
                break;
            case 1:
            	break;
            case 2:
            	break;
            case 3:
            	badge = R.drawable.hospital;
            	break;
            }
            
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
            //marker標題設定
            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
                
            } else {
                titleUi.setText("");
            }
            //marker內容設定
            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                //snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (map != null) {
                setUpMap();
                map.setMyLocationEnabled(true);
            }
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
    
    //設定地圖Listener
    private void setUpMap() {
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onMapClick(LatLng point) {
        //mTapTextView.setText("tapped, point=" + point);
    }
    
    /*
     * (non-Javadoc)
     * @see com.google.android.gms.maps.GoogleMap.OnMapLongClickListener#onMapLongClick(com.google.android.gms.maps.model.LatLng)
     * 
     * 長按地圖後之動作為是否要新稱地圖資訊
     */
    @Override
    public void onMapLongClick(final LatLng point) {
        
    	new AlertDialog.Builder(AndroidMap.this)
        .setTitle("要進行動作...")
        .setMessage("要新增地圖資訊?")
        .setNegativeButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent = new Intent();
            	switch (index){
	            	case 0:
	            		intent.setClass(AndroidMap.this, Add_view.class);
	            		break;
	            	case 1:
	            		intent.setClass(AndroidMap.this, Add_stay.class);
	            		break;
	            	case 2:
	            		intent.setClass(AndroidMap.this, Add_restaurant.class);
	            		break;
	            	case 3:
	            		intent.setClass(AndroidMap.this, Add_hospital.class);
	            		break;
            	}
            	Bundle bundle = new Bundle();
            	bundle.putDouble("lat", point.latitude);
                bundle.putDouble("lng", point.longitude);
            	//將Bundle物件assign給intent
                intent.putExtras(bundle);
                //切換Activity
                startActivity(intent);
            	//Toast.makeText(getApplicationContext(),result, Toast.LENGTH_SHORT).show();
            }
        })
        .setPositiveButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        })
        .show();
    }
    
	@Override
	public boolean onMarkerClick(final Marker marker) {
		// TODO Auto-generated method stub
		/*
		 *地圖上marker跳動動畫 
		 * 
		 */
		final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator
                        .getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
        
		return false;
	}
	
	@Override
    public void onInfoWindowClick(Marker marker) {
		Dialog(marker);
    }
	
	/*/
	 * 點選marker上訊息後出現的對話框
	 * 1.修改地圖資訊
	 * 2.路線規劃
	 * 3.
	 */
	private void Dialog(final Marker marker){
		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		String[] items = {"修改資訊", "路線導航"};
		builder.setTitle("要進行動作...");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dialog != null) dialog.dismiss();
				switch(which){
				case 0:
					break;
				case 1:
					GetDirection(marker.getPosition());
					break;
				case 2:
					//
					break;
				}
			}
		});
		dialog = builder.create();
		dialog.show();
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
		mLocationClient.requestLocationUpdates(
		        REQUEST,
		        this);  // LocationListener
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
}
