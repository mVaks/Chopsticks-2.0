package com.luxser.chopsticksOnline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.model.GameRequestContent.ActionType;
import com.facebook.share.widget.GameRequestDialog;

public class OnlineOtherActivity extends Activity{
	private GameRequestDialog requestDialog;
	private CallbackManager callbackManager;
	  protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	          WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	        FacebookSdk.sdkInitialize(getApplicationContext());
	        requestDialog = new GameRequestDialog(this);
	        requestDialog.registerCallback(callbackManager,
	          new FacebookCallback<GameRequestDialog.Result>() {
	          public void onSuccess(GameRequestDialog.Result result) {
	            String id = result.getRequestId();
	          }
	          public void onCancel() {}
	            public void onError(FacebookException error) {}
	          }
	        );
	      }

	      private void onClickRequestButton() {
	    	  GameRequestContent content = new GameRequestContent.Builder()
	    	    .setMessage("Come play Chopsticks with me")
	    	    .setTo("USER_ID")
	    	    .setActionType(ActionType.SEND)
	    	    .setObjectId("YOUR_OBJECT_ID")
	    	    .build();
	    	  requestDialog.show(content);
	      }

	      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        callbackManager.onActivityResult(requestCode, resultCode, data);
	      }
	
}
