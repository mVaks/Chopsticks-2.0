package com.luxser.chopsticksOnline;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class SplashActivity extends Activity {
   
  //Set waktu lama splashscreen
  private static int splashInterval = 2500;
 private ImageView imageView;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);
  requestWindowFeature(Window.FEATURE_NO_TITLE);
  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
    WindowManager.LayoutParams.FLAG_FULLSCREEN);   
   overridePendingTransition(R.anim.fadein, R.anim.fadeout);
   overridePendingTransition(R.anim.fadein, R.anim.fadeout);
   overridePendingTransition(R.anim.fadein, R.anim.fadeout);
   overridePendingTransition(R.anim.fadein, R.anim.fadeout);
   overridePendingTransition(R.anim.fadein, R.anim.fadeout);
   overridePendingTransition(R.anim.fadein, R.anim.fadeout);
   overridePendingTransition(R.anim.fadein, R.anim.fadeout);
   
   setContentView(R.layout.splashscreen);
   
   imageView = (ImageView)findViewById(R.id.imageView1);
   imageView.setBackgroundResource(R.drawable.movie);
   AnimationDrawable anim = (AnimationDrawable) imageView.getBackground();
   anim.start();

   new Handler().postDelayed(new Runnable() {
     
     
    @Override
    public void run() {
     // TODO Auto-generated method stub
     Intent i = new Intent(SplashActivity.this, OnlineActivity.class);
     startActivity(i);
     
  
                                 //jeda selesai Splashscreen
     this.finish();
        }

 private void finish() {
  // TODO Auto-generated method stub
  
 }
     }, splashInterval);
     
   };
   
     
  }
