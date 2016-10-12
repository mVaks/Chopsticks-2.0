package com.luxser.chopsticksOnline;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;


public class SinglePlayerActivity extends Activity {
	   private boolean aiJustWent;
	
	   private LinearLayout bottomLeft;
		private LinearLayout bottomRight;
		private LinearLayout topLeft;
		private LinearLayout topRight;
		
		private ImageView zeroTopLeft;
		private ImageView oneTopLeft;
		private ImageView twoTopLeft;
		private ImageView threeTopLeft;
		 private ImageView fourTopLeft;
		 
		 private ImageView zeroTopRight;
		 private ImageView oneTopRight;
		 private ImageView twoTopRight;
		 private ImageView threeTopRight;
		 private ImageView fourTopRight;

		 private ImageView zeroBottomLeft;
		 private ImageView oneBottomLeft;
		 private ImageView twoBottomLeft;
		 private ImageView threeBottomLeft;
		 private ImageView fourBottomLeft;
		 
		 private ImageView zeroBottomRight;
		 private ImageView oneBottomRight;
		 private ImageView twoBottomRight;
		 private ImageView threeBottomRight;
		 private ImageView fourBottomRight;
		 private boolean isBottomTurn;
		 
		 private Handler handler;
		 
		 private TextView bottomYT;
		 private TextView topYT;
		 
		 private boolean isMoving;
		 private MediaPlayer mpclap;
		    private MediaPlayer mpsad;
		  private InterstitialAd adView;  // The ad
		  private Handler mHandler;       // Handler to display the ad on the UI thread
		  private Runnable displayAd;     // Code to execute to perform this operation
		 private InterstitialAd interstitial;
		 
	@Override
    //dragging stuff
    //http://www.vogella.com/tutorials/AndroidDragAndDrop/article.html

    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    	handler = new Handler();     // new handler
        handler.postDelayed(runnable, 1000);   // 10 mins int.
        setContentView(R.layout.activity_fullscreen);
        isBottomTurn = true;
         bottomLeft = (LinearLayout) findViewById(R.id.bottomleft);
    	bottomRight = (LinearLayout) findViewById(R.id.bottomright);
    	 topLeft = (LinearLayout) findViewById(R.id.topleft);
    	 topRight= (LinearLayout) findViewById(R.id.topright);
    	 
    	 zeroTopLeft = (ImageView) findViewById(R.id.zeroTopLeft);   

      	  oneTopLeft = (ImageView) findViewById(R.id.oneTopLeft);
      	
      	  
      	  twoTopLeft = (ImageView) findViewById(R.id.twoTopLeft);

         
      	  threeTopLeft = (ImageView) findViewById(R.id.threeTopLeft);

         
      	  fourTopLeft = (ImageView) findViewById(R.id.fourTopLeft);

      	  
      	  zeroTopRight = (ImageView) findViewById(R.id.zeroTopRight);
    
      	  
      	  oneTopRight= (ImageView) findViewById(R.id.oneTopRight);

         
      	  twoTopRight= (ImageView) findViewById(R.id.twoTopRight);

      	  
      	  threeTopRight= (ImageView) findViewById(R.id.threeTopRight);

      	  
      	  fourTopRight= (ImageView) findViewById(R.id.fourTopRight);

        	
      	  zeroBottomLeft = (ImageView) findViewById(R.id.zeroBottomLeft);

        	
      	  oneBottomLeft= (ImageView) findViewById(R.id.oneBottomLeft);


      	  twoBottomLeft= (ImageView) findViewById(R.id.twoBottomLeft);

      	  
      	  threeBottomLeft= (ImageView) findViewById(R.id.threeBottomLeft);

        	
      	  fourBottomLeft= (ImageView) findViewById(R.id.fourBottomLeft);

      	
      	  zeroBottomRight = (ImageView) findViewById(R.id.zeroBottomRight);

        	
      	  oneBottomRight= (ImageView) findViewById(R.id.oneBottomRight);

        	
      	  twoBottomRight= (ImageView) findViewById(R.id.twoBottomRight);

        	
      	  threeBottomRight= (ImageView) findViewById(R.id.threeBottomRight);

        	
      	  fourBottomRight= (ImageView) findViewById(R.id.fourBottomRight); 


        zeroBottomLeft.setOnTouchListener(new MyTouchListener());
        oneBottomLeft.setOnTouchListener(new MyTouchListener());
        twoBottomLeft.setOnTouchListener(new MyTouchListener());
       threeBottomLeft.setOnTouchListener(new MyTouchListener());
        fourBottomLeft.setOnTouchListener(new MyTouchListener());


        zeroBottomRight.setOnTouchListener(new MyTouchListener());
        oneBottomRight.setOnTouchListener(new MyTouchListener());
        twoBottomRight.setOnTouchListener(new MyTouchListener());
        threeBottomRight.setOnTouchListener(new MyTouchListener());
        fourBottomRight.setOnTouchListener(new MyTouchListener());
        
        
        topLeft.setOnDragListener(new MyDragListener());
        topRight.setOnDragListener(new MyDragListener());
        bottomLeft.setOnDragListener(new MyDragListener());
        bottomRight.setOnDragListener(new MyDragListener());
        
        bottomYT = (TextView) findViewById(R.id.bottomYT);
    	//topYT = (TextView) findViewById(R.id.//topYT);
         mpclap = MediaPlayer.create(this, R.raw.clap);
         mpsad = MediaPlayer.create(this, R.raw.sad);
         inaskDialog = false;
  

        	
    	adView = new InterstitialAd(this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        adView.setAdListener(new AdListener() {
            // Implement AdListener
        });
        mHandler = new Handler(Looper.getMainLooper());
        displayAd = new Runnable() {
          public void run() {  
            runOnUiThread(new Runnable() { 
              public void run() { 
                if (adView.isLoaded()) {
                  adView.show();
                }
              }
            });
          }
        };
        loadAd();

    }
	public boolean inaskDialog = false;
	public void askForRematch() {
		inaskDialog = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("Do you want a rematch?");

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Sure, rematch!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            	onDestroy();
                  	    	   Intent i = new Intent(SinglePlayerActivity.this, SinglePlayerActivity.class);
                  	    	     startActivity(i);
                            }
                        })
                .setNegativeButton("No.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            	inaskDialog = false;
                            }
                        });

        alertDialogBuilder.show();
    }

	private Runnable runnable = new Runnable() {
		   @Override
		   public void run() {

			  if(isBottomTurn && aiJustWent){
				  findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
       	    	  bottomYT.setText("Your Turn");
		          //findViewById(R.id.topYT).setVisibility(View.GONE);
			  }
			  else if(!isBottomTurn && !aiJustWent){
				  //findViewById(R.id.bottomYT).setVisibility(View.GONE);
       	    	  bottomYT.setText("Waiting...");
		          //findViewById(R.id.topYT).setVisibility(View.VISIBLE);
			  }
			   if(!isMoving)
			   findInvisible();
         	    if(!isBottomTurn){
         	    	aiMove();
         	    }
         	    if(ifBottomWon()){
         	    	bottomYT.setVisibility(View.VISIBLE);
        			//topYT.setVisibility(View.VISIBLE);
         	    	bottomYT.setText("You Won");
 		        	//topYT.setText("You Lost");
 		        	reCreateMedia();
 		        	mpclap.start();
 		    		if((int)(((Math.random()*4)+1)) == 1)
 		        	displayInterstitial();
 		        	askForRematch();
 		        	return;
         	    }
         	    if(ifTopWon()){
         	    	bottomYT.setVisibility(View.VISIBLE);
        			//topYT.setVisibility(View.VISIBLE);
         	    	bottomYT.setText("You Lost");
 		        	//topYT.setText("You Won");
 		        	reCreateMedia();
 		        	mpsad.start();
 		        	if((int)(((Math.random()*4)+1)) == 1)
 		        	displayInterstitial();
 		        	askForRematch();
 		        	return;
         	    }
         	  
       

		      handler.postDelayed(this, 1500);          // reschedule the handler
		   }
		};
public void reCreateMedia(){
	 mpclap = MediaPlayer.create(this, R.raw.clap);
      mpsad = MediaPlayer.create(this, R.raw.sad);
}
	private void findInvisible(){
		if (oneTopRight.getVisibility() == View.INVISIBLE){
			oneTopRight.setVisibility(View.VISIBLE);
    	}
    	else if (twoTopRight.getVisibility() == View.INVISIBLE){
    		twoTopRight.setVisibility(View.VISIBLE);
    	}
    	else if (threeTopRight.getVisibility() == View.INVISIBLE){
    		threeTopRight.setVisibility(View.VISIBLE);
    	}
    	else if (fourTopRight.getVisibility() == View.INVISIBLE){
    		fourTopRight.setVisibility(View.VISIBLE);
    	}
    	else if (zeroTopRight.getVisibility() == View.INVISIBLE){
    		zeroTopRight.setVisibility(View.VISIBLE);
    	}
	    if (oneBottomRight.getVisibility() == View.INVISIBLE){
	    	oneBottomRight.setVisibility(View.VISIBLE);
    	}
    	else if (twoBottomRight.getVisibility() == View.INVISIBLE){
	    	twoBottomRight.setVisibility(View.VISIBLE);
    	}
    	else if (threeBottomRight.getVisibility() == View.INVISIBLE){
	    	threeBottomRight.setVisibility(View.VISIBLE);

    	}
    	else if (fourBottomRight.getVisibility() == View.INVISIBLE){
	    	fourBottomRight.setVisibility(View.VISIBLE);

    	}
    	else if (zeroBottomRight.getVisibility() == View.INVISIBLE){
	    	zeroBottomRight.setVisibility(View.VISIBLE);

    	}
	if (oneBottomLeft.getVisibility() == View.INVISIBLE){
		oneBottomLeft.setVisibility(View.VISIBLE);
    	}
    	else if (twoBottomLeft.getVisibility() == View.INVISIBLE){
    		twoBottomLeft.setVisibility(View.VISIBLE);
    	}
    	else if (threeBottomLeft.getVisibility() == View.INVISIBLE){
    		threeBottomLeft.setVisibility(View.VISIBLE);
    	}
    	else if (fourBottomLeft.getVisibility() == View.INVISIBLE){
    		fourBottomLeft.setVisibility(View.VISIBLE);
    	}
    	else if (zeroBottomLeft.getVisibility() == View.INVISIBLE){
    		zeroBottomLeft.setVisibility(View.VISIBLE);
    	}
	if (oneTopLeft.getVisibility() == View.INVISIBLE){
		oneTopLeft.setVisibility(View.VISIBLE);
    	}
    	else if (twoTopLeft.getVisibility() == View.INVISIBLE){
    		twoTopLeft.setVisibility(View.VISIBLE);
    	}
    	else if (threeTopLeft.getVisibility() == View.INVISIBLE){
    		threeTopLeft.setVisibility(View.VISIBLE);
    	}
    	else if (fourTopLeft.getVisibility() == View.INVISIBLE){
    		fourTopLeft.setVisibility(View.VISIBLE);
    	}
    	else if (zeroTopLeft.getVisibility() == View.INVISIBLE){
    		zeroTopLeft.setVisibility(View.VISIBLE);
    	}


       
	}
	  private final class MyTouchListener implements OnTouchListener {
		    public boolean onTouch(View view, MotionEvent motionEvent) {
		    
		   if (view == zeroTopLeft || view == zeroTopRight || view == zeroBottomLeft || view ==zeroBottomRight) 	
			   return false;
		   if(isBottomTurn&&((view == oneTopLeft || view == twoTopLeft || view == threeTopLeft || view == fourTopLeft || view == zeroTopLeft ||view == oneTopRight || view == twoTopRight || view == threeTopRight || view == fourTopRight || view == zeroTopRight  ))){
			  return false;
		   }
		   else if(!isBottomTurn&&((view == oneBottomLeft || view == twoBottomLeft || view == threeBottomLeft || view == fourBottomLeft || view == zeroBottomLeft ||view == oneBottomRight || view == twoBottomRight || view == threeBottomRight || view == fourBottomRight || view == zeroBottomRight  ))){
		      return false;
		   }
		    else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
		          ClipData data = ClipData.newPlainText("", "");
		          DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
		          view.startDrag(data, shadowBuilder, view, 0);
		          view.setVisibility(View.INVISIBLE);
		          isMoving = true;
		           return true;
		    	}
		      
		    else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
		    	findInvisible();
	            view.setVisibility(View.VISIBLE);
	            isMoving = false;
	            return true;
		    }
		    else{
		    	return false;
		    }
		  }
	  }

		  class MyDragListener implements OnDragListener {
		    //Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
		   // Drawable normalShape = getResources().getDrawable(R.drawable.shape);

		    @Override
		    public boolean onDrag(View v, DragEvent event) {
		      int action = event.getAction();
		      switch (event.getAction()) {
		      case DragEvent.ACTION_DRAG_STARTED:
		        // do nothing
		        break;
		      case DragEvent.ACTION_DRAG_ENTERED:
		       // v.setBackgroundDrawable(enterShape);
		        break;
		      case DragEvent.ACTION_DRAG_EXITED:
		    	  
		    	//View view3 = (View) event.getLocalState();
		    	//if (dropEventNotHandled(event)) 
		        //      view3.setVisibility(View.VISIBLE);
		    	//View view = (View) event.getLocalState();
		        //view.setVisibility(View.VISIBLE);
		       // v.setBackgroundDrawable(normalShape);
		        //view3.setVisibility(View.VISIBLE);
		        break;
		      case DragEvent.ACTION_DROP:
		        // Dropped, reassign View to ViewGroup
		        View view1 = (View) event.getLocalState();
		       // ViewGroup owner = (ViewGroup) view1.getParent();
		        
		      //  owner.removeView(view);
		        LinearLayout container = (LinearLayout) v;
		        if (isBottomTurn&&((container == bottomRight || container == bottomLeft) && (view1 == oneBottomRight || view1 == twoBottomRight || view1 == threeBottomRight || view1 == fourBottomRight || view1 == zeroBottomRight || view1 == oneBottomLeft || view1 == twoBottomLeft || view1 == threeBottomLeft || view1 == fourBottomLeft || view1 == zeroBottomLeft))){
		        	if (splitBottom(view1,container)){
		        		isBottomTurn=!isBottomTurn;
		        	}
		        }
		        else if (!isBottomTurn&&((container == topRight||container==topLeft) && (view1 == oneTopRight || view1 == twoTopRight || view1 == threeTopRight || view1 == fourTopRight || view1 == zeroTopRight || view1 == oneTopLeft || view1 == twoTopLeft || view1 == threeTopLeft || view1 == fourTopLeft || view1 == zeroTopLeft))){
		        	if (splitTop(view1,container)){
		        		isBottomTurn=!isBottomTurn;
		        	}
		        	
		        	int numberTopLeft = 0;
		        	int numberTopRight = 0;
		        	int total = 0;

		        	 if (view1 == oneTopLeft){
		        	  numberTopLeft = 1;
		        	}
		        	 else if (view1 == twoTopLeft){
		        	  numberTopLeft = 2;
		        	}

		        	 else if (view1 == threeTopLeft){
		        	numberTopLeft = 3;

		        	}

		        	 else if (view1 == fourTopLeft){
		        	numberTopLeft = 4;

		        	}

		        	else if (view1 == zeroTopLeft){
		        	numberTopLeft = 0;

		        	}

		        	if (oneTopRight.getVisibility() == View.VISIBLE){
		        	numberTopRight = 1;
		        	}
		        	else if (twoTopRight.getVisibility() == View.VISIBLE){
		        	numberTopRight = 2;

		        	}

		        	else if (threeTopRight.getVisibility() == View.VISIBLE){
		        	numberTopRight = 3;

		        	}
		        	else if (fourTopRight.getVisibility() == View.VISIBLE){
		        	numberTopRight = 4;

		        	}
		        	else if (zeroTopRight.getVisibility() == View.VISIBLE){
		        	numberTopRight = 0;

		        	}
		        	total = numberTopLeft + numberTopRight;
		        	//getInput(view1);
		        	int inputLeft;
		        	int inputRight;
		        	//if ((inputLeft >= 0)&&(inputLeft < 5) && (inputRight <5)&& (inputRight >=0) && (inputLeft != numberTopLeft && inputRight != numberTopRight) && ((inputLeft + inputRight)==total)){
		        	//do something
		        	//}
		        	
		        	}

		        else if (isBottomTurn && container == topRight){
		        	attackTopRight(view1);
			    }
		    	else if(isBottomTurn && container == topLeft){
		    		attackTopLeft(view1);
			    }
		    	else if (!isBottomTurn && container == bottomRight){
		    		attackBottomRight(view1);
		    	}
		    		
		    	else if (!isBottomTurn && container == bottomLeft){
		    	attackBottomLeft(view1);	
		    	}
		        		findInvisible();
		    	
		       // container.addView(view);
		        break;
		      case DragEvent.ACTION_DRAG_ENDED:
		    	  
		    	View view = (View) event.getLocalState();
		    	LinearLayout container1 = (LinearLayout) v;
		        //v.setBackgroundDrawable(normalShape);
		        
		        if (!event.getResult() || (view!=null && (view.getVisibility()==View.INVISIBLE)))
		        view.setVisibility(View.VISIBLE);
		        if (isBottomTurn){
		        	findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
	       	    	  bottomYT.setText("Your Turn");

		        	//findViewById(R.id.//topYT).setVisibility(View.GONE);
		        }
		        else if (!isBottomTurn){
		        	//findViewById(R.id.//topYT).setVisibility(View.VISIBLE);
		        	//findViewById(R.id.bottomYT).setVisibility(View.GONE);
	       	    	  bottomYT.setText("Waiting...");
		        }
		        if(ifBottomWon()){
		        	//findViewById(R.id.topYT).setVisibility(View.VISIBLE);
		        	findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
        	    	bottomYT.setText("You Won");
		        	//topYT.setText("You Lost");
        	    }
		        else if(ifTopWon()){
		        	//findViewById(R.id.topYT).setVisibility(View.VISIBLE);
		        	findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
        	    	bottomYT.setText("You Lost");
		        	//topYT.setText("You Won");
        	    }
        	    	
		      default:
		    	
		        break;
		      }
		      return true;
		    }
		  }
		
		  public boolean ifBottomWon(){
			  return (zeroTopRight.getVisibility() == View.VISIBLE && zeroTopLeft.getVisibility()==View.VISIBLE);
		  }
		  public boolean ifTopWon(){
			  return (zeroBottomRight.getVisibility() == View.VISIBLE && zeroBottomLeft.getVisibility()==View.VISIBLE);
		  }
		 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public int getInput(View v){
    	 PopupMenu popup = new PopupMenu(this, v);
    	    MenuInflater inflater = popup.getMenuInflater();
    	    inflater.inflate(R.menu.actions, popup.getMenu());
    	    popup.show();
		return 0;
    }
    public boolean splitTop(View view1, LinearLayout x){
    	if (x == topRight){
    		if (view1 == oneTopLeft  && oneTopRight.getVisibility()==View.VISIBLE){
    			view1.setVisibility(View.GONE);
				zeroTopLeft.setVisibility(View.VISIBLE);
				oneTopRight.setVisibility(View.GONE);
				twoTopRight.setVisibility(View.VISIBLE);
				return true;
    	}
    		else if ((view1 == oneTopLeft && twoTopRight.getVisibility()==View.VISIBLE) || (view1 == twoTopLeft && oneTopRight.getVisibility()==View.VISIBLE)){
    			if(view1==oneTopLeft){
    				view1.setVisibility(View.GONE);
    				zeroTopLeft.setVisibility(View.VISIBLE);
    				twoTopRight.setVisibility(View.GONE);
    				threeTopRight.setVisibility(View.VISIBLE);
    			}
    			else {
    				view1.setVisibility(View.GONE);
    				zeroTopLeft.setVisibility(View.VISIBLE);
    				oneTopRight.setVisibility(View.GONE);
    				threeTopRight.setVisibility(View.VISIBLE);
    			}
    			return true;
    		}
    		else if ((view1 == oneTopLeft && threeTopRight.getVisibility()==View.VISIBLE) || (view1 == threeTopLeft && oneTopRight.getVisibility()==View.VISIBLE)){
    			if(view1==oneTopLeft){
    				view1.setVisibility(View.GONE);
    				twoTopLeft.setVisibility(View.VISIBLE);
    				threeTopRight.setVisibility(View.GONE);
    				twoTopRight.setVisibility(View.VISIBLE);
    			}
    			else {
    				view1.setVisibility(View.GONE);
    				twoTopLeft.setVisibility(View.VISIBLE);
    				oneTopRight.setVisibility(View.GONE);
    				twoTopRight.setVisibility(View.VISIBLE);
    			}
    			return true;
			}
	    	else if ((view1 == oneTopLeft && fourTopRight.getVisibility()==View.VISIBLE) || (view1 == fourTopLeft && oneTopRight.getVisibility()==View.VISIBLE)){
	    		if(view1==oneTopLeft){
					view1.setVisibility(View.GONE);
					twoTopLeft.setVisibility(View.VISIBLE);
					fourTopRight.setVisibility(View.GONE);
					threeTopRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					threeTopLeft.setVisibility(View.VISIBLE);
					oneTopRight.setVisibility(View.GONE);
					twoTopRight.setVisibility(View.VISIBLE);
				}
	    		return true;
	    	}
	    	else if (view1 == twoTopLeft && twoTopRight.getVisibility()==View.VISIBLE){
	    		view1.setVisibility(View.GONE);
				oneTopLeft.setVisibility(View.VISIBLE);
				twoTopRight.setVisibility(View.GONE);
				threeTopRight.setVisibility(View.VISIBLE);
				return true;
	    	}
	    	else if ((view1 == twoTopLeft && threeTopRight.getVisibility()==View.VISIBLE) || (view1 == threeTopLeft && twoTopRight.getVisibility()==View.VISIBLE)){
	    		if(view1==twoTopLeft){
					view1.setVisibility(View.GONE);
					oneTopLeft.setVisibility(View.VISIBLE);
					threeTopRight.setVisibility(View.GONE);
					fourTopRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					fourTopLeft.setVisibility(View.VISIBLE);
					twoTopRight.setVisibility(View.GONE);
					oneTopRight.setVisibility(View.VISIBLE);
				}	
	    		return true;
	    	}
	    	else if ((view1 == twoTopLeft && fourTopRight.getVisibility()==View.VISIBLE) || (view1 == fourTopLeft && twoTopRight.getVisibility()==View.VISIBLE)){
	    		if(view1==twoTopLeft){
					view1.setVisibility(View.GONE);
					threeTopLeft.setVisibility(View.VISIBLE);
					fourTopRight.setVisibility(View.GONE);
					threeTopRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					threeTopLeft.setVisibility(View.VISIBLE);
					twoTopRight.setVisibility(View.GONE);
					threeTopRight.setVisibility(View.VISIBLE);
				}
	    		return true;
	    	}
	    	else if ((view1 == twoTopLeft && zeroTopRight.getVisibility()==View.VISIBLE) || (view1 == zeroTopLeft && twoTopRight.getVisibility()==View.VISIBLE)){
	    		if(view1==twoTopLeft){
					view1.setVisibility(View.GONE);
					oneTopLeft.setVisibility(View.VISIBLE);
					zeroTopRight.setVisibility(View.GONE);
					oneTopRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					oneTopLeft.setVisibility(View.VISIBLE);
					twoTopRight.setVisibility(View.GONE);
					oneTopRight.setVisibility(View.VISIBLE);
				}	
	    		return true;
	    	}
	    	else if (view1 == threeTopLeft && threeTopRight.getVisibility()==View.VISIBLE){
	    		view1.setVisibility(View.GONE);
				twoTopLeft.setVisibility(View.VISIBLE);
				threeTopRight.setVisibility(View.GONE);
				fourTopRight.setVisibility(View.VISIBLE);
				return true;
	    	}
	     	else if ((view1 == threeTopLeft && zeroTopRight.getVisibility()==View.VISIBLE) || (view1 == zeroTopLeft && threeTopRight.getVisibility()==View.VISIBLE)){
	     		if(view1==threeTopLeft){
					view1.setVisibility(View.GONE);
					oneTopLeft.setVisibility(View.VISIBLE);
					zeroTopRight.setVisibility(View.GONE);
					twoTopRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					twoTopLeft.setVisibility(View.VISIBLE);
					zeroTopRight.setVisibility(View.GONE);
					oneTopRight.setVisibility(View.VISIBLE);
				}	
	     		return true;
	     	}
	     	else if ((view1 == fourTopLeft && zeroTopRight.getVisibility()==View.VISIBLE) || (view1 == zeroTopLeft && fourTopRight.getVisibility()==View.VISIBLE)){
	     		 if(view1==fourTopLeft){
	 				view1.setVisibility(View.GONE);
	 				twoTopLeft.setVisibility(View.VISIBLE);
	 				zeroTopRight.setVisibility(View.GONE);
	 				twoTopRight.setVisibility(View.VISIBLE);
	 			}
	 			else {
	 				view1.setVisibility(View.GONE);
	 				twoTopLeft.setVisibility(View.VISIBLE);
	 				zeroTopRight.setVisibility(View.GONE);
	 				twoTopRight.setVisibility(View.VISIBLE);
	 			}	
	     		return true;
	     	}
    		
    	}

    	
    	
    	
    	
    		else if(x == topLeft){
    			if (view1 == oneTopRight  && oneTopLeft.getVisibility()==View.VISIBLE){
    				view1.setVisibility(View.GONE);
    				zeroTopRight.setVisibility(View.VISIBLE);
    				oneTopLeft.setVisibility(View.GONE);
    				twoTopLeft.setVisibility(View.VISIBLE);
    				return true;
    			}
    			else if ((view1 == oneTopRight && twoTopLeft.getVisibility()==View.VISIBLE) || (view1 == twoTopRight && oneTopLeft.getVisibility()==View.VISIBLE)){
    				if(view1==oneTopRight){
    					view1.setVisibility(View.GONE);
    					zeroTopRight.setVisibility(View.VISIBLE);
    					twoTopLeft.setVisibility(View.GONE);
    					threeTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==twoTopRight){
    					view1.setVisibility(View.GONE);
    					zeroTopRight.setVisibility(View.VISIBLE);
    					oneTopLeft.setVisibility(View.GONE);
    					threeTopLeft.setVisibility(View.VISIBLE);
    				}
    				return true;
    			}
    			else if ((view1 == oneTopRight && threeTopLeft.getVisibility()==View.VISIBLE) || (view1 == threeTopRight && oneTopLeft.getVisibility()==View.VISIBLE)){
    				if(view1==oneTopRight){
    					view1.setVisibility(View.GONE);
    					twoTopRight.setVisibility(View.VISIBLE);
    					threeTopLeft.setVisibility(View.GONE);
    					twoTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==threeTopRight){
    					view1.setVisibility(View.GONE);
    					twoTopRight.setVisibility(View.VISIBLE);
    					oneTopLeft.setVisibility(View.GONE);
    					twoTopLeft.setVisibility(View.VISIBLE);
    				}
    				return true;
    			}
    	    	else if ((view1 == oneTopRight && fourTopLeft.getVisibility()==View.VISIBLE) || (view1 == fourTopRight && oneTopLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==oneTopRight){
    					view1.setVisibility(View.GONE);
    					twoTopRight.setVisibility(View.VISIBLE);
    					fourTopLeft.setVisibility(View.GONE);
    					threeTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==fourTopRight){
    					view1.setVisibility(View.GONE);
    					threeTopRight.setVisibility(View.VISIBLE);
    					oneTopLeft.setVisibility(View.GONE);
    					twoTopLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if (view1 == twoTopRight && twoTopLeft.getVisibility()==View.VISIBLE){
    	    		if(view1==twoTopRight){
    					view1.setVisibility(View.GONE);
    					oneTopRight.setVisibility(View.VISIBLE);
    					twoTopLeft.setVisibility(View.GONE);
    					threeTopLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if ((view1 == twoTopRight && threeTopLeft.getVisibility()==View.VISIBLE) || (view1 == threeTopRight && twoTopLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==twoTopRight){
    					view1.setVisibility(View.GONE);
    					oneTopRight.setVisibility(View.VISIBLE);
    					threeTopLeft.setVisibility(View.GONE);
    					fourTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==threeTopRight){
    					view1.setVisibility(View.GONE);
    					fourTopRight.setVisibility(View.VISIBLE);
    					twoTopLeft.setVisibility(View.GONE);
    					oneTopLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if ((view1 == twoTopRight && fourTopLeft.getVisibility()==View.VISIBLE) || (view1 == fourTopRight && twoTopLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==twoTopRight){
    					view1.setVisibility(View.GONE);
    					threeTopRight.setVisibility(View.VISIBLE);
    					fourTopLeft.setVisibility(View.GONE);
    					threeTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==fourTopRight){
    					view1.setVisibility(View.GONE);
    					threeTopRight.setVisibility(View.VISIBLE);
    					twoTopLeft.setVisibility(View.GONE);
    					threeTopLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if ((view1 == twoTopRight && zeroTopLeft.getVisibility()==View.VISIBLE) || (view1 == zeroTopRight && twoTopLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==twoTopRight){
    					view1.setVisibility(View.GONE);
    					oneTopRight.setVisibility(View.VISIBLE);
    					zeroTopLeft.setVisibility(View.GONE);
    					oneTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==zeroTopRight){
    					view1.setVisibility(View.GONE);
    					oneTopRight.setVisibility(View.VISIBLE);
    					twoTopLeft.setVisibility(View.GONE);
    					oneTopLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if (view1 == threeTopRight && threeTopLeft.getVisibility()==View.VISIBLE){
    	    		if(view1==threeTopRight){
    					view1.setVisibility(View.GONE);
    					twoTopRight.setVisibility(View.VISIBLE);
    					threeTopLeft.setVisibility(View.GONE);
    					fourTopLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	     	else if ((view1 == threeTopRight && zeroTopLeft.getVisibility()==View.VISIBLE) || (view1 == zeroTopRight && threeTopLeft.getVisibility()==View.VISIBLE)){
    	     		if(view1==threeTopRight){
    					view1.setVisibility(View.GONE);
    					oneTopRight.setVisibility(View.VISIBLE);
    					zeroTopLeft.setVisibility(View.GONE);
    					twoTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==zeroTopRight){
    					view1.setVisibility(View.GONE);
    					twoTopRight.setVisibility(View.VISIBLE);
    					zeroTopLeft.setVisibility(View.GONE);
    					oneTopLeft.setVisibility(View.VISIBLE);
    				}
    	     		return true;
    	     	}
    	    	//four zero has two possible combinations (4->2,2 or 4->3,1)

    	     	else if ((view1 == fourTopRight && zeroTopLeft.getVisibility()==View.VISIBLE) || (view1 == zeroTopRight && fourTopLeft.getVisibility()==View.VISIBLE)){
    	     		if(view1==fourTopRight){
    					view1.setVisibility(View.GONE);
    					twoTopRight.setVisibility(View.VISIBLE);
    					zeroTopLeft.setVisibility(View.GONE);
    					twoTopLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==zeroTopRight){
    					view1.setVisibility(View.GONE);
    					twoTopRight.setVisibility(View.VISIBLE);
    					fourTopLeft.setVisibility(View.GONE);
    					twoTopLeft.setVisibility(View.VISIBLE);
    				}
    	     		return true;
    	     	}
    			
    			
    		}
  
    	
    	
    	
    	
     	
		return false;
     	
    }


    public boolean splitBottom(View view1, LinearLayout x){
    	if (x == bottomRight){
    		if (view1 == oneBottomLeft  && oneBottomRight.getVisibility()==View.VISIBLE){
    			view1.setVisibility(View.GONE);
				zeroBottomLeft.setVisibility(View.VISIBLE);
				oneBottomRight.setVisibility(View.GONE);
				twoBottomRight.setVisibility(View.VISIBLE);
				return true;
    	}
    		else if ((view1 == oneBottomLeft && twoBottomRight.getVisibility()==View.VISIBLE) || (view1 == twoBottomLeft && oneBottomRight.getVisibility()==View.VISIBLE)){
    			if(view1==oneBottomLeft){
    				view1.setVisibility(View.GONE);
    				zeroBottomLeft.setVisibility(View.VISIBLE);
    				twoBottomRight.setVisibility(View.GONE);
    				threeBottomRight.setVisibility(View.VISIBLE);
    			}
    			else {
    				view1.setVisibility(View.GONE);
    				zeroBottomLeft.setVisibility(View.VISIBLE);
    				oneBottomRight.setVisibility(View.GONE);
    				threeBottomRight.setVisibility(View.VISIBLE);
    			}
    			return true;
    		}
    		else if ((view1 == oneBottomLeft && threeBottomRight.getVisibility()==View.VISIBLE) || (view1 == threeBottomLeft && oneBottomRight.getVisibility()==View.VISIBLE)){
    			if(view1==oneBottomLeft){
    				view1.setVisibility(View.GONE);
    				twoBottomLeft.setVisibility(View.VISIBLE);
    				threeBottomRight.setVisibility(View.GONE);
    				twoBottomRight.setVisibility(View.VISIBLE);
    			}
    			else {
    				view1.setVisibility(View.GONE);
    				twoBottomLeft.setVisibility(View.VISIBLE);
    				oneBottomRight.setVisibility(View.GONE);
    				twoBottomRight.setVisibility(View.VISIBLE);
    			}
    			return true;
			}
	    	else if ((view1 == oneBottomLeft && fourBottomRight.getVisibility()==View.VISIBLE) || (view1 == fourBottomLeft && oneBottomRight.getVisibility()==View.VISIBLE)){
	    		if(view1==oneBottomLeft){
					view1.setVisibility(View.GONE);
					twoBottomLeft.setVisibility(View.VISIBLE);
					fourBottomRight.setVisibility(View.GONE);
					threeBottomRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					threeBottomLeft.setVisibility(View.VISIBLE);
					oneBottomRight.setVisibility(View.GONE);
					twoBottomRight.setVisibility(View.VISIBLE);
				}
	    		return true;
	    	}
	    	else if (view1 == twoBottomLeft && twoBottomRight.getVisibility()==View.VISIBLE){
	    		view1.setVisibility(View.GONE);
				oneBottomLeft.setVisibility(View.VISIBLE);
				twoBottomRight.setVisibility(View.GONE);
				threeBottomRight.setVisibility(View.VISIBLE);
				return true;
	    	}
	    	else if ((view1 == twoBottomLeft && threeBottomRight.getVisibility()==View.VISIBLE) || (view1 == threeBottomLeft && twoBottomRight.getVisibility()==View.VISIBLE)){
	    		if(view1==twoBottomLeft){
					view1.setVisibility(View.GONE);
					oneBottomLeft.setVisibility(View.VISIBLE);
					threeBottomRight.setVisibility(View.GONE);
					fourBottomRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					fourBottomLeft.setVisibility(View.VISIBLE);
					twoBottomRight.setVisibility(View.GONE);
					oneBottomRight.setVisibility(View.VISIBLE);
				}	
	    		return true;
	    	}
	    	else if ((view1 == twoBottomLeft && fourBottomRight.getVisibility()==View.VISIBLE) || (view1 == fourBottomLeft && twoBottomRight.getVisibility()==View.VISIBLE)){
	    		if(view1==twoBottomLeft){
					view1.setVisibility(View.GONE);
					threeBottomLeft.setVisibility(View.VISIBLE);
					fourBottomRight.setVisibility(View.GONE);
					threeBottomRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					threeBottomLeft.setVisibility(View.VISIBLE);
					twoBottomRight.setVisibility(View.GONE);
					threeBottomRight.setVisibility(View.VISIBLE);
				}
	    		return true;
	    	}
	    	else if ((view1 == twoBottomLeft && zeroBottomRight.getVisibility()==View.VISIBLE) || (view1 == zeroBottomLeft && twoBottomRight.getVisibility()==View.VISIBLE)){
	    		if(view1==twoBottomLeft){
					view1.setVisibility(View.GONE);
					oneBottomLeft.setVisibility(View.VISIBLE);
					zeroBottomRight.setVisibility(View.GONE);
					oneBottomRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					oneBottomLeft.setVisibility(View.VISIBLE);
					twoBottomRight.setVisibility(View.GONE);
					oneBottomRight.setVisibility(View.VISIBLE);
				}	
	    		return true;
	    	}
	    	else if (view1 == threeBottomLeft && threeBottomRight.getVisibility()==View.VISIBLE){
	    		view1.setVisibility(View.GONE);
				twoBottomLeft.setVisibility(View.VISIBLE);
				threeBottomRight.setVisibility(View.GONE);
				fourBottomRight.setVisibility(View.VISIBLE);
				return true;
	    	}
	     	else if ((view1 == threeBottomLeft && zeroBottomRight.getVisibility()==View.VISIBLE) || (view1 == zeroBottomLeft && threeBottomRight.getVisibility()==View.VISIBLE)){
	     		if(view1==threeBottomLeft){
					view1.setVisibility(View.GONE);
					oneBottomLeft.setVisibility(View.VISIBLE);
					zeroBottomRight.setVisibility(View.GONE);
					twoBottomRight.setVisibility(View.VISIBLE);
				}
				else {
					view1.setVisibility(View.GONE);
					twoBottomLeft.setVisibility(View.VISIBLE);
					zeroBottomRight.setVisibility(View.GONE);
					oneBottomRight.setVisibility(View.VISIBLE);
				}	
	     		return true;
	     	}
	     	else if ((view1 == fourBottomLeft && zeroBottomRight.getVisibility()==View.VISIBLE) || (view1 == zeroBottomLeft && fourBottomRight.getVisibility()==View.VISIBLE)){
	     		 if(view1==fourBottomLeft){
	 				view1.setVisibility(View.GONE);
	 				twoBottomLeft.setVisibility(View.VISIBLE);
	 				zeroBottomRight.setVisibility(View.GONE);
	 				twoBottomRight.setVisibility(View.VISIBLE);
	 			}
	 			else {
	 				view1.setVisibility(View.GONE);
	 				twoBottomLeft.setVisibility(View.VISIBLE);
	 				zeroBottomRight.setVisibility(View.GONE);
	 				twoBottomRight.setVisibility(View.VISIBLE);
	 			}	
	     		return true;
	     	}
    		
    	}
    	
    	
    	
    	
    	
    		else if(x == bottomLeft){
    			if (view1 == oneBottomRight  && oneBottomLeft.getVisibility()==View.VISIBLE){
    				view1.setVisibility(View.GONE);
    				zeroBottomRight.setVisibility(View.VISIBLE);
    				oneBottomLeft.setVisibility(View.GONE);
    				twoBottomLeft.setVisibility(View.VISIBLE);
    				return true;
    			}
    			else if ((view1 == oneBottomRight && twoBottomLeft.getVisibility()==View.VISIBLE) || (view1 == twoBottomRight && oneBottomLeft.getVisibility()==View.VISIBLE)){
    				if(view1==oneBottomRight){
    					view1.setVisibility(View.GONE);
    					zeroBottomRight.setVisibility(View.VISIBLE);
    					twoBottomLeft.setVisibility(View.GONE);
    					threeBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==twoBottomRight){
    					view1.setVisibility(View.GONE);
    					zeroBottomRight.setVisibility(View.VISIBLE);
    					oneBottomLeft.setVisibility(View.GONE);
    					threeBottomLeft.setVisibility(View.VISIBLE);
    				}
    				return true;
    			}
    			else if ((view1 == oneBottomRight && threeBottomLeft.getVisibility()==View.VISIBLE) || (view1 == threeBottomRight && oneBottomLeft.getVisibility()==View.VISIBLE)){
    				if(view1==oneBottomRight){
    					view1.setVisibility(View.GONE);
    					twoBottomRight.setVisibility(View.VISIBLE);
    					threeBottomLeft.setVisibility(View.GONE);
    					twoBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==threeBottomRight){
    					view1.setVisibility(View.GONE);
    					twoBottomRight.setVisibility(View.VISIBLE);
    					oneBottomLeft.setVisibility(View.GONE);
    					twoBottomLeft.setVisibility(View.VISIBLE);
    				}
    				return true;
    			}
    	    	else if ((view1 == oneBottomRight && fourBottomLeft.getVisibility()==View.VISIBLE) || (view1 == fourBottomRight && oneBottomLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==oneBottomRight){
    					view1.setVisibility(View.GONE);
    					twoBottomRight.setVisibility(View.VISIBLE);
    					fourBottomLeft.setVisibility(View.GONE);
    					threeBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==fourBottomRight){
    					view1.setVisibility(View.GONE);
    					threeBottomRight.setVisibility(View.VISIBLE);
    					oneBottomLeft.setVisibility(View.GONE);
    					twoBottomLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if (view1 == twoBottomRight && twoBottomLeft.getVisibility()==View.VISIBLE){
    	    		if(view1==twoBottomRight){
    					view1.setVisibility(View.GONE);
    					oneBottomRight.setVisibility(View.VISIBLE);
    					twoBottomLeft.setVisibility(View.GONE);
    					threeBottomLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if ((view1 == twoBottomRight && threeBottomLeft.getVisibility()==View.VISIBLE) || (view1 == threeBottomRight && twoBottomLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==twoBottomRight){
    					view1.setVisibility(View.GONE);
    					oneBottomRight.setVisibility(View.VISIBLE);
    					threeBottomLeft.setVisibility(View.GONE);
    					fourBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==threeBottomRight){
    					view1.setVisibility(View.GONE);
    					fourBottomRight.setVisibility(View.VISIBLE);
    					twoBottomLeft.setVisibility(View.GONE);
    					oneBottomLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if ((view1 == twoBottomRight && fourBottomLeft.getVisibility()==View.VISIBLE) || (view1 == fourBottomRight && twoBottomLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==twoBottomRight){
    					view1.setVisibility(View.GONE);
    					threeBottomRight.setVisibility(View.VISIBLE);
    					fourBottomLeft.setVisibility(View.GONE);
    					threeBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==fourBottomRight){
    					view1.setVisibility(View.GONE);
    					threeBottomRight.setVisibility(View.VISIBLE);
    					twoBottomLeft.setVisibility(View.GONE);
    					threeBottomLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if ((view1 == twoBottomRight && zeroBottomLeft.getVisibility()==View.VISIBLE) || (view1 == zeroBottomRight && twoBottomLeft.getVisibility()==View.VISIBLE)){
    	    		if(view1==twoBottomRight){
    					view1.setVisibility(View.GONE);
    					oneBottomRight.setVisibility(View.VISIBLE);
    					zeroBottomLeft.setVisibility(View.GONE);
    					oneBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==zeroBottomRight){
    					view1.setVisibility(View.GONE);
    					oneBottomRight.setVisibility(View.VISIBLE);
    					twoBottomLeft.setVisibility(View.GONE);
    					oneBottomLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	    	else if (view1 == threeBottomRight && threeBottomLeft.getVisibility()==View.VISIBLE){
    	    		if(view1==threeBottomRight){
    					view1.setVisibility(View.GONE);
    					twoBottomRight.setVisibility(View.VISIBLE);
    					threeBottomLeft.setVisibility(View.GONE);
    					fourBottomLeft.setVisibility(View.VISIBLE);
    				}
    	    		return true;
    	    	}
    	     	else if ((view1 == threeBottomRight && zeroBottomLeft.getVisibility()==View.VISIBLE) || (view1 == zeroBottomRight && threeBottomLeft.getVisibility()==View.VISIBLE)){
    	     		if(view1==threeBottomRight){
    					view1.setVisibility(View.GONE);
    					oneBottomRight.setVisibility(View.VISIBLE);
    					zeroBottomLeft.setVisibility(View.GONE);
    					twoBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==zeroBottomRight){
    					view1.setVisibility(View.GONE);
    					twoBottomRight.setVisibility(View.VISIBLE);
    					zeroBottomLeft.setVisibility(View.GONE);
    					oneBottomLeft.setVisibility(View.VISIBLE);
    				}
    	     		return true;
    	     	}
    	    	//four zero has two possible combinations (4->2,2 or 4->3,1)

    	     	else if ((view1 == fourBottomRight && zeroBottomLeft.getVisibility()==View.VISIBLE) || (view1 == zeroBottomRight && fourBottomLeft.getVisibility()==View.VISIBLE)){
    	     		if(view1==fourBottomRight){
    					view1.setVisibility(View.GONE);
    					twoBottomRight.setVisibility(View.VISIBLE);
    					zeroBottomLeft.setVisibility(View.GONE);
    					twoBottomLeft.setVisibility(View.VISIBLE);
    				}
    				else if(view1==zeroBottomRight){
    					view1.setVisibility(View.GONE);
    					twoBottomRight.setVisibility(View.VISIBLE);
    					fourBottomLeft.setVisibility(View.GONE);
    					twoBottomLeft.setVisibility(View.VISIBLE);
    				}
    	     		return true;
    	     	}
    			
    			
    		}
  
    	
    	
    	
    	
     	
		return false;
     	
    }

   

    public boolean attackTopRight(View view1){
    	isBottomTurn=!isBottomTurn;
    	boolean quack = true;
    	if (oneTopRight.getVisibility() == View.VISIBLE){
    		if (view1 == oneBottomLeft || view1 == oneBottomRight){
    		       oneTopRight.setVisibility(View.GONE);
			           twoTopRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
        		   oneTopRight.setVisibility(View.GONE);
   			       threeTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
        		   oneTopRight.setVisibility(View.GONE);
   			       fourTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
        		   oneTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
        		   isBottomTurn=!isBottomTurn;
        		   quack = false;
	        	}
    	}
    	
    	else if (twoTopRight.getVisibility() == View.VISIBLE){
    		if (view1 == oneBottomLeft || view1 == oneBottomRight){
    			twoTopRight.setVisibility(View.GONE);
			           threeTopRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
    			twoTopRight.setVisibility(View.GONE);
   			       fourTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
    			twoTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
    			twoTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
    	else if (threeTopRight.getVisibility() == View.VISIBLE){
    		if (view1 == oneBottomLeft || view1 == oneBottomRight){
    			threeTopRight.setVisibility(View.GONE);
			           fourTopRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
    			threeTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
    			threeTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
    			threeTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
    	else if (fourTopRight.getVisibility() == View.VISIBLE){
    		 if (view1 == oneBottomLeft || view1 == oneBottomRight){
    			 fourTopRight.setVisibility(View.GONE);
			           zeroTopRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
    			fourTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
    			fourTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
    			fourTopRight.setVisibility(View.GONE);
   			       zeroTopRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
    	else if (zeroTopRight.getVisibility() == View.VISIBLE){
    		isBottomTurn=!isBottomTurn;
    		quack = false;
    	}
    	return quack;
    }
    public boolean attackBottomRight(View view1){
    	boolean quack = true;
    	isBottomTurn=!isBottomTurn;
		if (oneBottomRight.getVisibility() == View.VISIBLE){
    		if (view1 == oneTopLeft || view1 == oneTopRight){
    		       oneBottomRight.setVisibility(View.GONE);
			           twoBottomRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
        		   oneBottomRight.setVisibility(View.GONE);
   			       threeBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
        		   oneBottomRight.setVisibility(View.GONE);
   			       fourBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
        		   oneBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
    	else if (twoBottomRight.getVisibility() == View.VISIBLE){
    		if (view1 == oneTopLeft || view1 == oneTopRight){
    			twoBottomRight.setVisibility(View.GONE);
			           threeBottomRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
    			twoBottomRight.setVisibility(View.GONE);
   			       fourBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
    			twoBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
    			twoBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
    	else if (threeBottomRight.getVisibility() == View.VISIBLE){
    		if (view1 == oneTopLeft || view1 == oneTopRight){
    			threeBottomRight.setVisibility(View.GONE);
			           fourBottomRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
    			threeBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
    			threeBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
    			threeBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
    	else if (fourBottomRight.getVisibility() == View.VISIBLE){
    		 if (view1 == oneTopLeft || view1 == oneTopRight){
    			 fourBottomRight.setVisibility(View.GONE);
			           zeroBottomRight.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
    			fourBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
    			fourBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
    			fourBottomRight.setVisibility(View.GONE);
   			       zeroBottomRight.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack=false;
	        	}

    	}
    	else if (zeroBottomRight.getVisibility() == View.VISIBLE){
    		isBottomTurn=!isBottomTurn;
    		quack = false;
    	}
		return quack;
	}
    public boolean attackBottomLeft(View view1){
    	boolean quack = true;
    	isBottomTurn=!isBottomTurn;
		if (oneBottomLeft.getVisibility() == View.VISIBLE){
    		if (view1 == oneTopLeft || view1 == oneTopRight){
    			oneBottomLeft.setVisibility(View.GONE);
			           twoBottomLeft.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
    			oneBottomLeft.setVisibility(View.GONE);
   			       threeBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
    			oneBottomLeft.setVisibility(View.GONE);
   			       fourBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
    			oneBottomLeft.setVisibility(View.GONE);
   			       zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
		 else if (twoBottomLeft.getVisibility() == View.VISIBLE){
    		if (view1 == oneTopLeft || view1 == oneTopRight){
    			twoBottomLeft.setVisibility(View.GONE);
			           threeBottomLeft.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
    			twoBottomLeft.setVisibility(View.GONE);
   			       fourBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
    			twoBottomLeft.setVisibility(View.GONE);
   			       zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
    			twoBottomLeft.setVisibility(View.GONE);
   			       zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
		 else if (threeBottomLeft.getVisibility() == View.VISIBLE){
    		if (view1 == oneTopLeft || view1 == oneTopRight){
    			threeBottomLeft.setVisibility(View.GONE);
			           fourBottomLeft.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
    			threeBottomLeft.setVisibility(View.GONE);
   			       zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
    			threeBottomLeft.setVisibility(View.GONE);
    			zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
    			threeBottomLeft.setVisibility(View.GONE);
    			zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
		 else if (fourBottomLeft.getVisibility() == View.VISIBLE){
    		if (view1 == oneTopLeft || view1 == oneTopRight){
    			fourBottomLeft.setVisibility(View.GONE);
			           zeroBottomLeft.setVisibility(View.VISIBLE);
        	}
    		else if (view1 == twoTopLeft || view1 == twoTopRight){
    			fourBottomLeft.setVisibility(View.GONE);
    			zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == threeTopLeft || view1 == threeTopRight){
    			fourBottomLeft.setVisibility(View.GONE);
    			zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == fourTopLeft || view1 == fourTopRight){
    			fourBottomLeft.setVisibility(View.GONE);
    			zeroBottomLeft.setVisibility(View.VISIBLE);
	        	}
    		else if (view1 == zeroTopLeft || view1 == zeroTopRight){
    			isBottomTurn=!isBottomTurn;
    			quack = false;
	        	}
    	}
		 else if (zeroBottomLeft.getVisibility() == View.VISIBLE){
			 isBottomTurn=!isBottomTurn;
			 quack = false;
    	}
		return quack;
    }
  
    public boolean attackTopLeft(View view1){
    	boolean quack = true;
    	isBottomTurn=!isBottomTurn;
	      if (oneTopLeft.getVisibility() == View.VISIBLE){
  		if (view1 == oneBottomLeft || view1 == oneBottomRight){
  			oneTopLeft.setVisibility(View.GONE);
			           twoTopLeft.setVisibility(View.VISIBLE);
      	}
  		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
  			oneTopLeft.setVisibility(View.GONE);
 			       threeTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
  			oneTopLeft.setVisibility(View.GONE);
 			       fourTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
  			oneTopLeft.setVisibility(View.GONE);
 			       zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
  			isBottomTurn=!isBottomTurn;
  			quack = false;
	        	}
  	}
		 else if (twoTopLeft.getVisibility() == View.VISIBLE){
  		if (view1 == oneBottomLeft || view1 == oneBottomRight){
  			twoTopLeft.setVisibility(View.GONE);
			           threeTopLeft.setVisibility(View.VISIBLE);
      	}
  		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
  			twoTopLeft.setVisibility(View.GONE);
 			       fourTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
  			twoTopLeft.setVisibility(View.GONE);
 			       zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
  			twoTopLeft.setVisibility(View.GONE);
 			       zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
  			isBottomTurn=!isBottomTurn;
  			quack = false;
	        	}
  	}
		 else if (threeTopLeft.getVisibility() == View.VISIBLE){
  		if (view1 == oneBottomLeft || view1 == oneBottomRight){
  			threeTopLeft.setVisibility(View.GONE);
			           fourTopLeft.setVisibility(View.VISIBLE);
      	}
  		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
  			threeTopLeft.setVisibility(View.GONE);
 			       zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
  			threeTopLeft.setVisibility(View.GONE);
  			zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
  			threeTopLeft.setVisibility(View.GONE);
  			zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
  			isBottomTurn=!isBottomTurn;
  			quack = false;
	        	}
  	}
		 else if (fourTopLeft.getVisibility() == View.VISIBLE){
  		if (view1 == oneBottomLeft || view1 == oneBottomRight){
  			fourTopLeft.setVisibility(View.GONE);
			           zeroTopLeft.setVisibility(View.VISIBLE);
      	}
  		else if (view1 == twoBottomLeft || view1 == twoBottomRight){
  			fourTopLeft.setVisibility(View.GONE);
  			zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == threeBottomLeft || view1 == threeBottomRight){
  			fourTopLeft.setVisibility(View.GONE);
  			zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == fourBottomLeft || view1 == fourBottomRight){
  			fourTopLeft.setVisibility(View.GONE);
  			zeroTopLeft.setVisibility(View.VISIBLE);
	        	}
  		else if (view1 == zeroBottomLeft || view1 == zeroBottomRight){
  			isBottomTurn=!isBottomTurn;
  			quack = false;
	        	}
  	}
		 else if (zeroTopLeft.getVisibility() == View.VISIBLE){
			 isBottomTurn=!isBottomTurn;
			 quack = false;
  	}
	      return quack;
     
    }

   public void aiMoveFixed(){
	   int sum1 = returnNumericalTopRightZero();
		int sum2 = returnNumericalTopLeftZero();
		int sum3 = returnNumericalBottomRightZero();
		int sum4 = returnNumericalBottomLeftZero();
   	while(!isBottomTurn && !aiJustWent){
   	
   	
   		
   		if(ifBottomWon()){
   			bottomYT.setVisibility(View.VISIBLE);
   			////topYT.setVisibility(View.VISIBLE);
 	    	bottomYT.setText("You Won");
	        	////topYT.setText("You Lost");
	        	onPause();
	        	return;
   		}
   		if(ifTopWon()){
   			bottomYT.setVisibility(View.VISIBLE);
   			//topYT.setVisibility(View.VISIBLE);
   			bottomYT.setText("You Lost");
   			//topYT.setText("You Won");
   			onPause();
   			return;
   		}
   		View topLeft = findVisibilityTopLeft();
   		View topRight = findVisibilityTopRight();
   		View bottomLeft = findVisibilityBottomLeft();
   		View bottomRight = findVisibilityBottomRight();
   		int sumTopRight = returnNumericalTopRightZero();
   		int sumTopLeft = returnNumericalTopLeftZero();
   		int sumBottomRight = returnNumericalBottomRightZero();
   		int sumBottomLeft = returnNumericalBottomLeftZero();
   		//can win do it fam
   		
   		
   	}
   }
    
    public void aiMove(){
    	int sum1 = returnNumericalTopRightZero();
		int sum2 = returnNumericalTopLeftZero();
		int sum3 = returnNumericalBottomRightZero();
		int sum4 = returnNumericalBottomLeftZero();
    	while(!isBottomTurn && !aiJustWent){
    	
    	
    		
    		if(ifBottomWon()){
    			bottomYT.setVisibility(View.VISIBLE);
    			////topYT.setVisibility(View.VISIBLE);
  	    	bottomYT.setText("You Won");
	        	////topYT.setText("You Lost");
	        	onPause();
	        	return;
    		}
    		if(ifTopWon()){
    			bottomYT.setVisibility(View.VISIBLE);
    			//topYT.setVisibility(View.VISIBLE);
    			bottomYT.setText("You Lost");
    			//topYT.setText("You Won");
    			onPause();
    			return;
    		}
    		View view5 = findVisibilityTopLeft();
    		View view6 = findVisibilityTopRight();
    		View view7 = findVisibilityBottomLeft();
    		View view8 = findVisibilityBottomRight();
    		int sum5 = returnNumericalTopRightZero();
    		int sum6 = returnNumericalTopLeftZero();
    		int sum7 = returnNumericalBottomRightZero();
    		int sum8 = returnNumericalBottomLeftZero();
    		int x = (int)(((Math.random()*6)+1));
			if (findVisibilityBottomRight()==findVisibilityBottomLeft()&&(returnNumericalTopLeft()+returnNumericalBottomLeft()>=5 || returnNumericalTopRight()+returnNumericalBottomLeft()>=5)){
				if(findVisibilityBottomLeft()==findVisibilityBottomRight()){
					if(returnNumericalTopLeft()>returnNumericalTopRight()||view5==view6){
						int y = (int)(((Math.random()*2)+1));
						if (y == 1)
		    				x = 3;
		    			else if(y==2)
		    				x = 4;
					}
					else{
					int y = (int)(((Math.random()*4)+1));
					if (y == 1)
	    				x = 1;
	    			else if(y==2)
	    				x = 2;
	    			
					}
				}
				else if(returnNumericalTopRight()+returnNumericalBottomLeft()>=5){
					int y = (int)((Math.random()*2+1));
					if (y==1)
						x = 2;
					else
						x=3;
				}
				else{
					int y = (int)((Math.random()*2+1));
					if (y == 1)
	    				x = 1;
	    			else 
	    				x = 4;
				}
    			
			}
    		
			else if((returnNumericalTopLeft() == 3 && returnNumericalTopRight()==2) || (returnNumericalTopLeft()==2 && returnNumericalTopRight()==3)){
    			int y = (int)(((Math.random()*4)+1));
    			if (y == 1)
    				x = 1;
    			else if(y==2)
    				x = 2;
    			else if (y==3)
    				x = 3;
    			else if(y==4)
    				x=4;
    		}
    		
    		
    		 else if(view5==view6 && (view5==oneTopRight||view5==oneTopLeft)){
    			 if(findVisibilityBottomRight()==fourBottomRight){
    				 x=1;
    			 }
    			 else if(findVisibilityBottomLeft()==fourBottomLeft){
    				 x=2;
    			 }
    			 if(returnNumericalBottomRight()<returnNumericalBottomLeft())
    				x = 1;
    				else
    			    x = 2;
    		}
    		else if(view5 == view6){
    			int y = (int)((Math.random()*3)+1);
    			
    			 if(view5 == fourTopLeft){
    	    			y = (int)(((Math.random()*4)+1));
    	    			if (y == 1)
    	    				x = 3;
    	    			else if(y==2)
    	    				x = 5;
    	    			else if (y==3)
    	    				x = 5;
    	    			else
    	    				x=6;
    	    		}
    			 else if(view5==fourTopRight){
    					y = (int)((Math.random()*4+1));
     	    			if (y == 1)
     	    				x = 3;
     	    			else if(y==2)
     	    				x = 5;
     	    			else if (y==3)
     	    				x = 5;
     	    			else
     	    				x=6;
    			 }
    		
    			
    			 else if(returnNumericalTopLeft()+returnNumericalBottomRight()>=5 &&(findVisibilityBottomRight()==findVisibilityBottomLeft())){
    					y = (int)((Math.random()*2+1));
    				if(y==1)
    				x=1;
    				else
    			    x=2;
    			}
    			else if(returnNumericalTopLeft()+returnNumericalBottomLeft()>=5 &&(findVisibilityBottomRight()==findVisibilityBottomLeft())){
    				y = (int)((Math.random()*2+1));
    				if(y==1)
    				x=2;
    				else
    			    x=3;
    			}
    			
    			else{
    			y = (int)((Math.random()*2+1));
    			if(y==1)
    				x=6;
    			else
    				x=5;
    			}
    		}
    		else if (returnNumericalTopLeft()+returnNumericalBottomRight()>=5){
    			int y = (int)((Math.random()*6+1));
    			if (y == 1)
    				x = 4;
    			else if(y==2)
    				x = 4;
    			else if (y==3)
    				x = 4;
    			else if(y==4)
    				x = 4;
    			else if(y==5)
    				x=1;
    			else
    				x=6;
    		}
    		else if (returnNumericalTopLeft()+returnNumericalBottomLeft()>=5){
    			int y = (int)((Math.random()*6+1));
    			if (y == 1)
    				x = 3;
    			else if(y==2)
    				x = 3;
    			else if (y==3)
    				x = 3;
    			else if(y==4)
    				x = 3;
    			else if(y==5)
    				x= 2;
    			else
    				x=6;
    		}
    		else if (returnNumericalTopRight()+returnNumericalBottomRight()>=5){
    			int y = (int)((Math.random()*6+1));
    			if (y == 1)
    				x = 1;
    			else if(y==2)
    				x = 1;
    			else if (y==3)
    				x = 1;
    			else if(y==4)
    				x = 1;
    			else if(y==5)
    				x=4;
    			else
    				x=6;
    		}
    		else if (returnNumericalTopRight()+returnNumericalBottomLeft()>=5){
    			int y = (int)(((Math.random()*6)+1));
    			if (y == 1)
    				x = 2;
    			else if(y==2)
    				x = 2;
    			else if (y==3)
    				x = 2;
    			else if(y==4)
    				x = 2;
    			else if(y==5)
    				x=3;
    			else
    				x=6;
    		}
    	    
    		 else if((returnNumericalTopLeft() == 3 && returnNumericalTopRight()==3)){
      			int y = (int)(((Math.random()*4)+1));
      			if (y == 1)
      				x = 1;
      			else if(y==2)
      				x = 2;
      			else if (y==3)
      				x = 3;
      			else if(y==4)
      				x=4;
      		}
      		
    		else if(view5 == fourTopLeft){
    			int y = (int)(((Math.random()*4)+1));
    			if (y == 1)
    				x = 3;
    			else if(y==2)
    				x = 4;
    			else if (y==3)
    				x = 5;
    			else
    				x=6;
    		}
    		else if(view6 == fourTopRight){
    			int y = (int)(((Math.random()*4)+1));
    			if (y == 1)
    				x = 1;
    			else if(y==2)
    				x = 2;
    			else if (y==3)
    				x = 5;
    			else
    				x=6;
    		}
    		else if(returnNumericalBottomRight() == 0 && ((returnNumericalBottomLeft()+returnNumericalTopRight())>4 || (returnNumericalBottomLeft()+returnNumericalTopLeft())>4)){
	   			if((returnNumericalBottomLeft()+returnNumericalTopRight())>4)
	   				x=2;
	   			else
	   				x=3;
	   		}
	   		//can win do it fam
	   		else if(returnNumericalBottomLeft() == 0 && ((returnNumericalBottomRight()+returnNumericalTopRight())>4 || (returnNumericalBottomRight()+returnNumericalTopLeft())>4)){
	   			if((returnNumericalBottomRight()+returnNumericalTopRight())>4)
	   			x=1;
	   			else
	   			x=4;
	   		}
	   		//if only one finger
	   		else if(((returnNumericalTopRight() == 1) && (returnNumericalTopLeft() == 0)) || ((returnNumericalTopRight() == 0) && (returnNumericalTopLeft() == 1))){
	   			if(((returnNumericalTopRight() == 1) && (returnNumericalTopLeft() == 0))){
	   				//check which is less
	   				if(returnNumericalBottomRight() > returnNumericalBottomLeft())
	   					x=2;
	   				else
	   					x=1;
	   			}
	   			else{
	   			//check which is less
	   				if(returnNumericalBottomRight() > returnNumericalBottomLeft())
	   					x=3;
	   				else
	   					x=4;
	   			}
	   		}
	   		//if you are about to lose, don't and swap
	   		else if((returnNumericalTopRight() == 0 || returnNumericalTopLeft() == 0)){
	   			if(returnNumericalTopRight() == 0)
	   				x=6;
	   			else
	   				x=5;
	   		}
	   		else if(returnNumericalTopRight()+returnNumericalBottomRight() > 4){
	   			x=1;
	   		}
	   		else if(returnNumericalTopRight()+returnNumericalBottomLeft() > 4){
	   			x=2;
	   		}
	   		else if(returnNumericalTopLeft()+returnNumericalBottomLeft() > 4){
	   			x=3;
	   		}
	   		else if(returnNumericalTopLeft()+returnNumericalBottomRight() > 4){
	   			x=4;
	   		}
	   		else if(returnNumericalTopRight() == 1 && returnNumericalTopLeft() == 1){
	   			x=6;
	   		}
			
    		else if((returnNumericalBottomLeft()>=(Math.abs(5-returnNumericalTopRight() + returnNumericalTopLeft())) || returnNumericalBottomRight()>=(Math.abs(5-returnNumericalTopRight() + returnNumericalTopLeft())))){
    			int y = (int)(((Math.random()*5)+1));
    			if (y == 1)
    				x = 1;
    			else if(y==2)
    				x = 2;
    			else if (y==3)
    				x = 3;
    			else if(y==4)
    				x=4;
    			else
    				x=5;
    		}
    		else{
    			x = (int)(((Math.random()*6)+1));
    		}
    	    move(x);
    	    if (changedState(sum5,sum6,sum7,sum8)){
        		aiJustWent=true;
        		isBottomTurn=true;
        	}
    		else if (!changedState(sum5,sum6,sum7,sum8)){
        		aiJustWent=false;
        		isBottomTurn=false;
        	}
    		
    		  if(isBottomTurn){
    			  findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
       	    	  bottomYT.setText("Your Turn");

    	         // findViewById(R.id.topYT).setVisibility(View.GONE);
    		  }
    		  else if(!isBottomTurn){
    			  //findViewById(R.id.bottomYT).setVisibility(View.GONE);
    	         // findViewById(R.id.topYT).setVisibility(View.VISIBLE);
       	    	  bottomYT.setText("Waiting...");
    		  }
        	}
    	if (changedState(sum1,sum2,sum3,sum4)){
    		aiJustWent=true;
    		isBottomTurn=true;
    	}
		else if (!changedState(sum1,sum2,sum3,sum4)){
    		aiJustWent=false;
    		isBottomTurn=false;
    	}
    	}
    	
   public void move(int x){
		if (x==1){
    		attackBottomRight(findVisibilityTopRight());
    	    
        
    		}
    		
    		else if(x==2){
    			attackBottomLeft(findVisibilityTopRight());

    		}
    		else if(x==3){
    			attackBottomLeft(findVisibilityTopLeft());
    		}
    		else if(x==4){
    			attackBottomRight(findVisibilityTopLeft());
    			
        			
    		}
    		else if (x==5){
    			splitTop(findVisibilityTopLeft(),topRight);    		
    		} 
    		else{
    			splitTop(findVisibilityTopRight(),topLeft);
    		}
   }
    public View findVisibilityTopLeft(){
   	 if (oneTopLeft.getVisibility()==View.VISIBLE){
   	   return oneTopLeft;
   	}
   	 else if (twoTopLeft.getVisibility()==View.VISIBLE){
   	   return twoTopLeft;
   	}

   	 else if (threeTopLeft.getVisibility()==View.VISIBLE){
   	 return threeTopLeft;

   	}

   	 else if (fourTopLeft.getVisibility()==View.VISIBLE){
   	  return fourTopLeft;

   	}

     return findVisibilityTopRight();

   	}



    
    public View findVisibilityTopRight(){
      	 if (oneTopRight.getVisibility()==View.VISIBLE){
         	   return oneTopRight;
         	}
         	 else if (twoTopRight.getVisibility()==View.VISIBLE){
         	   return twoTopRight;
         	}

         	 else if (threeTopRight.getVisibility()==View.VISIBLE){
         	 return threeTopRight;

         	}

         	 else if (fourTopRight.getVisibility()==View.VISIBLE){
         	  return fourTopRight;

         	}

           return findVisibilityTopLeft();

         	
    }
    public View findVisibilityBottomRight(){
     	 if (oneBottomRight.getVisibility()==View.VISIBLE){
        	   return oneBottomRight;
        	}
        	 else if (twoBottomRight.getVisibility()==View.VISIBLE){
        	   return twoBottomRight;
        	}

        	 else if (threeBottomRight.getVisibility()==View.VISIBLE){
        	 return threeBottomRight;

        	}

        	 else if (fourBottomRight.getVisibility()==View.VISIBLE){
        	  return fourBottomRight;

        	}

          return findVisibilityBottomLeft();

        	
   }
    
    private View findVisibilityBottomLeft() {
   	 if (oneBottomLeft.getVisibility()==View.VISIBLE){
  	   return oneBottomLeft;
  	}
  	 else if (twoBottomLeft.getVisibility()==View.VISIBLE){
  	   return twoBottomLeft;
  	}

  	 else if (threeBottomLeft.getVisibility()==View.VISIBLE){
  	 return threeBottomLeft;

  	}

  	 else if (fourBottomLeft.getVisibility()==View.VISIBLE){
  	  return fourBottomLeft;

  	}

    return findVisibilityBottomRight();
	}
    
    
    
    
    
    public int returnNumericalTopLeft(){
      	 if (oneTopLeft.getVisibility()==View.VISIBLE){
      	   return 1;
      	}
      	 else if (twoTopLeft.getVisibility()==View.VISIBLE){
      	   return 2;
      	}

      	 else if (threeTopLeft.getVisibility()==View.VISIBLE){
      	 return 3;

      	}

      	 else if (fourTopLeft.getVisibility()==View.VISIBLE){
      	  return 4;

      	}

        return returnNumericalTopRight();

      	}
    
    public int returnNumericalTopLeftZero(){
      	 if (oneTopLeft.getVisibility()==View.VISIBLE){
      	   return 1;
      	}
      	 else if (twoTopLeft.getVisibility()==View.VISIBLE){
      	   return 2;
      	}

      	 else if (threeTopLeft.getVisibility()==View.VISIBLE){
      	 return 3;

      	}

      	 else if (fourTopLeft.getVisibility()==View.VISIBLE){
      	  return 4;

      	}

        return 0;

      	}


       public int returnNumericalTopRightZero(){
         	 if (oneTopRight.getVisibility()==View.VISIBLE){
            	   return 1;
            	}
            	 else if (twoTopRight.getVisibility()==View.VISIBLE){
            	   return 2;
            	}

            	 else if (threeTopRight.getVisibility()==View.VISIBLE){
            	 return 3;

            	}

            	 else if (fourTopRight.getVisibility()==View.VISIBLE){
            	  return 4;

            	}

              return 0;

            	
       }
       public int returnNumericalBottomRightZero(){
        	 if (oneBottomRight.getVisibility()==View.VISIBLE){
           	   return 1;
           	}
           	 else if (twoBottomRight.getVisibility()==View.VISIBLE){
           	   return 2;
           	}

           	 else if (threeBottomRight.getVisibility()==View.VISIBLE){
           	 return 3;

           	}

           	 else if (fourBottomRight.getVisibility()==View.VISIBLE){
           	  return 4;

           	}

             return 0;

           	
      }
       
       private int returnNumericalBottomLeftZero() {
      	 if (oneBottomLeft.getVisibility()==View.VISIBLE){
     	   return 1;
     	}
     	 else if (twoBottomLeft.getVisibility()==View.VISIBLE){
     	   return 2;
     	}

     	 else if (threeBottomLeft.getVisibility()==View.VISIBLE){
     	 return 3;

     	}

     	 else if (fourBottomLeft.getVisibility()==View.VISIBLE){
     	  return 4;

     	}

       return 0;
   	}
    

    
public boolean changedState(int tR,int tL,int bR, int bL){
	return ((tR != returnNumericalTopRightZero()) || (bR != returnNumericalBottomRightZero()) || (bL != returnNumericalBottomLeftZero()) || (tL!=returnNumericalTopLeftZero())	);
}

     

       public int returnNumericalTopRight(){
         	 if (oneTopRight.getVisibility()==View.VISIBLE){
            	   return 1;
            	}
            	 else if (twoTopRight.getVisibility()==View.VISIBLE){
            	   return 2;
            	}

            	 else if (threeTopRight.getVisibility()==View.VISIBLE){
            	 return 3;

            	}

            	 else if (fourTopRight.getVisibility()==View.VISIBLE){
            	  return 4;

            	}

              return returnNumericalTopLeft();

            	
       }
       public int returnNumericalBottomRight(){
        	 if (oneBottomRight.getVisibility()==View.VISIBLE){
           	   return 1;
           	}
           	 else if (twoBottomRight.getVisibility()==View.VISIBLE){
           	   return 2;
           	}

           	 else if (threeBottomRight.getVisibility()==View.VISIBLE){
           	 return 3;

           	}

           	 else if (fourBottomRight.getVisibility()==View.VISIBLE){
           	  return 4;

           	}

             return returnNumericalBottomLeft();

           	
      }
       
       private int returnNumericalBottomLeft() {
      	 if (oneBottomLeft.getVisibility()==View.VISIBLE){
     	   return 1;
     	}
     	 else if (twoBottomLeft.getVisibility()==View.VISIBLE){
     	   return 2;
     	}

     	 else if (threeBottomLeft.getVisibility()==View.VISIBLE){
     	 return 3;

     	}

     	 else if (fourBottomLeft.getVisibility()==View.VISIBLE){
     	  return 4;

     	}

       return returnNumericalBottomRight();
   	}
    
    
    
    
    
    
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // This defines your touch listener
	public void onBackPressed(){
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        displayInterstitial();
   	 Intent i = new Intent(SinglePlayerActivity.this, OnlineActivity.class);
	     startActivity(i);
	 }
	public void onAdClosed() {
	      loadAd(); // Need to reload the Ad when it is closed.
	    }

	    void loadAd() {
	      AdRequest adRequest = new AdRequest.Builder()
	      //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	     .build();

	      // Load the adView object witht he request
	      adView.loadAd(adRequest);
	    }

	    //Call displayInterstitial() once you are ready to display the ad.
	    public void displayInterstitial() {
	      mHandler.postDelayed(displayAd, 1);
	    }
	    public void onDestroy() {
	        super.onDestroy();
	        exitPlayer();
	 	  
	        
	 	 }
	    private void exitPlayer() {
	        if(mpclap!=null && mpclap.isPlaying()){
	        mpclap.stop();
	        mpclap = null;
	        }
	        if(mpsad!=null && mpsad.isPlaying()){
	        mpsad.stop();
	        }
	         }

	    
	    @Override
	    protected void onPause() {
	    	if(mpclap!=null){
	    	 mpclap.stop();
	    	 mpclap.release();
	    	}
	    	if(mpsad!=null){
	    	 mpsad.stop();
	    	 mpsad.release();
	    	}
	    	
	        super.onPause();
	       
	    }
	    @Override
	    protected void onResume(){
	        reCreateMedia();
	        //if(inaskDialog)
	        	//askForRematch();
	        
	        super.onResume();
	    }
	    
	    @Override
	    protected void onStop() {
	    	if(mpclap!=null){
	    		try{
	    	 mpclap.stop();
	    	 mpclap.release();
	    	}
	    		catch(Exception e){
	    		       Log.d("Stop", e.toString());
	    		}
	    	}
	    	if(mpsad!=null){
	    		try{
	    	 mpsad.stop();
	    	 mpsad.release();
	    		}
	    		catch(Exception e){
	    		       Log.d("Stop", e.toString());
	    		}
	    	}
	    	
	        super.onStop();
	       
	    }
}
