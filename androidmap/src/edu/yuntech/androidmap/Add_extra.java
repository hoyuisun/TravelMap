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
					Toast.makeText(Add_extra.this, "上傳成功", Toast.LENGTH_LONG).show();
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
		// 建構子，設定要傳的字串
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

		/* 建立HTTP Post連線 */

		HttpPost httpRequest = new HttpPost(uriAPI);
		/*
		 * Post運作傳送變數必須用NameValuePair[]陣列儲存
		 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("select", select));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("site", site));
		params.add(new BasicNameValuePair("context", context));
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
}
