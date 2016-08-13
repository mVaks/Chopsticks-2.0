package com.luxser.chopsticksOnline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class InfoActivity extends Activity{
	 protected void onCreate(Bundle savedInstanceState) {
	    	
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	          WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        setContentView(R.layout.infoscreen);
	        Button button1=(Button)findViewById(R.id.button1);
	     	Button button2=(Button)findViewById(R.id.button2);
	     	Button button3=(Button)findViewById(R.id.button3);
	     	  Button button4=(Button)findViewById(R.id.button4);
	     	 Button button5=(Button)findViewById(R.id.button5);
	    	   button1.setOnClickListener(new OnClickListener() {

	    	       public void onClick(View v) {
	    	    	   onDestroy();
	    	    	   Intent i = new Intent(InfoActivity.this, MainActivity.class);
	    	    	     startActivity(i);
	    	    	     
	    	           
	    	}
	    	   });
	    	   
	    	   button2.setOnClickListener(new OnClickListener() {

	    	       public void onClick(View v) {
	    	    	   onDestroy();
	    	    	   Intent i = new Intent(InfoActivity.this, SinglePlayerActivity.class);
	    	    	     startActivity(i);
	    	    	     
	    	           
	    	}
	    	   });
	    	   
	    	   button3.setOnClickListener(new OnClickListener() {

	    	       public void onClick(View v) {
	    	    	  // Toast.makeText(getApplicationContext(), "Coming soon...",
	    	    			   //Toast.LENGTH_SHORT).show();
	    	    	   Intent i = new Intent(InfoActivity.this, OnlineActivity.class);
	    	    	    startActivity(i);
	    	    	     onDestroy();
	    	           
	    	}
	    	   });
	    	 
	    	   button4.setOnClickListener(new OnClickListener() {

	    	       public void onClick(View v) {
	    	    	   Toast.makeText(getApplicationContext(), "Coming soon...",
	    	    			   Toast.LENGTH_SHORT).show();
	    	    	   //onDestroy();
	    	    	   //Intent i = new Intent(InfoActivity.this, MainActivity.class);
	    	    	   //  startActivity(i);
	    	    	     
	    	           
	    	}
	    	   });
	    	   
	    	   button5.setOnClickListener(new OnClickListener() {

	    	       public void onClick(View v) {
	    	    	   Toast.makeText(getApplicationContext(), "Coming soon...",
	    	    			   Toast.LENGTH_SHORT).show();
	    	    	   //onDestroy();
	    	    	  // Intent i = new Intent(InfoActivity.this, MainActivity.class);
	    	    	   //  startActivity(i);
	    	    	     
	    	           
	    	}
	    	   });
	        
	      
	 }
	 public void onBackPressed(){
		 
	 }
	 
}
