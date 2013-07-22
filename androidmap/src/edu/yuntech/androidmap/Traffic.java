package edu.yuntech.androidmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Traffic extends Activity {
	
	private static final String[] mStrings = new String[] {"高快速公路路況資訊", "台鐵", "高鐵"};
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setTitle("交通資訊");
        ListView list = (ListView) findViewById(R.id.listView1);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrings));
        list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
        		Intent intent = new Intent();
        		Bundle bundle = new Bundle();
				switch (position){
	            	case 0:
	            		bundle.putInt("web", 0);
	            		break;
	            	case 1:
	            		bundle.putInt("web", 1);
	            		break;
	            	case 2:
	            		bundle.putInt("web", 2);
	            		break;
            	}
				intent.setClass(getApplicationContext(), Web_view.class);
        		intent.putExtras(bundle);
        		startActivity(intent);
			}
        	
        });
	}
}
