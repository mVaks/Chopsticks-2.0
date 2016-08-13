package com.luxser.chopsticksOnline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.luxser.chopsticksOnline.MainActivity.MyDragListener;

public class TurnOnline extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
View.OnClickListener, RealTimeMessageReceivedListener,
RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener{
	 private GoogleApiClient mGoogleApiClient;
	   final static int RC_SELECT_PLAYERS = 10000;
	   final static int RC_INVITATION_INBOX = 10001;
	   final static int RC_WAITING_ROOM = 10002;
	   private static final int RC_SIGN_IN = 9001;
	   public boolean signInClicked;
	   

	   final static int[] CLICKABLES = {
           R.id.button_accept_popup_invitation, R.id.button_invite_players,
           R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
           R.id.button_sign_out, R.id.button_offline, R.id.button_single_player
   };
	   final static String TAG = "Chopsticks";
	   private static final String EXTRA_MIN_AUTOMATCH_PLAYERS = null;

	   private static final String EXTRA_MAX_AUTOMATCH_PLAYERS = null;

	   private static final String EXTRA_INVITATION = null;
	// Are we currently resolving a connection failure?
	   private boolean mResolvingConnectionFailure = false;

	   // Has the user clicked the sign-in button?
	   private boolean mSignInClicked = false;

	   // Set to true to automatically start the sign in flow when the Activity starts.
	   // Set to false to require the user to click the button in order to sign in.
	   private boolean mAutoStartSignInFlow = true;

	     // Room ID where the currently active game is taking place; null if we're
	     // not playing.
	     String mRoomId = null;

	     // Are we playing in multiplayer mode?
	     boolean mMultiplayer = false;

	     // The participants in the currently active game
	     ArrayList<Participant> mParticipants = null;

	     // My participant ID in the currently active game
	     String mMyId = null;

	     // If non-null, this is the id of the invitation we received via the
	     // invitation listener
	     String mIncomingInvitationId = null;

	     // Message buffer for sending messages
	     byte[] mMsgBuf = new byte[2];
	  protected void onCreate(Bundle savedInstanceState) {
	    	
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.online);
	       
	        // Create the Google Api Client with access to Games
	        mGoogleApiClient = new GoogleApiClient.Builder(this)
	            .addConnectionCallbacks(this)
	            .addOnConnectionFailedListener(this)
	            .addApi(Games.API).addScope(Games.SCOPE_GAMES)
	            .build();
	        
	         // set up a click listener for everything we care about
	            for (int id : CLICKABLES) {
	              findViewById(id).setOnClickListener(this);
	            }
	          }

	          @Override
	          public void onClick(View v) {
	            Intent intent;

	                switch (v.getId()) {
	                case R.id.button_single_player:
	                	intent =  new Intent(TurnOnline.this, SinglePlayerActivity.class);
	    	    	     startActivity(intent);
	    	    	     break;
	                case R.id.button_offline:
	                	intent = new Intent(TurnOnline.this, MainActivity.class);
	                	startActivity(intent);
	                	break;
	                    case R.id.button_sign_in:
	                        // user wants to sign in
	                        // Check to see the developer who's running this sample code read the instructions :-)
	                        // NOTE: this check is here only because this is a sample! Don't include this
	                        // check in your actual production app.
	                        if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
	                          Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
	                        }

	                        // start the sign-in flow
	                        Log.d(TAG, "Sign-in button clicked");
	                        mSignInClicked = true;
	                        mGoogleApiClient.connect();
	                    break;
	                    case R.id.button_sign_out:
	                        // user wants to sign out
	                        // sign out.
	                        Log.d(TAG, "Sign-out button clicked");
	                        mSignInClicked = false;
	                        Games.signOut(mGoogleApiClient);
	                        mGoogleApiClient.disconnect();
	                        switchToScreen(R.id.screen_sign_in);
	                        break;
	                    case R.id.button_invite_players:
	                        // show list of invitable players
	                        intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
	                        switchToScreen(R.id.screen_wait);
	                        startActivityForResult(intent, RC_SELECT_PLAYERS);
	                        break;
	                    case R.id.button_see_invitations:
	                        // show list of pending invitations
	                        intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
	                        switchToScreen(R.id.screen_wait);
	                        startActivityForResult(intent, RC_INVITATION_INBOX);
	                        break;
	                    case R.id.button_accept_popup_invitation:
	                        // user wants to accept the invitation shown on the invitation popup
	                        // (the one we got through the OnInvitationReceivedListener).
	                        acceptInviteToRoom(mIncomingInvitationId);
	                        mIncomingInvitationId = null;
	                        break;
	                    case R.id.button_quick_game:
	                        // user wants to play against a random opponent right now
	                        startQuickGame();
	                        break;
	                 
	                }
	            
	  }

	
	  final static int[] SCREENS = {
          R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
          R.id.screen_wait
  };
  int mCurScreen = -1;
	  void switchToScreen(int screenId) {
	       // make the requested screen visible; hide all others.
	       for (int id : SCREENS) {
	           findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
	       }
	       mCurScreen = screenId;

	       // should we show the invitation popup?
	       boolean showInvPopup;
	       if (mIncomingInvitationId == null) {
	           // no invitation, so no popup
	           showInvPopup = false;
	       } else if (mMultiplayer) {
	           // if in multiplayer, only show invitation on main screen
	           showInvPopup = (mCurScreen == R.id.screen_main);
	       } else {
	           // single-player: show on main screen and gameplay screen
	           showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
	       }
	       findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
	   }
	   // Accept the given invitation.
	   void acceptInviteToRoom(String invId) {
	       // accept the invitation
	       Log.d(TAG, "Accepting invitation: " + invId);
	       RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
	       roomConfigBuilder.setInvitationIdToAccept(invId)
	               .setMessageReceivedListener(this)
	               .setRoomStatusUpdateListener(this);
	       switchToScreen(R.id.screen_wait);
	       keepScreenOn();
	       resetGameVars();
	       Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
	   }


	 // Sets the flag to keep this screen on. It's recommended to do that during
	   // the
	   // handshake when setting up a game, because if the screen turns off, the
	   // game will be
	   // cancelled.
	   void keepScreenOn() {
	       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	   }

	   // Clears the flag that keeps the screen on.
	   void stopKeepingScreenOn() {
	       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	   }
	   void startQuickGame() {
	       // quick-start a game with 1 randomly selected opponent
	       final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
	       Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
	               MAX_OPPONENTS, 0);
	       RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
	       rtmConfigBuilder.setMessageReceivedListener(this);
	       rtmConfigBuilder.setRoomStatusUpdateListener(this);
	       rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
	       switchToScreen(R.id.screen_wait);
	       keepScreenOn();
	       resetGameVars();
	       Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
	   }
	   // Handle back key to make sure we cleanly leave a game if we are in the middle of one
	   @Override
	   public boolean onKeyDown(int keyCode, KeyEvent e) {
	       if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
	           leaveRoom();
	           return true;
	       }
	       return super.onKeyDown(keyCode, e);
	   }
	   // Leave the room.
	   void leaveRoom() {
	       Log.d(TAG, "Leaving room.");
	       //mSecondsLeft = 0;
	       stopKeepingScreenOn();
	       if (mRoomId != null) {
	           Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
	           mRoomId = null;
	           switchToScreen(R.id.screen_wait);
	       } else {
	           switchToMainScreen();
	       }
	   }
	   void switchToMainScreen() {
	       if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
	    	   switchToScreen(R.id.screen_main);
	       }
	       else {
	           switchToScreen(R.id.screen_sign_in);
	       }
	   }
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
		 
		

	   // Game tick -- update countdown, check if game ended.
	   void gameTick() {
	      // if (mSecondsLeft > 0)
	        //   --mSecondsLeft;

	       // update countdown
	       //((TextView) findViewById(R.id.countdown)).setText("0:" +
	         //      (mSecondsLeft < 10 ? "0" : "") + String.valueOf(mSecondsLeft));

	       //if (mSecondsLeft <= 0) {
	           // finish game
	         // findViewById(R.id.button_click_me).setVisibility(View.GONE);
	          // broadcastScore(true);
	       }
	// Start the gameplay phase of the game.
	   void startGame(boolean multiplayer) {
	       mMultiplayer = true;
	       //updateScoreDisplay();
	       //broadcastScore(false);
	       switchToScreen(R.id.screen_game);

	       //findViewById(R.id.button_click_me).setVisibility(View.VISIBLE);
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
	        zeroTopLeft.setOnTouchListener(new MyTouchListener());
	        oneTopLeft.setOnTouchListener(new MyTouchListener());
	        twoTopLeft.setOnTouchListener(new MyTouchListener());
	        threeTopLeft.setOnTouchListener(new MyTouchListener());
	        fourTopLeft.setOnTouchListener(new MyTouchListener());

	        zeroBottomLeft.setOnTouchListener(new MyTouchListener());
	        oneBottomLeft.setOnTouchListener(new MyTouchListener());
	        twoBottomLeft.setOnTouchListener(new MyTouchListener());
	       threeBottomLeft.setOnTouchListener(new MyTouchListener());
	        fourBottomLeft.setOnTouchListener(new MyTouchListener());

	     zeroTopRight.setOnTouchListener(new MyTouchListener());
	        oneTopRight.setOnTouchListener(new MyTouchListener());
	        twoTopRight.setOnTouchListener(new MyTouchListener());
	        threeTopRight.setOnTouchListener(new MyTouchListener());
	        fourTopRight.setOnTouchListener(new MyTouchListener());
	        
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
	    	topYT = (TextView) findViewById(R.id.topYT);

	    	
	      		  
	      	
	    	
	         final Handler handler = new Handler();
	         final Runnable refresher = new Runnable() {
	        	  public void run() {
	        	    findInvisible();
	        	    if(ifBottomWon()){
	        	    	bottomYT.setText("You Won");
			        	topYT.setText("You Lost");
	        	    }
	        	    if(ifTopWon()){
	        	    	bottomYT.setText("You Lost");
			        	topYT.setText("You Won");
	        	    }
	        	    	
	        	  }
	        	};
	        	handler.postDelayed(refresher, 100);
	       // run the gameTick() method every second to update the game.
//	       final Handler h = new Handler();
//	       h.postDelayed(new Runnable() {
//	           @Override
//	           public void run() {
//	              // if (mSecondsLeft <= 0)
//	                //   return;
//	               gameTick();
//	               h.postDelayed(this, 1000);
//	           }
//	       }, 1000);
	   }
	   private Runnable runnable = new Runnable() {
		   @Override
		   public void run() {
			   if(!isMoving)
			   findInvisible();
      	    if(ifBottomWon()){
      	    	bottomYT.setText("You Won");
		        	topYT.setText("You Lost");
      	    }
      	    if(ifTopWon()){
      	    	bottomYT.setText("You Lost");
		        	topYT.setText("You Won");
      	    }
      	  
    

		      handler.postDelayed(this, 1000);          // reschedule the handler
		   }
		};
	   // Current state of the game:
	   //int mSecondsLeft = -1; // how long until the game ends (seconds)
	   //final static int GAME_DURATION = 20; // game duration, seconds.
	   //int mScore = 0; // user's current score

	   // Reset game variables in preparation for a new game.
	   void resetGameVars() {
	       //mSecondsLeft = GAME_DURATION;
	       //mScore = 0;
	       //mParticipantScore.clear();
	       //mFinishedParticipants.clear();
	   }
	

	   // Show the waiting room UI to track the progress of other players as they enter the
	   // room and get connected.
	   void showWaitingRoom(Room room) {
	       // minimum number of players required for our game
	       // For simplicity, we require everyone to join the game before we start it
	       // (this is signaled by Integer.MAX_VALUE).
	       final int MIN_PLAYERS = Integer.MAX_VALUE;
	       Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

	       // show waiting room UI
	       startActivityForResult(i, RC_WAITING_ROOM);
	   }

	   // Called when we get an invitation to play a game. We react by showing that to the user.
	   @Override
	   public void onInvitationReceived(Invitation invitation) {
	       // We got an invitation to play a game! So, store it in
	       // mIncomingInvitationId
	       // and show the popup on the screen.
	       mIncomingInvitationId = invitation.getInvitationId();
	       ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
	               invitation.getInviter().getDisplayName() + " " +
	                       getString(R.string.is_inviting_you));
	       switchToScreen(mCurScreen); // This will show the invitation popup
	   }

	   @Override
	   public void onInvitationRemoved(String invitationId) {
	      
	       if (mIncomingInvitationId.equals(invitationId)&&mIncomingInvitationId!=null) {
	           mIncomingInvitationId = null;
	           switchToScreen(mCurScreen); // This will hide the invitation popup
	       }
	     
	   }

	   
	    // CALLBACKS SECTION. This section shows how we implement the several games
	    // API callbacks.
	    

	   @Override
	   public void onConnected(Bundle connectionHint) {
	     Log.d(TAG, "onConnected() called. Sign in successful!");

	     Log.d(TAG, "Sign-in succeeded.");

	     // register listener so we are notified if we receive an invitation to play
	     // while we are in the game
	     Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

	     if (connectionHint != null) {
	       Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
	       Invitation inv = connectionHint
	           .getParcelable(Multiplayer.EXTRA_INVITATION);
	       if (inv != null && inv.getInvitationId() != null) {
	         // retrieve and cache the invitation ID
	         Log.d(TAG,"onConnected: connection hint has a room invite!");
	         acceptInviteToRoom(inv.getInvitationId());
	         return;
	       }
	     }
	     switchToMainScreen();

	   }

	   @Override
	   public void onConnectionSuspended(int i) {
	     Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
	     mGoogleApiClient.connect();
	   }

	   @Override
	   public void onConnectionFailed(ConnectionResult connectionResult) {
	     Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

	     if (mResolvingConnectionFailure) {
	       Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
	       return;
	     }

	     if (mSignInClicked || mAutoStartSignInFlow) {
	       mAutoStartSignInFlow = false;
	       mSignInClicked = false;
	       mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
	           connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
	     }

	     switchToScreen(R.id.screen_sign_in);
	   }

	   // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
	   // is connected yet).
	   @Override
	   public void onConnectedToRoom(Room room) {
	       Log.d(TAG, "onConnectedToRoom.");

	       //get participants and my ID:
	       mParticipants = room.getParticipants();
	       mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
	       
	        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
	        if(mRoomId==null)
	         mRoomId = room.getRoomId();

	       // print out the list of participants (for debug purposes)
	       Log.d(TAG, "Room ID: " + mRoomId);
	       Log.d(TAG, "My ID " + mMyId);
	       Log.d(TAG, "<< CONNECTED TO ROOM>>");
	   }

	   // Called when we've successfully left the room (this happens a result of voluntarily leaving
	   // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
	   @Override
	   public void onLeftRoom(int statusCode, String roomId) {
	       // we have left the room; return to main screen.
	       Log.d(TAG, "onLeftRoom, code " + statusCode);
	       switchToMainScreen();
	   }

	   // Called when we get disconnected from the room. We return to the main screen.
	   @Override
	   public void onDisconnectedFromRoom(Room room) {
	       mRoomId = null;
	       showGameError();
	   }

	   // Show error message about game being cancelled and return to main screen.
	   void showGameError() {
	       BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
	       switchToMainScreen();
	   }

	   // Called when room has been created
	   @Override
	   public void onRoomCreated(int statusCode, Room room) {
	       Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
	       if (statusCode != GamesStatusCodes.STATUS_OK) {
	           Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
	           showGameError();
	           return;
	       }

	      // save room ID so we can leave cleanly before the game starts.
	       mRoomId = room.getRoomId();

	       // show the waiting room UI
	       showWaitingRoom(room);
	   }

	   // Called when room is fully connected.
	   @Override
	   public void onRoomConnected(int statusCode, Room room) {
	       Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
	       if (statusCode != GamesStatusCodes.STATUS_OK) {
	           Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
	           showGameError();
	           return;
	       }
	       updateRoom(room);
	   }

	   @Override
	   public void onJoinedRoom(int statusCode, Room room) {
	       Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
	       if (statusCode != GamesStatusCodes.STATUS_OK) {
	           Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
	           showGameError();
	           return;
	       }

	       // show the waiting room UI
	       showWaitingRoom(room);
	   }

	   // We treat most of the room update callbacks in the same way: we update our list of
	   // participants and update the display. In a real game we would also have to check if that
	   // change requires some action like removing the corresponding player avatar from the screen,
	   // etc.
	   @Override
	   public void onPeerDeclined(Room room, List<String> arg1) {
	       updateRoom(room);
	   }

	   @Override
	   public void onPeerInvitedToRoom(Room room, List<String> arg1) {
	       updateRoom(room);
	   }

	   @Override
	   public void onP2PDisconnected(String participant) {
	   }

	   @Override
	   public void onP2PConnected(String participant) {
	   }

	   @Override
	   public void onPeerJoined(Room room, List<String> arg1) {
	       updateRoom(room);
	   }

	   @Override
	   public void onPeerLeft(Room room, List<String> peersWhoLeft) {
	       updateRoom(room);
	   }

	   @Override
	   public void onRoomAutoMatching(Room room) {
	       updateRoom(room);
	   }

	   @Override
	   public void onRoomConnecting(Room room) {
	       updateRoom(room);
	   }

	   @Override
	   public void onPeersConnected(Room room, List<String> peers) {
	       updateRoom(room);
	   }

	   @Override
	   public void onPeersDisconnected(Room room, List<String> peers) {
	       updateRoom(room);
	   }

	   void updateRoom(Room room) {
	       if (room != null) {
	           mParticipants = room.getParticipants();
	       }
	       if (mParticipants != null) {
	           //updatePeerScoresDisplay();
	       }
	   }
	

	   @Override
	   public void onActivityResult(int requestCode, int responseCode,
	           Intent intent) {
	       super.onActivityResult(requestCode, responseCode, intent);

	       switch (requestCode) {
	           case RC_SELECT_PLAYERS:
	               // we got the result from the "select players" UI -- ready to create the room
	               handleSelectPlayersResult(responseCode, intent);
	               break;
	           case RC_INVITATION_INBOX:
	               // we got the result from the "select invitation" UI (invitation inbox). We're
	               // ready to accept the selected invitation:
	               handleInvitationInboxResult(responseCode, intent);
	               break;
	           case RC_WAITING_ROOM:
	               // we got the result from the "waiting room" UI.
	               if (responseCode == Activity.RESULT_OK) {
	                   // ready to start playing
	                   Log.d(TAG, "Starting game (waiting room returned OK).");
	                   startGame(true);
	               } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
	                   // player indicated that they want to leave the room
	                   leaveRoom();
	               } else if (responseCode == Activity.RESULT_CANCELED) {
	                   // Dialog was cancelled (user pressed back key, for instance). In our game,
	                   // this means leaving the room too. In more elaborate games, this could mean
	                   // something else (like minimizing the waiting room UI).
	                   leaveRoom();
	               }
	               break;
	           case RC_SIGN_IN:
	               Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
	                   + responseCode + ", intent=" + intent);
	               mSignInClicked = false;
	               mResolvingConnectionFailure = false;
	               if (responseCode == RESULT_OK) {
	                 mGoogleApiClient.connect();
	               } else {
	                 BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
	               }
	               break;
	       }
	       super.onActivityResult(requestCode, responseCode, intent);
	   }

	   // Handle the result of the "Select players UI" we launched when the user clicked the
	   // "Invite friends" button. We react by creating a room with those players.
	   private void handleSelectPlayersResult(int response, Intent data) {
	       if (response != Activity.RESULT_OK) {
	           Log.w(TAG, "*** select players UI cancelled, " + response);
	           switchToMainScreen();
	           return;
	       }

	       Log.d(TAG, "Select players UI succeeded.");

	       // get the invitee list
	       final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
	       Log.d(TAG, "Invitee count: " + invitees.size());

	       // get the automatch criteria
	       Bundle autoMatchCriteria = null;
	       int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
	       int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
	       if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
	           autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
	                   minAutoMatchPlayers, maxAutoMatchPlayers, 0);
	           Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
	       }

	       // create the room
	       Log.d(TAG, "Creating room...");
	       RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
	       rtmConfigBuilder.addPlayersToInvite(invitees);
	       rtmConfigBuilder.setMessageReceivedListener(this);
	       rtmConfigBuilder.setRoomStatusUpdateListener(this);
	       if (autoMatchCriteria != null) {
	           rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
	       }
	       switchToScreen(R.id.screen_wait);
	       keepScreenOn();
	       resetGameVars();
	       Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
	       Log.d(TAG, "Room created, waiting for it to be ready...");
	   }

	   // Handle the result of the invitation inbox UI, where the player can pick an invitation
	   // to accept. We react by accepting the selected invitation, if any.
	   private void handleInvitationInboxResult(int response, Intent data) {
	       if (response != Activity.RESULT_OK) {
	           Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
	           switchToMainScreen();
	           return;
	       }

	       Log.d(TAG, "Invitation inbox UI succeeded.");
	       Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

	       // accept invitation
	       acceptInviteToRoom(inv.getInvitationId());
	   }

	

	   // Activity is going to the background. We have to leave the current room.
	   @Override
	   public void onStop() {
	       Log.d(TAG, "**** got onStop");

	       // if we're in a room, leave it.
	       leaveRoom();

	       // stop trying to keep the screen on
	       stopKeepingScreenOn();

	       if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
	         switchToScreen(R.id.screen_sign_in);
	       }
	       else {
	         switchToScreen(R.id.screen_wait);
	       }
	       super.onStop();
	     }

	   // Activity just got to the foreground. We switch to the wait screen because we will now
	   // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
	   // foreground we go through the sign-in flow -- but if the user is already authenticated,
	   // this flow simply succeeds and is imperceptible).
	   @Override
	   public void onStart() {
		   switchToScreen(R.id.screen_sign_in);
//	       switchToScreen(R.id.screen_wait);
//	       if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//	         Log.w(TAG,
//	             "GameHelper: client was already connected on onStart()");
//	       } else {
//	         Log.d(TAG,"Connecting client.");
//	         mGoogleApiClient.connect();
//	       }
       super.onStart();
	   }
	   // Score of other participants. We update this as we receive their scores
	   // from the network.
	   Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

	   // Participants who sent us their final score.
	   Set<String> mFinishedParticipants = new HashSet<String>();
	   @Override
	   public void onRealTimeMessageReceived(RealTimeMessage rtm) {
	       byte[] buf = rtm.getMessageData();
	       String sender = rtm.getSenderParticipantId();
	       Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

	       if (buf[0] == 'F' || buf[0] == 'U') {
	           // score update.
	           //int existingScore = mParticipantScore.containsKey(sender) ?
	             //      mParticipantScore.get(sender) : 0;
	           //int thisScore = (int) buf[1];
	           //if (thisScore > existingScore) {
	               // this check is necessary because packets may arrive out of
	               // order, so we
	               // should only ever consider the highest score we received, as
	               // we know in our
	               // game there is no way to lose points. If there was a way to
	               // lose points,
	               // we'd have to add a "serial number" to the packet.
	             //  mParticipantScore.put(sender, thisScore);
	           //}

	           // update the scores on the screen
	           //updatePeerScoresDisplay();

	           // if it's a final score, mark this participant as having finished
	           // the game
	           if ((char) buf[0] == 'F') {
	               mFinishedParticipants.add(rtm.getSenderParticipantId());
	           }
	       }
	   }
	   public void onBackPressed(){
			 
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
	   
	   public boolean ifBottomWon(){
			  return (zeroTopRight.getVisibility() == View.VISIBLE && zeroTopLeft.getVisibility()==View.VISIBLE);
		  }
		  public boolean ifTopWon(){
			  return (zeroBottomRight.getVisibility() == View.VISIBLE && zeroBottomLeft.getVisibility()==View.VISIBLE);
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
			        	findViewById(R.id.topYT).setVisibility(View.GONE);
			        }
			        else if (!isBottomTurn){
			        	findViewById(R.id.topYT).setVisibility(View.VISIBLE);
			        	findViewById(R.id.bottomYT).setVisibility(View.GONE);
			        }
			        if(ifBottomWon()){
			        	findViewById(R.id.topYT).setVisibility(View.VISIBLE);
			        	findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
	        	    	bottomYT.setText("You Won");
			        	topYT.setText("You Lost");
	        	    }
			        else if(ifTopWon()){
			        	findViewById(R.id.topYT).setVisibility(View.VISIBLE);
			        	findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
	        	    	bottomYT.setText("You Lost");
			        	topYT.setText("You Won");
	        	    }
	        	    	
			      default:
			    	
			        break;
			      }
			      return true;
			    }
			  }
		    public void attackTopRight(View view1){
		    	isBottomTurn=!isBottomTurn;
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
			        	}
		    	}
		    	else if (zeroTopRight.getVisibility() == View.VISIBLE){
		    		isBottomTurn=!isBottomTurn;
		    	}
		    }
		    public void attackBottomRight(View view1){
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
			        	}

		    	}
		    	else if (zeroBottomRight.getVisibility() == View.VISIBLE){
		    		isBottomTurn=!isBottomTurn;
		    	}
			}
		    public void attackBottomLeft(View view1){
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
			        	}
		    	}
				 else if (zeroBottomLeft.getVisibility() == View.VISIBLE){
					 isBottomTurn=!isBottomTurn;
		    	}
		    }
		  
		    public void attackTopLeft(View view1){
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
			        	}
		  	}
				 else if (zeroTopLeft.getVisibility() == View.VISIBLE){
					 isBottomTurn=!isBottomTurn;
		  	}
		     
		    }
	   }

