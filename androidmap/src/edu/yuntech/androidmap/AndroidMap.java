package edu.yuntech.androidmap;

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
import android.app.ProgressDialog;
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
    
    private String index = "���I";
    private int table_cnt = 0;
    private List<LatLng> _points = new ArrayList<LatLng>();
    private List<Data> _data = new ArrayList<Data>();
    private List<String> _table = new ArrayList<String>();
    private HashMap<String, String> hash = new HashMap<String, String>();
    
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
         *���q���n!! 
         *�Ψӧ��mysql��T, �����L�k���
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
         * android map�a�ϳ]�w�A�P�����I���]�w
         */
        setUpMapIfNeeded();
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13.0f));
        
        spin = (Spinner)findViewById(R.id.spinner1);
        
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
			            	String result = DBConnector.executeQuery("create table " + context.getText().toString() + "(id integer auto_increment primary key,name char(20),site char(50),context char(100),lat decimal(9, 7), lng decimal(10,7));", "http://140.125.45.113/contest/post_mysql/travel.php");
			            }catch(Exception e){
			            	Toast.makeText(getApplicationContext(), "�إߥ���", 5).show();
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
			        if(index.equals("���I")){
			        	getView(input);
			        	
			        }else if(index.equals("���J")){
			        	getStay(input);
			        	
			        }else if(index.equals("�\�U")){
			        	getRestaurant(input);
			        	
			        }else if(index.equals("��|")){
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
			            .snippet("\n��m: " + stop.site_tw + "\n����: " + stop.context)
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
    }
    
    public void get_Table(){
    	_table.clear();
    	try{
        	String result = DBConnector.executeQuery("show tables;", "http://140.125.45.113/contest/post_mysql/query_table.php");
        	JSONArray jsonArray = new JSONArray(result);
        	for(int i = 0; i < jsonArray.length(); i++){
        		_table.add(jsonArray.getJSONObject(i).getString("Tables_in_location").toString());
        		//Toast.makeText(getApplicationContext(), jsonArray.getJSONObject(i).getString("Tables_in_location").toString(), 5).show();
        	}
        }catch(Exception e){
        	Toast.makeText(getApplicationContext(), "�إߥ���", 5).show();
        }
    	//�U�Ԧ����
    	if(_table.size() > table_cnt){
    		table_cnt = _table.size();
	    	listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, _table);
	        spin.setAdapter(listAdapter);
	        spin.setOnItemSelectedListener(new OnItemSelectedListener(){
	            @Override
	            public void onItemSelected(AdapterView<?> arg0, View arg1,final int position, long arg3) {
	            	
	            	final ProgressDialog PDialog = ProgressDialog.show(AndroidMap.this, "abc", "Loading", true);

	                new Thread(){
	                public void run(){
	                try{
	                	//get_Data(_table.get(position));
	                	sleep(2000);
	                }
	                catch(Exception e){
	                e.printStackTrace();
	                }
	                finally{
	               PDialog.dismiss();
	                 }
	                }
	               }.start();
	               get_Data(_table.get(position));
	            }
	            @Override
	            public void onNothingSelected(AdapterView<?> arg0) {
	               // TODO Auto-generated method stub
	            }
	        });
    	}
    }
    
    public void get_Data(String position){
    	clear_Data();
        String select = "SELECT * FROM " + position;
        index = position;
        if(index.equals("���I")){
        	getView(select);
        	
        }else if(index.equals("���J")){
        	getStay(select);
        	
        }else if(index.equals("�\�U")){
        	getRestaurant(select);
        	
        }else if(index.equals("��|")){
        	getHospital(select);
        	
        }else{
        	getExtra(select);
        }
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
	            .draggable(true)
	            .snippet("�s���q��: " + stay.phone + "\n�ǯu: " + stay.fax + "\n���}: " + stay.website + "\nEMAIL: " + stay.email + "\n����a�}: " + stay.site_tw + "\n�^��a�}: " + stay.site_en)
	            .title(stay.name_tw));
                _data.add(stay);
                hash.put(stay.name_tw, stay.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
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
	            .draggable(true)
	            .snippet("�s���q��: " + hospital.phone + "\n����a�}: " + hospital.site_tw)
	            .title(hospital.name_tw));
                _data.add(hospital);
                hash.put(hospital.name_tw, hospital.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "Failed", 5).show();
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
	            .draggable(true)
	            .snippet("�s���q��: " + restaurant.phone + "\n�t�d�H: " + restaurant.people + "\n����a�}: " + restaurant.site_tw)
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
	            .draggable(true)
	            .snippet("\n��m: " + stop.site_tw + "\n����: " + stop.context)
	            .icon(BitmapDescriptorFactory.defaultMarker(210))
	            .title(stop.name_tw));
                _data.add(stop);
                hash.put(stop.name_tw, stop.id);
            }
        } catch(Exception e) {
            // Log.e("log_tag", e.toString());
        	Toast.makeText(AndroidMap.this, "�|�����a�ϸ�T", 5).show();
        }
    }
    
    //�W�����|�A�N�I��iList��
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
	        	Toast.makeText(AndroidMap.this, "�W�����u���ѡA�Э��s����!", 5).show();
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
    
    private void clear_Data(){
    	map.clear();
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
            int badge=R.drawable.badge_qld;
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
            //if (marker.equals(mBrisbane)) {
            
            if(index.equals("���I")){
            	badge = R.drawable.landscape;
            	
            }else if(index.equals("���J")){
            	
            	
            }else if(index.equals("�\�U")){
            	
            	
            }else if(index.equals("��|")){
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
        get_Table();
        get_Data(index);
    }

	@Override
    public void onPause() {
    	super.onPause();
	    if (mLocationClient != null) {
	    	mLocationClient.disconnect();
	    }
    }
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
    	get_Table();
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
            	//�NBundle����assign��intent
                intent.putExtras(bundle);
                //����Activity
                startActivity(intent);
            	//Toast.makeText(getApplicationContext(),result, Toast.LENGTH_SHORT).show();
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
		
		String[] items = {"�ק��T", "���u�ɯ�"};
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
					final ProgressDialog PDialog = ProgressDialog.show(AndroidMap.this, "abc", "Loading", true);
	                new Thread(){
		                public void run(){
			                try{
			                	//GetDirection(marker.getPosition());
			                	sleep(2000);
			                }catch(Exception e){
			                	e.printStackTrace();
			                }
			                finally{
			                	PDialog.dismiss();
				            }
		                }
	               }.start();
					//GetDirection(marker.getPosition());
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
