package com.luxser.chopsticks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
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
	       mMultiplayer = multiplayer;
	       //updateScoreDisplay();
	       //broadcastScore(false);
	       switchToScreen(R.id.screen_game);

	       //findViewById(R.id.button_click_me).setVisibility(View.VISIBLE);

	       // run the gameTick() method every second to update the game.
	       final Handler h = new Handler();
	       h.postDelayed(new Runnable() {
	           @Override
	           public void run() {
	              // if (mSecondsLeft <= 0)
	                //   return;
	               gameTick();
	               h.postDelayed(this, 1000);
	           }
	       }, 1000);
	   }
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

	   }

