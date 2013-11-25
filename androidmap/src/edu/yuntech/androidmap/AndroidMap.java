package edu.yuntech.androidmap;

import static edu.yuntech.androidmap.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static edu.yuntech.androidmap.CommonUtilities.EXTRA_MESSAGE;
import static edu.yuntech.androidmap.CommonUtilities.SENDER_ID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
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

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class AndroidMap extends FragmentActivity implements OnMapClickListener, OnInfoWindowClickListener, OnMapLongClickListener, OnMarkerClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
	
    private GoogleMap map;
    private LocationClient mLocationClient;
    private Spinner spin;
    private ImageButton add;
    private ImageButton near;
    private ImageButton traffic;
    private ImageButton weather;
    private ImageButton star;
    private ImageButton plan;
    private ImageButton download;
    private ImageButton roadwork;
    private ImageButton traffic_jam;
    private ImageButton push;
    private ImageButton information;
    private boolean starting = false;
    
    private String index = "���I";
    private String regId;
    private int table_cnt = 0;
    private int select = 0;
    private List<LatLng> _points = new ArrayList<LatLng>();
    public static ArrayList<LatLng> _view = new ArrayList<LatLng>();
    public static HashMap<String, ArrayList<LatLng>> viewhash = new HashMap<String, ArrayList<LatLng>>();
    public static ArrayList<String> viewStrings= new ArrayList<String>();
    public static ArrayList<LatLng> _planPoints = new ArrayList<LatLng>();
    private List<Data> _data = new ArrayList<Data>();
    private List<Data> _infodata = new ArrayList<Data>();
    private List<String> _table = new ArrayList<String>();
    private HashMap<String, String> hash = new HashMap<String, String>();
    private HashMap<String, String> infohash = new HashMap<String, String>();
    
    private ArrayAdapter<String> listAdapter;
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
	ConnectionDetector wifi;	
	public static String name;
	public static String email;
	public static int view_cnt = 0;
	public static int plan_cnt = 0;
	public static boolean view_direction = false;
	
	private UiLifecycleHelper uiHelper;
	private ProfilePictureView profile;
	private GraphUser user;
	private ImageButton publish;
	private ImageButton picture;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(5);
        //setProgressBarIndeterminateVisibility(true);
        /*
         *���q���n!! 
         *�Ψӧ��mysql��T, �����L�k���
         */
        if(android.os.Build.VERSION.SDK_INT > 8){
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
        }
        Log.i("ver.", Integer.toString(android.os.Build.VERSION.SDK_INT));
        // start Facebook Login
        uiHelper = new UiLifecycleHelper(AndroidMap.this, callback);
	    uiHelper.onCreate(savedInstanceState);
	    LoginButton login = (LoginButton)findViewById(R.id.login);
	    /*
	     * ����P�ɭn����v��, �u��n�@��
	     * �Y�n�n�D�t�@�v��, �ݭ��s����s��session
	     * */
	    //login.setReadPermissions(Arrays.asList("user_likes", "user_status"));
	    login.setPublishPermissions(Arrays.asList("publish_actions"));
	    login.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                AndroidMap.this.user = user;
                updateUI();
            }
        });
	    profile = (ProfilePictureView) findViewById(R.id.profile);
	    publish = (ImageButton)findViewById(R.id.publish);
	    publish.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            publishFeedDialog();
	        }
	    });
	    picture = (ImageButton)findViewById(R.id.picture);
	    picture.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	performPublish();
	        }
	    });
        
        /*
         * android map�a�ϳ]�w�A�P�����I���]�w
         */
        setUpMapIfNeeded();
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13.0f));
        
        spin = (Spinner)findViewById(R.id.spinner1);
        
        wifi = new ConnectionDetector(getApplicationContext());
        if(!wifi.isConnectingToInternet())
        	read_Table();
        else
        	get_Table();
        
        add = (ImageButton)findViewById(R.id.imageButton1);
        add.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final EditText context = new EditText(AndroidMap.this);
				new AlertDialog.Builder(AndroidMap.this)
				.setTitle("�п�J...")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(context)
				.setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
			            try{
			            	String result = DBConnector.executeQuery("create table " + context.getText().toString() + "(id integer auto_increment primary key,name varchar(20),site varchar(50),context varchar(100),lat decimal(9, 7), lng decimal(10,7)) CHARACTER SET utf8 COLLATE utf8_general_ci;", "http://140.125.45.113/contest/post_mysql/travel.php");
			            	index = context.getText().toString();
			            }catch(Exception e){
			            	Toast.makeText(getApplicationContext(), "�إߦa�Ϫ���", Toast.LENGTH_SHORT).show();
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
				String input = "select * from " + index + " where (" + Double.toString(now.longitude-0.02) + " < lng and lng < " + Double.toString(now.longitude+0.02) + " and lat > " + Double.toString(now.latitude-0.02) + " and lat < " + Double.toString(now.latitude+0.02) + ");";
				clear_Data();
				try{
					//String result = DBConnector.executeQuery(input, "http://140.125.45.113/contest/post_mysql/query_table.php");
					//JSONArray jsonArray = new JSONArray(result);
			        getInfo();
					if(index.equals("���I")){
			        	getView(input);
			        	
			        }else if(index.equals("���J")){
			        	getStay(input);
			        	
			        }else if(index.equals("�\�U")){
			        	getRestaurant(input);
			        	
			        }else if(index.equals("��|")){
			        	getHospital(input);
			        	
			        }else if(index.equals("���Z")){
			        	getRest(input);
					
			        }else{
			        	getExtra(input);
			        }
				}catch(Exception e){
					Toast.makeText(AndroidMap.this, "����@�����B�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
				}
			}
        	
        });
        
        traffic = (ImageButton)findViewById(R.id.traffic);
        traffic.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				wifi = new ConnectionDetector(getApplicationContext());
            	if (!wifi.isConnectingToInternet()) {
        			// Internet Connection is not present
        			alert.showAlertDialog(AndroidMap.this,
        					"Internet Connection Error",
        					"Please connect to working Internet connection", false);
        			// stop executing code by return
        			return;
        		}
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
				wifi = new ConnectionDetector(getApplicationContext());
            	if (!wifi.isConnectingToInternet()) {
        			// Internet Connection is not present
        			alert.showAlertDialog(AndroidMap.this,
        					"Internet Connection Error",
        					"Please connect to working Internet connection", false);
        			// stop executing code by return
        			return;
        		}
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
				wifi = new ConnectionDetector(getApplicationContext());
            	if (!wifi.isConnectingToInternet()) {
        			// Internet Connection is not present
        			alert.showAlertDialog(AndroidMap.this,
        					"Internet Connection Error",
        					"Please connect to working Internet connection", false);
        			// stop executing code by return
        			return;
        		}
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
        
        download = (ImageButton)findViewById(R.id.download);
        download.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				select = 1;
				offline_Table();
				new asyncTaskProgress().execute();
			}
        	
        });
        
        roadwork = (ImageButton)findViewById(R.id.roadwork);
        roadwork.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				show_Dialog("�A�T�w�n�^����T?", 3);
			}  	
        });
        
        traffic_jam = (ImageButton)findViewById(R.id.traffic_jam);
        traffic_jam.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				show_Dialog("�A�T�w�n�^����T?", 4);
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
        
        information = (ImageButton)findViewById(R.id.information);
        information.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(AndroidMap.this);
		        dialog.setTitle("About");
		        dialog.setMessage("Team: Yuntech EOSLab\n" +
		        				  "Website: hoyuisun/TravelMap\n" +
		        				  "Version: v2.8\n" + 
		        				  "Update: 09/24/2013");
		        dialog.setPositiveButton("�T�w",
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
	public void offline_Download(String select, String name){
		File vSDCard = null;
    	FileWriter vFile = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            // �g�J�ɮ�
            vFile = new FileWriter( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap/" + name + ".json" );
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            vFile.write(result);
            vFile.close();
    	} catch(Exception e){
    		Toast.makeText(getApplicationContext(), name + "database download failed", Toast.LENGTH_SHORT).show();
    	}
    	
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
		
		name = "Gin";
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
			else{
				int start = newMessage.indexOf("[ĵ�ٰϰ�Ψƶ�]");
				int end = newMessage.indexOf("[�䭷�ʺA]");
				
				if(start == -1 || end == -1)
					show_Dialog(newMessage, 2);
				else
					show_Dialog(newMessage.substring(start+9, end), 2);
			}
			
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
        	}
        	//starting = true;
        }catch(Exception e){
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
        }
    	//�U�Ԧ����
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
    		select = 2;
    		int des = _planPoints.size()-1;
			new asyncTaskProgress().execute(Double.toString(_planPoints.get(des).latitude), Double.toString(_planPoints.get(des).longitude));
    		return ;
    	}
    	clear_Data();
        String select = "SELECT * FROM " + position;
        index = position;
        getInfo();
        if(index.equals("���I")){
        	getView(select);
        	
        }else if(index.equals("���J")){
        	getStay(select);
        	
        }else if(index.equals("�\�U")){
        	getRestaurant(select);
        	
        }else if(index.equals("��|")){
        	getHospital(select);
        	
        }else if(index.equals("���Z")){
        	getRest(select);
        	
        }else{
        	getExtra(select);
        }
    	starting = true;
    }
    
    //������J��Ʈw���
    public void getStay(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL ���G���h����Ʈɨϥ�JSONArray
                                                                  �u���@����Ʈɪ����إ�JSONObject����
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data stay = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	stay.id = jsonData.getString("���J�s��");
				stay.name_tw = jsonData.getString("����W��");
                stay.name_en = jsonData.getString("�^��W��");
                stay.phone = jsonData.getString("�p���q��");
                stay.fax = jsonData.getString("�ǯu");
                stay.website = jsonData.getString("���}");
                stay.email = jsonData.getString("EMAIL");
                stay.site_tw = jsonData.getString("����a�}");
                stay.site_en = jsonData.getString("�^��a�}");
                stay.lat = jsonData.getString("lat");
                stay.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(stay.lat), Double.valueOf(stay.lng)))
	            .snippet("�s���q��: " + stay.phone + "\n�ǯu: " + stay.fax + "\n���}: " + stay.website + "\nEMAIL: " + stay.email + "\n����a�}: " + stay.site_tw + "\n�^��a�}: " + stay.site_en)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
	            .title(stay.name_tw));
                _data.add(stay);
                hash.put(stay.name_tw, stay.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
        }
    }
    
    //����^����Ʈw���
    public void getInfo(){
    	try {
            String result = DBConnector.executeQuery("SELECT * FROM disaster", "http://140.125.45.113/contest/disaster.php");
            /*
                SQL ���G���h����Ʈɨϥ�JSONArray
                                                                  �u���@����Ʈɪ����إ�JSONObject����
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data info = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	info.id = jsonData.getString("id");
            	info.site_tw = jsonData.getString("site");
            	info.context = jsonData.getString("context");
            	info.lat = jsonData.getString("lat");
            	info.lng = jsonData.getString("lng");
            	BitmapDescriptor dw;
            	if(info.site_tw.equals("�D���I�u"))
            		dw = BitmapDescriptorFactory.fromResource(R.drawable.roadwork_marker);
            	else
            		dw = BitmapDescriptorFactory.fromResource(R.drawable.traffic_jam_marker);
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(info.lat), Double.valueOf(info.lng)))
	            .snippet(info.context)
	            .icon(dw)
	            .title(info.site_tw));
                _infodata.add(info);
                infohash.put(info.name_tw, info.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        }
    }
    
    public void getRest(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL ���G���h����Ʈɨϥ�JSONArray
                                                                  �u���@����Ʈɪ����إ�JSONObject����
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data rest = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	rest.id = jsonData.getString("id");
            	rest.name_tw = jsonData.getString("���Z�W��");
            	rest.context = jsonData.getString("���W��");
            	rest.site_tw = jsonData.getString("�a�I");
            	rest.phone = jsonData.getString("�p���q��");
            	rest.lat = jsonData.getString("lat");
            	rest.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(rest.lat), Double.valueOf(rest.lng)))
	            .snippet("�a�I: " + rest.site_tw + "\n���W��: " + rest.context + "\n�q��: " + rest.phone)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
	            .title(rest.name_tw));
                _data.add(rest);
                hash.put(rest.name_tw, rest.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
        }
    }
    
    //�����|��Ʈw���
    public void getHospital(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL ���G���h����Ʈɨϥ�JSONArray
                                                                  �u���@����Ʈɪ����إ�JSONObject����
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data hospital = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	hospital.id = jsonData.getString("�Ǹ�");
            	hospital.name_tw = jsonData.getString("��|�W��");
            	hospital.phone = jsonData.getString("�q��");
            	hospital.site_tw = jsonData.getString("�a�}");
            	hospital.lat = jsonData.getString("lat");
            	hospital.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(hospital.lat), Double.valueOf(hospital.lng)))
	            .snippet("�s���q��: " + hospital.phone + "\n����a�}: " + hospital.site_tw)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
	            .title(hospital.name_tw));
                _data.add(hospital);
                hash.put(hospital.name_tw, hospital.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
        }
    }
    public void read_Table(){
    	_table.clear();
    	File vSDCard = null;
    	FileReader vRead = null;
    	String result = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            // �g�J�ɮ�
            vRead = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap/table.json" );
    	} catch(Exception e){}
    	try{
    		char[] buf = new char[10000];
			int num = vRead.read(buf);
		    result = new String(buf, 0, num);
	    	JSONArray jsonArray = new JSONArray(result);
	    	for(int i = 0; i < jsonArray.length(); i++){
	    		_table.add(jsonArray.getJSONObject(i).getString("Tables_in_location").toString());
	    	}
	    	vRead.close();
    	//starting = true;
	    }catch(Exception e){
	    	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
	    }
    	//�U�Ԧ����
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
	            }
	            @Override
	            public void onNothingSelected(AdapterView<?> arg0) {
	               // TODO Auto-generated method stub
	            }
	        });
    	}
    }
    public void offline_Table(){
    	_table.clear();
    	File vSDCard = null;
    	FileWriter vFile = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            // �g�J�ɮ�
            vFile = new FileWriter( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap/table.json" );
    	} catch(Exception e){}
    	try{
        	String result = DBConnector.executeQuery("show tables;", "http://140.125.45.113/contest/post_mysql/query_table.php");
        	vFile.write(result);
        	JSONArray jsonArray = new JSONArray(result);
        	for(int i = 0; i < jsonArray.length(); i++){
        		_table.add(jsonArray.getJSONObject(i).getString("Tables_in_location").toString());
        	}
        	vFile.close();
        	//starting = true;
        }catch(Exception e){
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
        }
    }
    public void offline_View(String path){
    	File vSDCard = null;
    	FileReader vRead = null;
    	String result = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            vRead = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() + path );
    	} catch(Exception e){}
    	try{
			char[] buf = new char[1000000];
			int num = vRead.read(buf);
		    result = new String(buf, 0, num);
    		vRead.close(); 
    		JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data view = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	view.id = jsonData.getString("id");
                view.name_tw = jsonData.getString("�a�I");
                view.phone = jsonData.getString("�q��");
                view.site_tw = jsonData.getString("�a�}");
                view.lat = jsonData.getString("lat");
                view.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(view.lat), Double.valueOf(view.lng)))
	            .snippet("�s���q��: " + view.phone + "\n����a�}: " + view.site_tw)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker))
	            .title(view.name_tw));
                _data.add(view);
                hash.put(view.name_tw, view.id);
            }
		}catch(Exception e){
			Log.i("jodata", "err");
		}
    }
    public void offline_Hospital(String path){
    	File vSDCard = null;
    	FileReader vRead = null;
    	String result = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            vRead = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() + path );
    	} catch(Exception e){}
    	try{
			char[] buf = new char[1000000];
			int num = vRead.read(buf);
		    result = new String(buf, 0, num);
    		vRead.close(); 
    		JSONArray jsonArray = new JSONArray(result);
    		for(int i = 0; i < jsonArray.length(); i++) {
                Data hospital = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	hospital.id = jsonData.getString("�Ǹ�");
            	hospital.name_tw = jsonData.getString("��|�W��");
            	hospital.phone = jsonData.getString("�q��");
            	hospital.site_tw = jsonData.getString("�a�}");
            	hospital.lat = jsonData.getString("lat");
            	hospital.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(hospital.lat), Double.valueOf(hospital.lng)))
	            .snippet("�s���q��: " + hospital.phone + "\n����a�}: " + hospital.site_tw)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
	            .title(hospital.name_tw));
                _data.add(hospital);
                hash.put(hospital.name_tw, hospital.id);
            }
		}catch(Exception e){}
    }
    
    public void offline_Stay(String path){
    	File vSDCard = null;
    	FileReader vRead = null;
    	String result = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            vRead = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() + path );
    	} catch(Exception e){}
    	try{
    		char[] buf = new char[1000000];
			int num = vRead.read(buf);
		    result = new String(buf, 0, num);
    		vRead.close();  
    		JSONArray jsonArray = new JSONArray(result);
    		for(int i = 0; i < jsonArray.length(); i++) {
                Data stay = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	stay.id = jsonData.getString("���J�s��");
    			stay.name_tw = jsonData.getString("����W��");
                stay.name_en = jsonData.getString("�^��W��");
                stay.phone = jsonData.getString("�p���q��");
                stay.fax = jsonData.getString("�ǯu");
                stay.website = jsonData.getString("���}");
                stay.email = jsonData.getString("EMAIL");
                stay.site_tw = jsonData.getString("����a�}");
                stay.site_en = jsonData.getString("�^��a�}");
                stay.lat = jsonData.getString("lat");
                stay.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.valueOf(stay.lat), Double.valueOf(stay.lng)))
                .snippet("�s���q��: " + stay.phone + "\n�ǯu: " + stay.fax + "\n���}: " + stay.website + "\nEMAIL: " + stay.email + "\n����a�}: " + stay.site_tw + "\n�^��a�}: " + stay.site_en)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
                .title(stay.name_tw));
                _data.add(stay);
                hash.put(stay.name_tw, stay.id);
            }
		}catch(Exception e){}
    }
    
    public void offline_Restaurant(String path){
    	File vSDCard = null;
    	FileReader vRead = null;
    	String result = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            vRead = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() + path );
    	} catch(Exception e){}
    	try{
			char[] buf = new char[1000000];
			int num = vRead.read(buf);
		    result = new String(buf, 0, num);
    		vRead.close(); 
    		JSONArray jsonArray = new JSONArray(result);
    		for(int i = 0; i < jsonArray.length(); i++) {
                Data restaurant = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	restaurant.id = jsonData.getString("�s��");
            	restaurant.name_tw = jsonData.getString("�\�U�W��");
            	restaurant.people = jsonData.getString("�t�d�H");
            	restaurant.phone = jsonData.getString("�q��");
            	restaurant.site_tw = jsonData.getString("�a�}");
            	restaurant.lat = jsonData.getString("lat");
            	restaurant.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(restaurant.lat), Double.valueOf(restaurant.lng)))
	            .snippet("�s���q��: " + restaurant.phone + "\n�t�d�H: " + restaurant.people + "\n����a�}: " + restaurant.site_tw)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker))
	            .title(restaurant.name_tw));
                _data.add(restaurant);
                hash.put(restaurant.name_tw, restaurant.id);
            }
		}catch(Exception e){}
    }
    
    public void offline_Restroom(String path){
    	File vSDCard = null;
    	FileReader vRead = null;
    	String result = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            vRead = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() + path );
    	} catch(Exception e){}
    	try{
			char[] buf = new char[1000000];
			int num = vRead.read(buf);
		    result = new String(buf, 0, num);
    		vRead.close(); 
    		JSONArray jsonArray = new JSONArray(result);
    		for(int i = 0; i < jsonArray.length(); i++) {
    			Data rest = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	rest.id = jsonData.getString("id");
            	rest.name_tw = jsonData.getString("���Z�W��");
            	rest.context = jsonData.getString("���W��");
            	rest.site_tw = jsonData.getString("�a�I");
            	rest.phone = jsonData.getString("�p���q��");
            	rest.lat = jsonData.getString("lat");
            	rest.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(rest.lat), Double.valueOf(rest.lng)))
	            .snippet("�a�I: " + rest.site_tw + "\n���W��: " + rest.context + "\n�q��: " + rest.phone)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
	            .title(rest.name_tw));
                _data.add(rest);
                hash.put(rest.name_tw, rest.id);
            }
		}catch(Exception e){}
    }
    
    public void offline_Extra(String path){
    	File vSDCard = null;
    	FileReader vRead = null;
    	String result = null;
    	try{
    		// ���o SD Card ��m
            vSDCard = Environment.getExternalStorageDirectory();
            // �P�_�ؿ��O�_�s�b
            File vPath = new File( vSDCard.getParent() + "/" + vSDCard.getName() + "/TravelMap");
            if(!vPath.exists())
            	vPath.mkdirs();
            vRead = new FileReader( vSDCard.getParent() + "/" + vSDCard.getName() + path );
    	} catch(Exception e){}
    	try{
			char[] buf = new char[1000000];
			int num = vRead.read(buf);
		    result = new String(buf, 0, num);
    		vRead.close(); 
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
                .snippet("\n��m: " + stop.site_tw + "\n����: " + stop.context)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
                .title(stop.name_tw));
                _data.add(stop);
                hash.put(stop.name_tw, stop.id);
            }
		}catch(Exception e){}
    }
    
    public void read_Data(String position){
    	clear_Data();
    	index = position;
    	String path = "/TravelMap/";
        if(position.equals("���I")){
            path += "���I.json";
            offline_View(path);
        }
        else if(position.equals("���J")){
            path += "���J.json";
            offline_Stay(path);
        }
        else if(position.equals("�\�U")){
            path += "�\�U.json";
            offline_Restaurant(path);
        }
        else if(position.equals("��|")){
            path += "��|.json";
            offline_Hospital(path);
        }
        else if(position.equals("���Z")){
        	path += "���Z.json";
        	offline_Restroom(path);
        }
        else{
            path += position + ".json";
            offline_Extra(path);
        }
        
    }
    //������I��Ʈw���
    public void getView(String select){
    	try {
    		
    		String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL ���G���h����Ʈɨϥ�JSONArray
                                                                  �u���@����Ʈɪ����إ�JSONObject����
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data view = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	view.id = jsonData.getString("id");
                view.name_tw = jsonData.getString("�a�I");
                view.phone = jsonData.getString("�q��");
                view.site_tw = jsonData.getString("�a�}");
                view.lat = jsonData.getString("lat");
                view.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(view.lat), Double.valueOf(view.lng)))
	            .snippet("�s���q��: " + view.phone + "\n����a�}: " + view.site_tw)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker))
	            .title(view.name_tw));
                _data.add(view);
                hash.put(view.name_tw, view.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
        }
    }
    
    //����\�U��Ʈw���
    public void getRestaurant(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL ���G���h����Ʈɨϥ�JSONArray
                                                                  �u���@����Ʈɪ����إ�JSONObject����
                JSONObject jsonData = new JSONObject(result);
            */
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++) {
                Data restaurant = new Data();
            	JSONObject jsonData = jsonArray.getJSONObject(i);
            	restaurant.id = jsonData.getString("�s��");
            	restaurant.name_tw = jsonData.getString("�\�U�W��");
            	restaurant.people = jsonData.getString("�t�d�H");
            	restaurant.phone = jsonData.getString("�q��");
            	restaurant.site_tw = jsonData.getString("�a�}");
            	restaurant.lat = jsonData.getString("lat");
            	restaurant.lng = jsonData.getString("lng");
                map.addMarker(new MarkerOptions()
	            .position(new LatLng(Double.valueOf(restaurant.lat), Double.valueOf(restaurant.lng)))
	            .snippet("�s���q��: " + restaurant.phone + "\n�t�d�H: " + restaurant.people + "\n����a�}: " + restaurant.site_tw)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker))
	            .title(restaurant.name_tw));
                _data.add(restaurant);
                hash.put(restaurant.name_tw, restaurant.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", Toast.LENGTH_SHORT).show();
        }
    }
    
  //�����L�a�ϸ�Ʈw���
    public void getExtra(String select){
    	try {
            String result = DBConnector.executeQuery(select, "http://140.125.45.113/contest/travel.php");
            /*
                SQL ���G���h����Ʈɨϥ�JSONArray
                                                                  �u���@����Ʈɪ����إ�JSONObject����
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
	            .snippet("\n��m: " + stop.site_tw + "\n����: " + stop.context)
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
	            .title(stop.name_tw));
                _data.add(stop);
                hash.put(stop.name_tw, stop.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        }
    }
    
    //�W�����|�A�N�I��iList��
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
	        	Toast.makeText(getApplicationContext(), "�W�����u���ѡA�Э��s����!", Toast.LENGTH_SHORT).show();
	    }
		return _points;
	}
    
    /*
     * ��u�ѽX�t��k�A�ѪRJSON����points
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
    
    //��PolyLine�e�a�ϡA��ܸ��|
    public void Route(){
    	Polyline line;
    	for(int i = 1; i < _points.size(); i++){
    		line = map.addPolyline(new PolylineOptions()
            .add(_points.get(i-1), _points.get(i))
            .width(8)
            .color(Color.RED));
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
    	if (name.equals("�D���I�u") || name.equals("��q����"))
        	return ;
		String data_id = hash.get(name).toString();
		if(index.equals("���I")){
			intent.setClass(AndroidMap.this, Modify_view.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        	
        }else if(index.equals("���J")){
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
        	
        }else if(index.equals("�\�U")){
        	intent.setClass(AndroidMap.this, Modify_restaurant.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("people", _data.get(Integer.valueOf(data_id)-1).people);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        	
        }else if(index.equals("��|")){
        	intent.setClass(AndroidMap.this, Modify_hospital.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("phone", _data.get(Integer.valueOf(data_id)-1).phone);
    		bundle.putString("site_tw", _data.get(Integer.valueOf(data_id)-1).site_tw);
    		bundle.putString("lat", _data.get(Integer.valueOf(data_id)-1).lat);
    		bundle.putString("lng", _data.get(Integer.valueOf(data_id)-1).lng);
        }else if(index.equals("���Z")){
        	intent.setClass(AndroidMap.this, Modify_restroom.class);
    		bundle.putString("id", data_id);
    		bundle.putString("name_tw", _data.get(Integer.valueOf(data_id)-1).name_tw);
    		bundle.putString("context", _data.get(Integer.valueOf(data_id)-1).context);
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
				wifi = new ConnectionDetector(getApplicationContext());
		        if(!wifi.isConnectingToInternet())
		        	read_Data(input[0]);
		        else
		        	get_Data(input[0]);
				message = "\"" + index + "\"" + " ��T�w���J�ܦa��";
			} else if(select == 1){
				for(int i = 0; i < _table.toArray().length; i++){
					//index = 
					String select = "SELECT * FROM " + _table.get(i);
					//String select = "SELECT * FROM " + position;
					/*index = _table.get(i);
					if(index.equals("���I"))
						select += view;
					else if(index.equals("���J"))
					else if(index.equals("��|"))
					else if("�\�U")
						select += "restaurant"
					else
						select += index;*/
					offline_Download(select, _table.get(i));
				}
				PDialog.dismiss();
				show_Dialog("���u��ƤU������", 0);
				return ;
			} else{
				GetDirection(new LatLng(Double.valueOf(input[0]), Double.valueOf(input[1])));
				message = "���|�W������w����";
			}
			PDialog.dismiss();
			show_Dialog(message, select);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(select == 0)
				PDialog = PDialog.show(AndroidMap.this, null, "���b���J��...");
			else if(select == 1)
				PDialog = PDialog.show(AndroidMap.this, null, "�U����Ƥ�...");
			else
				PDialog = PDialog.show(AndroidMap.this, null, "�W�����|��...");
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				for(int i = 0; i < params.length; i++)
					input[i] = params[i];
				if(select == 0 || select == 1)
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
     * �Ȼs��marker����
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
            if(marker.getTitle().equals("�D���I�u")){
            	badge = R.drawable.roadwork_marker_icon;
            	
            }else if(marker.getTitle().equals("��q����")){
            	badge = R.drawable.traffic_jam_markericon;
            	
            }else if(index.equals("���I")){
            	badge = R.drawable.landscape;
            	
            }else if(index.equals("���J")){
            	badge = R.drawable.homestay;
            	
            }else if(index.equals("�\�U")){
            	badge = R.drawable.restaurant;
            	
            }else if(index.equals("��|")){
            	badge = R.drawable.hospital;
            	
            }else if(index.equals("���Z")){
            	badge = R.drawable.restroom;
            	
            }else{
            	
            }
            
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
            //marker���D�]�w
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
            //marker���e�]�w
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
        	wifi = new ConnectionDetector(getApplicationContext());
	        if(!wifi.isConnectingToInternet()){
	        	read_Table();
	        	read_Data(index);
	        }else{
	        	if(_table.size() > table_cnt)
		        	get_Table();
		        else
		        	get_Data(index);
	        }
        }
        Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    uiHelper.onResume();
    }

	@Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      uiHelper.onActivityResult(requestCode, resultCode, data);
	      if (resultCode == RESULT_OK){
	            // ���o�ɮת� Uri
	    	  Uri image_uri = data.getData();
	    	  if(image_uri != null){
		            Bitmap image;
					try {
						image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
						//image = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon);
						Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
			                @Override
			                public void onCompleted(Response response) {
			                }
			            });
			            request.executeAsync();
			            Toast.makeText(getApplicationContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
					}catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
					}
	  		  	}
	      }
	}

	@Override
    public void onPause() {
    	super.onPause();
	    if (mLocationClient != null) {
	    	mLocationClient.disconnect();
	    }
	    uiHelper.onPause();
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
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
		uiHelper.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
    	if(starting == true){
    		wifi = new ConnectionDetector(getApplicationContext());
	        if(!wifi.isConnectingToInternet())
	        	read_Table();
	        else
	        	get_Table();
    	}
	}
    private void show_Dialog(String message, int flag){
    	Builder dialog = new AlertDialog.Builder(AndroidMap.this);
        
    	if(flag == 0){
    		dialog.setTitle("���")
            .setMessage(message)
            .setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 1){
    		dialog.setTitle("�W��")
            .setMessage(message)
            .setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 2){
    		dialog.setTitle("�Y�ɰT��")
    		.setMessage(message)
    		.setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 3){
    		dialog.setTitle("��T�^��")
    		.setMessage(message)
    		.setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
    				LatLng now = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
	            	try{
	            		String result = DBConnector.executeQuery("INSERT INTO `disaster` (`site`,`context`,`lat`,`lng`) VALUES ('�D���I�u','�аj�׹D���Χ�D��p','" + Double.toString(now.latitude) + "','" + Double.toString(now.longitude) +"');", "http://140.125.45.113/contest/disaster.php");
	            		Toast.makeText(getApplicationContext(), "�^����T���\", Toast.LENGTH_SHORT).show();
	            	}catch(Exception e){
	            		Toast.makeText(getApplicationContext(), "�^����T����", Toast.LENGTH_SHORT).show();
	            	}
	            	get_Data(index);
                }
            })
            .setNegativeButton("����", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 4){
    		dialog.setTitle("��T�^��")
    		.setMessage(message)
    		.setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	LatLng now = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
                	try{
	            		String result = DBConnector.executeQuery("INSERT INTO `disaster` (`site`,`context`,`lat`,`lng`) VALUES ('��q����','�аj�׹D���Χ�D��p','" + Double.toString(now.latitude) + "','" + Double.toString(now.longitude) +"');", "http://140.125.45.113/contest/disaster.php");
	            		Toast.makeText(getApplicationContext(), "�^����T���\", Toast.LENGTH_SHORT).show();
	            	}catch(Exception e){
	            		Toast.makeText(getApplicationContext(), "�^����T����", Toast.LENGTH_SHORT).show();
	            	}
                	get_Data(index);
                }
            })
            .setNegativeButton("����", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}else if(flag == 5){
    		dialog.setTitle("�T�w�n���}��?")
    		.setMessage(message)
    		.setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	finish();
                }
            })
            .setNegativeButton("����", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
    	}
    	dialog.show();
    }
    
    private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", "Open Travel Map");

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(AndroidMap.this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(getApplicationContext(),
	                            //"Posted story, id: "+postId,
	                        	"Publish Successful",
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(getApplicationContext().getApplicationContext(), 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(getApplicationContext(), 
	                        "Publish cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
    
    private void updateUI() {
        Session session = Session.getActiveSession();
        boolean enableButtons = (session != null && session.isOpened());

        publish.setEnabled(enableButtons);
        picture.setEnabled(enableButtons);

        if (enableButtons && user != null) {
            profile.setProfileId(user.getId());
            Toast.makeText(getApplicationContext(), "Login as: " + user.getName(), Toast.LENGTH_SHORT).show();
        } else {
            profile.setProfileId(null);
        }
    }
    
    private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	}
	
	private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    private void performPublish() {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
                // We can do the action right away.
            	postPhoto();
            } else {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, Arrays.asList("publish_actions")));
            }
        }
    }
    
    private void postPhoto() {
    	Intent intent = new Intent(Intent.ACTION_PICK);
        // �L�o�ɮ׮榡
        intent.setType("image/*");
        // �إ� "�ɮ׿�ܾ�" �� Intent  (�ĤG�ӰѼ�: ��ܾ������D)
        //Intent destIntent = Intent.createChooser(intent, "����ɮ�");
        // �������ɮ׿�ܾ� (�����B�z���G, �|Ĳ�o onActivityResult �ƥ�)
        startActivityForResult(intent, 1);
    }
    
	private void setUpMapIfNeeded() {
        if (map == null) {
        	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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
    
    //�]�w�a��Listener
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
     * �����a�ϫᤧ�ʧ@���O�_�n�s�٦a�ϸ�T
     */
    @Override
    public void onMapLongClick(final LatLng point) {
        
    	wifi = new ConnectionDetector(getApplicationContext());
    	if (!wifi.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(AndroidMap.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
    	
    	new AlertDialog.Builder(AndroidMap.this)
        .setTitle("�n�i��ʧ@...")
        .setMessage("�n�s�W�a�ϸ�T?")
        .setPositiveButton("�O", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent = new Intent();
            	Bundle bundle = new Bundle();
            	if(index.equals("���I")){
            		intent.setClass(AndroidMap.this, Add_view.class);
                	
                }else if(index.equals("���J")){
                	intent.setClass(AndroidMap.this, Add_stay.class);
                	
                }else if(index.equals("�\�U")){
                	intent.setClass(AndroidMap.this, Add_restaurant.class);
                	
                }else if(index.equals("��|")){
                	intent.setClass(AndroidMap.this, Add_hospital.class);
                	
                }else if(index.equals("���Z")){
                	intent.setClass(AndroidMap.this, Add_restroom.class);
                	
                }else{
                	intent.setClass(AndroidMap.this, Add_extra.class);
                	bundle.putString("select", index);
                }
            	bundle.putDouble("lat", point.latitude);
                bundle.putDouble("lng", point.longitude);
            	//�NBundle����assign��intent
                intent.putExtras(bundle);
                //����Activity
                startActivity(intent);
            }
        })
        .setNegativeButton("�_", new DialogInterface.OnClickListener() {
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
		 *�a�ϤWmarker���ʰʵe 
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
		wifi = new ConnectionDetector(getApplicationContext());
    	if (!wifi.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(AndroidMap.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
		
		Dialog(marker);
    }
	
	/*/
	 * �I��marker�W�T����X�{����ܮ�
	 * 1.�ק�a�ϸ�T
	 * 2.���u�W��
	 * 3.
	 */
	private void Dialog(final Marker marker){
		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		String[] items = {"�ק��T", "�[�J��{", "���u�ɯ�", "�c�N�^��"};
		builder.setTitle("�n�i��ʧ@...");
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
					Toast.makeText(getApplicationContext(), "�ثe�w�[�J" + view_cnt + "�Ӧa�I", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					select = 2;
					new asyncTaskProgress().execute(Double.toString(marker.getPosition().latitude), Double.toString(marker.getPosition().longitude));
					break;
				case 3:
					Intent emailIntent = new Intent(Intent.ACTION_SEND);
			        emailIntent.setData(Uri.parse("mailto:"));
			        String [] to = new String []{"u9917010@yuntech.edu.tw"};
			        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
			        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "�c�N�^��");
			        emailIntent.putExtra(Intent.EXTRA_TEXT, "���O: " + marker.getTitle() + "\n���e: " + marker.getSnippet());
			        emailIntent.setType("message/rfc822");
			        startActivity(Intent.createChooser(emailIntent, "Email"));
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
