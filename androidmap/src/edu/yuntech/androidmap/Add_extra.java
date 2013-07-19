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

public class Add_extra extends Activity{
	
	private String uriAPI = "http://140.125.45.113/contest/post_mysql/add_extras.php";
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
					Toast.makeText(Add_extra.this, "�W�Ǧ��\", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	
	private EditText name;
	private EditText site;
	private EditText context;
	private Button submit;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_extra);
        final Bundle point =this.getIntent().getExtras();
        
        name = (EditText)findViewById(R.id.name);
        site = (EditText)findViewById(R.id.site);
        context = (EditText)findViewById(R.id.context);
        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Thread t = new Thread(new sendPostRunnable(point.getString("select"), name.getText().toString(), site.getText().toString(), 
						context.getText().toString(), Double.toString(point.getDouble("lat")), Double.toString(point.getDouble("lng"))));
				t.start();
				finish();
			}
        });
	}
	
	class sendPostRunnable implements Runnable {
		String select, name, context, site, lat, lng;
		// �غc�l�A�]�w�n�Ǫ��r��
		public sendPostRunnable(String select, String name, String site, String context, String lat, String lng) {
			this.select = select;
			this.name = name;
			this.site = site;
			this.context = context;
			this.lat = lat;
			this.lng = lng;
		}

		@Override
		public void run() {
			String result = sendPostDataToInternet(select, name, site, context, lat, lng);
			mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
		}

	}

	private String sendPostDataToInternet(String select, String name, String site, String context, String lat, String lng) {

		/* �إ�HTTP Post�s�u */

		HttpPost httpRequest = new HttpPost(uriAPI);
		/*
		 * Post�B�@�ǰe�ܼƥ�����NameValuePair[]�}�C�x�s
		 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("select", select));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("site", site));
		params.add(new BasicNameValuePair("context", context));
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
