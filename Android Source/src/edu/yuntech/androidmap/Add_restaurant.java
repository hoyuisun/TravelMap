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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Add_restaurant extends Activity{
	
	private String uriAPI = "http://140.125.45.113/contest/post_mysql/add_restaurant.php";
	/** �u�n��s�����v���T���N�X */
	protected static final int REFRESH_DATA = 0x00000001;

	/** �إ�UI Thread�ϥΪ�Handler�A�ӱ�����LThread�Ӫ��T�� */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// ��ܺ����W��������
			case REFRESH_DATA:
				String result = null;
				if (msg.obj instanceof String)
					result = (String) msg.obj;
				if (result != null)
					// �L�X�����^�Ǫ���r
					Toast.makeText(Add_restaurant.this, "�W�Ǧ��\", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	
	private EditText name;
	private EditText people;
	private EditText site;
	private EditText phone;
	private ImageButton submit;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_restaurant);
        final Bundle point =this.getIntent().getExtras();
        
        name = (EditText)findViewById(R.id.name);
        people = (EditText)findViewById(R.id.people);
        site = (EditText)findViewById(R.id.site);
        phone = (EditText)findViewById(R.id.phone);
        submit = (ImageButton)findViewById(R.id.submit);
        submit.setBackgroundDrawable(null);
        submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Thread t = new Thread(new sendPostRunnable(name.getText().toString(), people.getText().toString(), site.getText().toString(), 
						phone.getText().toString(), Double.toString(point.getDouble("lat")), Double.toString(point.getDouble("lng"))));
				t.start();
				finish();
			}
        });
        submit.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)  //���U���ɭԧ��ܭI�����C��
					submit.setImageResource(R.drawable.submit_down);
				else
					submit.setImageResource(R.drawable.submit);
				return false;
			}
        	
        });
	}
	
	class sendPostRunnable implements Runnable {
		String name, people, phone, site, lat, lng;
		// �غc�l�A�]�w�n�Ǫ��r��
		public sendPostRunnable(String name, String people, String site, String phone, String lat, String lng) {
			this.name = name;
			this.people = people;
			this.site = site;
			this.phone = phone;
			this.lat = lat;
			this.lng = lng;
		}

		@Override
		public void run() {
			String result = sendPostDataToInternet(name, people, site, phone, lat, lng);
			mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
		}

	}

	private String sendPostDataToInternet(String name, String people, String site, String phone, String lat, String lng) {

		/* �إ�HTTP Post�s�u */

		HttpPost httpRequest = new HttpPost(uriAPI);
		/*
		 * Post�B�@�ǰe�ܼƥ�����NameValuePair[]�}�C�x�s
		 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("people", people));
		params.add(new BasicNameValuePair("site", site));
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("lat", lat));
		params.add(new BasicNameValuePair("lng", lng));
		
		try {
			/* �o�XHTTP request */

			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			/* ���oHTTP response */
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
			/* �Y���A�X��200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* ���X�^���r�� */
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				// �^�Ǧ^���r��
				return strResult;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}