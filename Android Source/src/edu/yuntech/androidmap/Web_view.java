package edu.yuntech.androidmap;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Web_view extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);
        String myURL1 = "http://pda.freeway.gov.tw/m/";
        String myURL2 = "http://twtraffic.tra.gov.tw/twrail/mobile/home.aspx";
        String myURL3 = "http://www.cwb.gov.tw/pda/";
        String myURL4 = "http://www.necoast-nsa.gov.tw/mobile/scenicSpotList1.aspx";
        Bundle data = this.getIntent().getExtras();
        
        WebView myBrowser=(WebView)findViewById(R.id.webView1); 
        WebSettings websettings = myBrowser.getSettings();
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setJavaScriptEnabled(true);
        myBrowser.setWebViewClient(new WebViewClient());
        switch (data.getInt("web")){
        case 0:
        	myBrowser.loadUrl(myURL1);
        	setTitle("���ֳt�������p��T");
        	break;
        case 1:
        	myBrowser.loadUrl(myURL2);
        	setTitle("�C���d��");
        	break;
        case 2:
        	myBrowser.loadUrl(myURL3);
        	setTitle("�Y�ɮ�H");
        	break;
        case 3:
        	myBrowser.loadUrl(myURL4);
        	break;
        }
	}
}