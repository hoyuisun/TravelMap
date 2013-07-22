package edu.yuntech.androidmap;

import static edu.yuntech.androidmap.AndroidMap._planPoints;
import static edu.yuntech.androidmap.AndroidMap.plan_cnt;
import static edu.yuntech.androidmap.AndroidMap.viewStrings;
import static edu.yuntech.androidmap.AndroidMap.view_cnt;
import static edu.yuntech.androidmap.AndroidMap._view;
import static edu.yuntech.androidmap.AndroidMap.view_direction;
import static edu.yuntech.androidmap.AndroidMap.viewhash;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class Schedule extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setTitle("��{�W��");
        //ArrayList<LatLng> _view = (ArrayList<LatLng>)getIntent().getSerializableExtra("data");
        if(view_cnt > 8){
        	view_cnt = 0;
        	Toast.makeText(getApplicationContext(), "�A��ܶW�L8�Ӵ��I�A�Э��s���", Toast.LENGTH_SHORT).show();
        	this.finish();
        }else if(view_cnt > 0){
        	final EditText context = new EditText(Schedule.this);
        	
			new AlertDialog.Builder(Schedule.this)
			.setTitle("�п�J...")
			.setIcon(android.R.drawable.ic_dialog_info)
			.setView(context)
			.setPositiveButton("�T�w", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which){
					viewhash.put(context.getText().toString(), _view);
					viewStrings.add(context.getText().toString());
		        	view_cnt = 0;
		        	plan_cnt++;
		        	_view = new ArrayList<LatLng>();
		        }
			})
			.show();
        }else{
        	if(plan_cnt == 0){
        		Toast.makeText(getApplicationContext(), "�A�|������{�W��", Toast.LENGTH_SHORT).show();
        		finish();
        	}
        }
        ListView list = (ListView) findViewById(R.id.listView1);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, viewStrings));
        list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				_planPoints = viewhash.get(viewStrings.get(position));
				view_direction = true;
		        finish();
			}
        });
	}
}
