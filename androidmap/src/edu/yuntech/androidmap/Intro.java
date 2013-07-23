package edu.yuntech.androidmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class Intro extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.intro);
        setProgressBarIndeterminateVisibility(true);
        setTitle("程式載入中...");
        
        ImageView startup = (ImageView) findViewById(R.id.startup);
        AlphaAnimation alphaAnimation = new AlphaAnimation((float) 0.7, 1);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), AndroidMap.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub

			}
        });
        startup.setAnimation(alphaAnimation);
        startup.setVisibility(View.VISIBLE); 
        startup.setScaleType(ImageView.ScaleType.FIT_XY); 
	}
}