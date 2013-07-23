package edu.yuntech.androidmap;

import static edu.yuntech.androidmap.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static edu.yuntech.androidmap.CommonUtilities.EXTRA_MESSAGE;
import static edu.yuntech.androidmap.CommonUtilities.SENDER_ID;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
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
    private Spinner spin;
    private ImageButton add;
    private ImageButton near;
    private ImageButton traffic;
    private ImageButton weather;
    private ImageButton star;
    private ImageButton plan;
    private ImageButton roadwork;
    private ImageButton traffic_jam;
    private ImageButton push;
    private ImageButton facebook;
    private ImageButton information;
    private boolean starting = false;
    
    private String index = null;
    private String regId;
    private int table_cnt = 0;
    private int select = 0;
    private List<LatLng> _points = new ArrayList<LatLng>();
    public static ArrayList<LatLng> _view = new ArrayList<LatLng>();
    public static HashMap<String, ArrayList<LatLng>> viewhash = new HashMap<String, ArrayList<LatLng>>();
    public static ArrayList<String> viewStrings= new ArrayList<String>();
    public static ArrayList<LatLng> _planPoints = new ArrayList<LatLng>();
    private List<Data> _data = new ArrayList<Data>();
    private List<String> _table = new ArrayList<String>();
    private HashMap<String, String> hash = new HashMap<String, String>();
    
    private ArrayAdapter<String> listAdapter;
    String lat[] = new String[1000];
	String lng[] = new String[1000];
	LatLng center = new LatLng(24.730870310199286, 121.76321268081665);
	
	private final LocationRequest REQUEST = LocationRequest.create()
		      .setInterval(5000)         // 5 seconds
		      .setFastestInterval(16)    // 16ms = 60fps
		      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
		
	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
		
	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	// Connection detector
	ConnectionDetector cd;
		
	public static String name;
	public static String email;
	public static int view_cnt = 0;
	public static int plan_cnt = 0;
	public static boolean view_direction = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //setProgressBarIndeterminateVisibility(true);
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
        
        spin = (Spinner)findViewById(R.id.spinner1);
        get_Table();
        
        add = (ImageButton)findViewById(R.id.imageButton1);
        add.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final EditText context = new EditText(AndroidMap.this);
				new AlertDialog.Builder(AndroidMap.this)
				.setTitle("請輸入...")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(context)
				.setPositiveButton("確定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
			            try{
			            	String result = DBConnector.executeQuery("create table " + context.getText().toString() + "(id integer auto_increment primary key,name varchar(20),site varchar(50),context varchar(100),lat decimal(9, 7), lng decimal(10,7)) CHARACTER SET utf8 COLLATE utf8_general_ci;", "http://140.125.45.113/contest/post_mysql/travel.php");
			            	index = context.getText().toString();
			            }catch(Exception e){
			            	Toast.makeText(getApplicationContext(), "建立失敗", 5).show();
			            }
			        }
				})
				.show();
			}
        });
        
        near = (ImageButton)findViewById(R.id.imageButton2);
        near.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LatLng now = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
				//select * from log where (121.690425 < longitude and longitude < 121.700425);
				String input = "select * from " + index + " where (" + Double.toString(center.longitude-0.005) + " < lng and lng < " + Double.toString(center.longitude+0.005) + " and lat > " + Double.toString(center.latitude-0.005) + " and lat < " + Double.toString(center.latitude+0.005) + ");";
				clear_Data();
				try{
					//String result = DBConnector.executeQuery(input, "http://140.125.45.113/contest/post_mysql/query_table.php");
					//JSONArray jsonArray = new JSONArray(result);
			        if(index.equals("景點")){
			        	getView(input);
			        	
			        }else if(index.equals("民宿")){
			        	getStay(input);
			        	
			        }else if(index.equals("餐廳")){
			        	getRestaurant(input);
			        	
			        }else if(index.equals("醫院")){
			        	getHospital(input);
			        	
			        }else{
			        	getExtra(input);
			        }
		            /*for(int i = 0; i < jsonArray.length(); i++) {
		                Data stop = new Data();
		            	JSONObject jsonData = jsonArray.getJSONObject(i);
		            	stop.id = jsonData.getString("id");
		            	stop.name_tw = jsonData.getString("name");
		            	stop.site_tw = jsonData.getString("site");
		            	stop.context = jsonData.getString("context");
		            	stop.lat = jsonData.getString("lat");
		            	stop.lng = jsonData.getString("lng");
		                map.addMarker(new MarkerOptions()
			            .position(new LatLng(Double.valueOf(stop.lat), Double.valueOf(stop.lng)))
			            .draggable(true)
			            .snippet("\n位置: " + stop.site_tw + "\n情形: " + stop.context)
			            .icon(BitmapDescriptorFactory.defaultMarker(210))
			            .title(stop.name_tw));
		                _data.add(stop);
		                hash.put(stop.name_tw, stop.id);
		                Toast.makeText(getApplicationContext(), jsonArray.getJSONObject(i).toString(), 5).show();
		            }*/
				}catch(Exception e){
					Toast.makeText(getApplicationContext(), "ERROR", 5).show();
				}
			}
        	
        });
        
        traffic = (ImageButton)findViewById(R.id.traffic);
        //traffic.setBackground(null);
        traffic.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent= new Intent();
				intent.setClass(getApplicationContext(), Traffic.class);
				startActivity(intent);
			}
        	
        });
        
        weather = (ImageButton)findViewById(R.id.weather);
        weather.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent= new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("web", 3);
				intent.putExtras(bundle);
				intent.setClass(getApplicationContext(), Web_view.class);
				startActivity(intent);
			}
        	
        });
        
        star = (ImageButton)findViewById(R.id.star);
        star.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent= new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("web", 4);
				intent.putExtras(bundle);
				intent.setClass(getApplicationContext(), Web_view.class);
				startActivity(intent);
			}
        	
        });
        
        plan = (ImageButton)findViewById(R.id.plan);
        plan.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), Schedule.class);
				startActivity(intent);
			}
        	
        });
        
        roadwork = (ImageButton)findViewById(R.id.roadwork);
        roadwork.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				show_Dialog("你確定要回報資訊?", 3);
			}  	
        });
        
        traffic_jam = (ImageButton)findViewById(R.id.traffic_jam);
        traffic_jam.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				show_Dialog("你確定要回報資訊?", 4);
			}  	
        });
        
        push = (ImageButton)findViewById(R.id.push);
        push.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				register();
			}
        	
        });
        
        facebook = (ImageButton)findViewById(R.id.facebook);
        facebook.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(AndroidMap.this, Facebook.class);
				startActivity(intent);
			}
        	
        });
        
        information = (ImageButton)findViewById(R.id.information);
        information.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(AndroidMap.this);
		        dialog.setTitle("About");
		        dialog.setMessage("Auther: Chun-Yen Lin\n" +
		        				  "Website: hoyuisun/TravelMap\n" +
		        				  "Version: v2.4\n" + 
		        				  "Update: 07/23/2013");
		        dialog.setPositiveButton("確定",
		                new DialogInterface.OnClickListener(){
		                    public void onClick(
		                            DialogInterface dialoginterface, int i){
		                            }
		                    }
		        		);
		        dialog.show();
			}
        	
        });
        
    }
	
	public void register(){
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(AndroidMap.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
		
		// Getting name, email from intent
		
		name = "Jin";
		email = "u9917010@yuntech.edu.tw";	
		
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));
		// Get GCM registration id
		regId = GCMRegistrar.getRegistrationId(this);
		
		if (regId.equals("")) {
			// Registration is not present, register now with GCM			
			GCMRegistrar.register(this, SENDER_ID);
		}else{ 
		
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.				
				Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// Register on our server
						// On server creates a new user
						ServerUtilities.register(context, name, email, regId);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}
	
	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());
			
			/**
			 * Take appropriate action on this message
			 * depending upon your app requirement
			 * For now i am just displaying it on the screen
			 * */
			
			// Showing received message
			if(newMessage == null)
				return;
			else if(newMessage.equals("From Server: Successfully added device!"))
				Toast.makeText(getApplicationContext(), newMessage, Toast.LENGTH_SHORT).show();
			else
				show_Dialog(newMessage, 2);
			//Toast.makeText(getApplicationContext(), newMessage, Toast.LENGTH_LONG).show();
			
			// Releasing wake lock
			WakeLocker.release();
		}
	};
	
    public void get_Table(){
    	_table.clear();
    	try{
        	String result = DBConnector.executeQuery("show tables;", "http://140.125.45.113/contest/post_mysql/query_table.php");
        	JSONArray jsonArray = new JSONArray(result);
        	for(int i = 0; i < jsonArray.length(); i++){
        		_table.add(jsonArray.getJSONObject(i).getString("Tables_in_location").toString());
        		//Toast.makeText(getApplicationContext(), jsonArray.getJSONObject(i).getString("Tables_in_location").toString(), 5).show();
        	}
        	//starting = true;
        }catch(Exception e){
        	Toast.makeText(getApplicationContext(), "建立失敗", 5).show();
        }
    	//下拉式選單
    	if(_table.size() > table_cnt){
	    	listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, _table);
	        spin.setAdapter(listAdapter);
	        spin.setSelection(_table.indexOf(index));
	        spin.setOnItemSelectedListener(new OnItemSelectedListener(){
	            @Override
	            public void onItemSelected(AdapterView<?> arg0, View arg1,final int position, long arg3) {
	            	select = 0;
	        		table_cnt = _table.size();
	            	new asyncTaskProgress().execute(_table.get(position));
	            	/*final ProgressDialog PDialog = ProgressDialog.show(AndroidMap.this, null, "正在載入...", true);
	                new Thread(){
		                public void run(){
			                try{
			                	Thread.sleep(3000);
			                }catch(Exception e){
			                	e.printStackTrace();
			                }finally{
			                	PDialog.dismiss();
			                }
			            }
	               }.start();
	               get_Data(_table.get(position));*/
	            }
	            @Override
	            public void onNothingSelected(AdapterView<?> arg0) {
	               // TODO Auto-generated method stub
	            }
	        });
    	}
    }
    
    public void get_Data(String position){
    	if(view_direction == true){
    		select = 1;
    		int des = _planPoints.size()-1;
			new asyncTaskProgress().execute(Double.toString(_planPoints.get(des).latitude), Double.toString(_planPoints.get(des).longitude));
    		return ;
    	}
    	clear_Data();
        String select = "SELECT * FROM " + position;
        index = position;
        if(index.equals("景點")){
        	getView(select);
        	
        }else if(index.equals("民宿")){
        	getStay(select);
        	
        }else if(index.equals("餐廳")){
        	getRestaurant(select);
        	
        }else if(index.equals("醫院")){
        	getHospital(select);
        	
        }else{
        	getExtra(select);
        }
    	starting = true;
        /*switch (_table.get(position)){
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
        }*/
    }
    
    //抓取民宿資料庫資料
    public void getStay(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data stay = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	stay.id = jsonData.getString("民宿編號");
				stay.name_tw = jsonData.getString("中文名稱");
                stay.name_en = jsonData.getString("英文名稱");
                stay.phone = jsonData.getString("聯絡電話");
                stay.fax = jsonData.getString("傳真");
                stay.website = jsonData.getString("網址");
                stay.email = jsonData.getString("EMAIL");
                stay.site_tw = jsonData.getString("中文地址");
                stay.site_en = jsonData.getString("英文地址");
                stay.lat = jsonData.getString("lat");
                stay.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(stay.lat), Double.valueOf(stay.lng)))
	            .snippet("連絡電話: " + stay.phone + "\n傳真: " + stay.fax + "\n網址: " + stay.website + "\nEMAIL: " + stay.email + "\n中文地址: " + stay.site_tw + "\n英文地址: " + stay.site_en)
	            .title(stay.name_tw));
                _data.add(stay);
                hash.put(stay.name_tw, stay.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
    //抓取醫院資料庫資料
    public void getHospital(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data hospital = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	hospital.id = jsonData.getString("序號");
            	hospital.name_tw = jsonData.getString("醫院名稱");
            	hospital.phone = jsonData.getString("電話");
            	hospital.site_tw = jsonData.getString("地址");
            	hospital.lat = jsonData.getString("lat");
            	hospital.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(hospital.lat), Double.valueOf(hospital.lng)))
	            .snippet("連絡電話: " + hospital.phone + "\n中文地址: " + hospital.site_tw)
	            .title(hospital.name_tw));
                _data.add(hospital);
                hash.put(hospital.name_tw, hospital.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
    //抓取景點資料庫資料
    public void getView(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data view = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	view.id = jsonData.getString("id");
                view.name_tw = jsonData.getString("地點");
                view.phone = jsonData.getString("電話");
                view.site_tw = jsonData.getString("地址");
                view.lat = jsonData.getString("lat");
                view.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(view.lat), Double.valueOf(view.lng)))
	            .snippet("連絡電話: " + view.phone + "\n中文地址: " + view.site_tw)
	            .icon(BitmapDescriptorFactory.defaultMarker(120))
	            .title(view.name_tw));
                _data.add(view);
                hash.put(view.name_tw, view.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
    //抓取餐廳資料庫資料
    public void getRestaurant(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data restaurant = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	restaurant.id = jsonData.getString("編號");
            	restaurant.name_tw = jsonData.getString("餐廳名稱");
            	restaurant.people = jsonData.getString("負責人");
            	restaurant.phone = jsonData.getString("電話");
            	restaurant.site_tw = jsonData.getString("地址");
            	restaurant.lat = jsonData.getString("lat");
            	restaurant.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(restaurant.lat), Double.valueOf(restaurant.lng)))
	            .snippet("連絡電話: " + restaurant.phone + "\n負責人: " + restaurant.people + "\n中文地址: " + restaurant.site_tw)
	            .icon(BitmapDescriptorFactory.defaultMarker(210))
	            .title(restaurant.name_tw));
                _data.add(restaurant);
                hash.put(restaurant.name_tw, restaurant.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
        }
    }
    
  //抓取其他地圖資料庫資料
    public void getExtra(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL 結果有多筆資料時使用JSONArray
                                                                  只有一筆資料時直接建立JSONObject物件
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data stop = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	stop.id = jsonData.getString("id");
            	stop.name_tw = jsonData.getString("name");
            	stop.site_tw = jsonData.getString("site");
            	stop.context = jsonData.getString("context");
            	stop.lat = jsonData.getString("lat");
            	stop.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(stop.lat), Double.valueOf(stop.lng)))
	            .snippet("\n位置: " + stop.site_tw + "\n情形: " + stop.context)
	            .icon(BitmapDescriptorFactory.defaultMarker(210))
	            .title(stop.name_tw));
                _data.add(stop);
                hash.put(stop.name_tw, stop.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "尚未有地圖資訊", 5).show();
        }
    }
    
    //規劃路徑，將點放進List中
    public List<LatLng> GetDirection(LatLng position){
		String result = null;
		if(view_direction == false)
			get_Data(index);
		try {
			LatLng now = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
			
	        String route= "http://map.google.com/maps/api/directions/json?origin=" +
	           		now.latitude + "," + now.longitude +"&destination=" + position.latitude + "," + position.longitude + "&language=en&sensor=true";
	        if(view_direction){
	        	String waypoints = "";
	        	for(int i = 0; i < _planPoints.size()-1; i++){
	        		if(i > 0)
	        			waypoints += "|";
	        		waypoints += Double.valueOf(_planPoints.get(i).latitude) + "," + Double.valueOf(_planPoints.get(i).longitude);
	        	}
	        	route = "http://map.google.com/maps/api/directions/json?origin=" +
		           		now.latitude + "," + now.longitude +"&destination=" + position.latitude + "," + position.longitude + 
		           		"&waypoints=" + waypoints + "&language=en&sensor=true";
	        	view_direction = false;
	        	get_Data(index);
	        }
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
	        	Toast.makeText(getApplicationContext(), "規劃路線失敗，請重新執行!", Toast.LENGTH_SHORT).show();
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
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
    	
		return super.onCreateOptionsMenu(menu);
	}

	public void Modify(String name){
    	Intent intent = new Intent();
    	Bundle bundle = new Bundle();
    	if (name.equals("道路施工") || name.equals("交通堵塞"))
        	return ;
		String data_id = hash.get(name).toString();
		if(index.equals("景點")){
			intent.setClass(AndroidMap.this, Modify_view.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        	
        }else if(index.equals("民宿")){
        	intent.setClass(AndroidMap.this, Modify_stay.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("name_en", _data.get(Integer.valueOf(data_id)-1).name_en);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("fax", _data.get(Integer.valueOf(data_id)-1).fax);
    		bundle.putString("website", _data.get(Integer.valueOf(data_id)-1).website);
    		bundle.putString("email", _data.get(Integer.valueOf(data_id)-1).email);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("site_en", _data.get(Integer.valueOf(data_id)-1).site_en);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        	
        }else if(index.equals("餐廳")){
        	intent.setClass(AndroidMap.this, Modify_restaurant.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("people", _data.get(Integer.valueOf(data_id)-1).people);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        	
        }else if(index.equals("醫院")){
        	intent.setClass(AndroidMap.this, Modify_hospital.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        	
        }else{
        	intent.setClass(AndroidMap.this, Modify_extra.class);
        	bundle.putString("select", index);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("context", _data.get(Integer.valueOf(data_id)-1).context);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        }
    	/*switch (index){
    	case 0:
    		intent.setClass(AndroidMap.this, Modify_view.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
    		break;
    	case 1:
    		intent.setClass(AndroidMap.this, Modify_stay.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("name_en", _data.get(Integer.valueOf(data_id)-1).name_en);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("fax", _data.get(Integer.valueOf(data_id)-1).fax);
    		bundle.putString("website", _data.get(Integer.valueOf(data_id)-1).website);
    		bundle.putString("email", _data.get(Integer.valueOf(data_id)-1).email);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("site_en", _data.get(Integer.valueOf(data_id)-1).site_en);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
    		break;
    	case 2:
    		intent.setClass(AndroidMap.this, Modify_restaurant.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("people", _data.get(Integer.valueOf(data_id)-1).people);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
    		break;
    	case 3:
    		intent.setClass(AndroidMap.this, Modify_hospital.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
    		break;
    	}*/
    	intent.putExtras(bundle);
        startActivity(intent);
    }
    
    class Data {
    	String id;
    	String name_tw;
    	String name_en;
    	String people;
    	String phone;
    	String fax;
    	String email;
    	String website;
    	String site_tw;
    	String site_en;
    	String context;
    	String lat;
    	String lng;
    }
    
    public class asyncTaskProgress extends AsyncTask<String, Void, Void>{
    	
    	String input[] = new String[2];
    	String message;
    	ProgressDialog PDialog;
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(select == 0){
				get_Data(input[0]);
				message = "\"" + index + "\"" + " 資訊已載入至地圖";
			} else{
				GetDirection(new LatLng(Double.valueOf(input[0]), Double.valueOf(input[1])));
				message = "路徑規劃執行已完成";
			}
			PDialog.dismiss();
			show_Dialog(message, select);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(select == 0)
				PDialog = PDialog.show(AndroidMap.this, null, "正在載入...");
			else
				PDialog = PDialog.show(AndroidMap.this, null, "規劃路徑中...");
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				for(int i = 0; i < params.length; i++)
					input[i] = params[i];
				if(select == 0)
					Thread.sleep(1000);
				else
					Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
    }
    
    private void clear_Data(){
    	map.clear();
    	_points.clear();
    	hash.clear();
    	_data.clear();
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
            int badge = 0;
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
            //if (marker.equals(mBrisbane)) {
            if(marker.getTitle().equals("道路施工")){
            	badge = R.drawable.roadwork_marker_icon;
            	
            }else if(marker.getTitle().equals("交通堵塞")){
            	badge = R.drawable.traffic_jam_markericon;
            	
            }else if(index.equals("景點")){
            	badge = R.drawable.landscape;
            	
            }else if(index.equals("民宿")){
            	badge = R.drawable.homestay;
            	
            }else if(index.equals("餐廳")){
            	badge = R.drawable.restaurant;
            	
            }else if(index.equals("醫院")){
            	badge = R.drawable.hospital;
            	
            }else{
            	
            }
            
            /*switch (index){
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
            }*/
            
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
            if (snippet != null) {
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
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.unregister(this);
			GCMRegistrar.onDestroy(this);
		}catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    		show_Dialog(null, 5);
    	}
		return super.onKeyDown(keyCode, event);
	}

	@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
        if(starting == true){
	        if(_table.size() > table_cnt)
	        	get_Table();
	        else
	        	get_Data(index);
        }
    }

	@Override
    public void onPause() {
    	super.onPause();
	    if (mLocationClient != null) {
	    	mLocationClient.disconnect();
	    }
    }
	
	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
			final Context context = this;
			GCMRegistrar.unregister(this);
			GCMRegistrar.onDestroy(this);
		}catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}
	
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
    	if(starting == true)
    		get_Table();
	}
    private void show_Dialog(String message, int flag){
    	Builder dialog = new AlertDialog.Builder(AndroidMap.this);
        
    	if(flag == 0){
    		dialog.setTitle("資料")
            .setMessage(message)
            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 1){
    		dialog.setTitle("規劃")
            .setMessage(message)
            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 2){
    		dialog.setTitle("即時訊息")
    		.setMessage(message)
    		.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 3){
    		dialog.setTitle("TAG")
    		.setMessage(message)
    		.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	map.addMarker(new MarkerOptions()
	 	               .position(new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude()))
	 	               .icon(BitmapDescriptorFactory.fromResource(R.drawable.roadwork_marker))
	 	               .snippet("請迴避道路或改道行駛")
	 	               .title("道路施工")
                	);
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 4){
    		dialog.setTitle("TAG")
    		.setMessage(message)
    		.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	map.addMarker(new MarkerOptions()
	 	               .position(new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude()))
	 	               .icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_jam_marker))
	 	               .snippet("請迴避道路或改道行駛")
	 	               .title("交通堵塞")
                	);
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 5){
    		dialog.setTitle("確定要離開嗎?")
    		.setMessage(message)
    		.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	finish();
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}
    	dialog.show();
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
        .setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent = new Intent();
            	Bundle bundle = new Bundle();
            	if(index.equals("景點")){
            		intent.setClass(AndroidMap.this, Add_view.class);
                	
                }else if(index.equals("民宿")){
                	intent.setClass(AndroidMap.this, Add_stay.class);
                	
                }else if(index.equals("餐廳")){
                	intent.setClass(AndroidMap.this, Add_restaurant.class);
                	
                }else if(index.equals("醫院")){
                	intent.setClass(AndroidMap.this, Add_hospital.class);
                	
                }else{
                	intent.setClass(AndroidMap.this, Add_extra.class);
                	bundle.putString("select", index);
                }
            	/*switch (index){
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
            	}*/
            	bundle.putDouble("lat", point.latitude);
                bundle.putDouble("lng", point.longitude);
            	//將Bundle物件assign給intent
                intent.putExtras(bundle);
                //切換Activity
                startActivity(intent);
            	//Toast.makeText(getApplicationContext(),result, Toast.LENGTH_SHORT).show();
            }
        })
        .setNegativeButton("否", new DialogInterface.OnClickListener() {
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
		
		String[] items = {"修改資訊", "加入行程", "路線導航"};
		builder.setTitle("要進行動作...");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dialog != null) dialog.dismiss();
				switch(which){
				case 0:
					Modify(marker.getTitle());
					break;
				case 1:
					_view.add(marker.getPosition());
					view_cnt++;
					break;
				case 2:
					select = 1;
					new asyncTaskProgress().execute(Double.toString(marker.getPosition().latitude), Double.toString(marker.getPosition().longitude));
					/*final ProgressDialog PDialog = ProgressDialog.show(AndroidMap.this, null, "路徑規劃中", true);
	                new Thread(){
		                public void run(){
			                try{
			                	sleep(4000);
			                }catch(Exception e){
			                	e.printStackTrace();
			                }
			                finally{
			                	PDialog.dismiss();
				            }
		                }
	               }.start();
					GetDirection(marker.getPosition());*/
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
