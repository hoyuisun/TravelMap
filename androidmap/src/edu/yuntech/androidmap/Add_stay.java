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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Add_stay extends Activity{
	
	private String uriAPI = "http://140.125.45.113/contest/post_mysql/add_stay.php";
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
					Toast.makeText(Add_stay.this, "�W�Ǧ��\", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	
	private EditText name_tw;
	private EditText name_en;
	private EditText phone;
	private EditText fax;
	private EditText website;
	private EditText email;
	private EditText site_tw;
	private EditText site_en;
	private Button submit;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_stay);
        final Bundle point =this.getIntent().getExtras();
        
        name_tw = (EditText)findViewById(R.id.name_tw);
        name_en = (EditText)findViewById(R.id.name_en);
        phone = (EditText)findViewById(R.id.phone);
        fax = (EditText)findViewById(R.id.fax);
        website = (EditText)findViewById(R.id.website);
        email = (EditText)findViewById(R.id.email);
        site_tw = (EditText)findViewById(R.id.site_tw);
        site_en = (EditText)findViewById(R.id.site_en);
        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Thread t = new Thread(new sendPostRunnable(name_tw.getText().toString(), name_en.getText().toString(), phone.getText().toString(), fax.getText().toString(), 
						website.getText().toString(), email.getText().toString(), site_tw.getText().toString(), site_en.getText().toString() 
						, Double.toString(point.getDouble("lat")), Double.toString(point.getDouble("lng"))));
				t.start();
				finish();
			}
        });
	}
	
	class sendPostRunnable implements Runnable {
		String name_tw, name_en, fax, website, email, phone, site_tw, site_en, lat, lng;
		// �غc�l�A�]�w�n�Ǫ��r��
		public sendPostRunnable(String name_tw, String name_en, String phone, String fax, String website, String email, String site_tw, String site_en, String lat, String lng) {
			this.name_tw = name_tw;
			this.name_en = name_en;
			this.phone = phone;
			this.fax = fax;
			this.website = website;
			this.email = email;
			this.site_tw = site_tw;
			this.site_en = site_en;
			this.lat = lat;
			this.lng = lng;
		}

		@Override
		public void run() {
			String result = sendPostDataToInternet(name_tw, name_en, phone, fax, website, email, site_tw, site_en, lat, lng);
			mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
		}

	}

	private String sendPostDataToInternet(String name_tw, String name_en, String phone, String fax, String website, String email, String site_tw, String site_en, String lat, String lng) {

		/* �إ�HTTP Post�s�u */

		HttpPost httpRequest = new HttpPost(uriAPI);
		/*
		 * Post�B�@�ǰe�ܼƥ�����NameValuePair[]�}�C�x�s
		 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("name_tw", name_tw));
		params.add(new BasicNameValuePair("name_en", name_en));
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("fax", fax));
		params.add(new BasicNameValuePair("website", website));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("site_tw", site_tw));
		params.add(new BasicNameValuePair("site_en", site_en));
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
