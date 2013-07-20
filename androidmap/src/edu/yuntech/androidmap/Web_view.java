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
        String myURL3 = "http://www.thsrc.com.tw/thsrcPDA/";
        String myURL4 = "http://www.cwb.gov.tw/pda/";
        String myURL5 = "http://taiwan.net.tw/pda/m1.aspx?sNo=0001042&keyString=^10002^^";
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
        	setTitle("高快速公路路況資訊");
        	break;
        case 1:
        	myBrowser.loadUrl(myURL2);
        	setTitle("台鐵");
        	break;
        case 2:
        	myBrowser.loadUrl(myURL3);
        	setTitle("高鐵");
        	break;
        case 3:
        	myBrowser.loadUrl(myURL4);
        	setTitle("即時氣象");
        	break;
        case 4:
        	myBrowser.loadUrl(myURL5);
        	break;
        }
	}
}
