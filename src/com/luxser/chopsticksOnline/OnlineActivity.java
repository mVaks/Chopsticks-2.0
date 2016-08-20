package com.luxser.chopsticksOnline;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.Leaderboards.SubmitScoreResult;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;


/**
 * TBMPSkeleton: A minimalistic "game" that shows turn-based
 * multiplayer features for Play Games Services.  In this game, you
 * can invite a variable number of players and take turns editing a
 * shared state, which consists of single string.  You can also select
 * automatch players; all known players play before automatch slots
 * are filled.
 *
 * INSTRUCTIONS: To run this sample, please set up
 * a project in the Developer Console. Then, place your app ID on
 * res/values/ids.xml. Also, change the package name to the package name you
 * used to create the client ID in Developer Console. Make sure you sign the
 * APK with the certificate whose fingerprint you entered in Developer Console
 * when creating your Client Id.
 *
 * @author Wolff (wolff@google.com), 2013
 */
public class OnlineActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener,
        View.OnClickListener {

    public static final String TAG = "OnlineActivity";

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;
    public boolean justWent = false;
    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // Current turn-based match
    private TurnBasedMatch mTurnBasedMatch;
    public TurnBasedMatch cMatch = null;

    // Local convenience pointers
    public TextView mDataView;
    public TextView mTurnTextView;

    private AlertDialog mAlertDialog;

    // For our intents
    private static final int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_LOOK_AT_MATCHES = 10001;

    // Should I be showing the turn API?
    public boolean isDoingTurn = false;

    // This is the current match we're in; null if not loaded
    public TurnBasedMatch mMatch;

    // This is the current match data after being unpersisted.
    // Do not retain references to match data once you have
    // taken an action on the match, such as takeTurn()
    public SkeletonTurn mTurnData;
    final static int[] CLICKABLES = {
    	 R.id.button_tutorial,R.id.next_one, R.id.previous_two, R.id.next_two, R.id.previous_three, R.id.next_three,R.id.previous_four, R.id.next_four,R.id.previous_five, R.id.next_five,R.id.next_six, R.id.no,R.id.button_accept_popup_invitation, R.id.button_invite_players,
        R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
        R.id.button_sign_out, R.id.button_offline, R.id.button_single_player, R.id.button1, R.id.button_leaderboard
};
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
	 

    @Override
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
        setContentView(R.layout.turn);
        assignVars();
        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        createSaved("matchid", "");
        
        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
          findViewById(id).setOnClickListener(this);
        }
        setViewVisibility();
        //mDataView = ((TextView) findViewById(R.id.data_view));
        //mTurnTextView = ((TextView) findViewById(R.id.turn_counter_view));
    }
    

    @Override
    protected void onStart() {
        super.onStart();
        //Log.d(TAG, "onStart(): Connecting to Google APIs");
        //mGoogleApiClient.connect();
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(mSignInClicked)
    	mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
    	createSaved("matchid", "");
    	stopKeepingScreenOn();
        super.onStop();
        Log.d(TAG, "onStop(): Disconnecting from Google APIs");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
  

    @Override
    public void onConnected(Bundle connectionHint) {
    	
        Log.d(TAG, "onConnected(): Connection successful");
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);

        // Retrieve the TurnBasedMatch from the connectionHint
        if (connectionHint != null) {
            mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
            
            if (mTurnBasedMatch != null) {
                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }

                updateMatch(mTurnBasedMatch);
                return;
            }
        }

        setViewVisibility();

        // As a demonstration, we are registering this activity as a handler for
        // invitation and match events.

        // This is *NOT* required; if you do not register a handler for
        // invitation events, you will get standard notifications instead.
        // Standard notifications may be preferable behavior in many cases.
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        // Likewise, we are registering the optional MatchUpdateListener, which
        // will replace notifications you would get otherwise. You do *NOT* have
        // to register a MatchUpdateListener.
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);
    }
    public void createSaved(String name, String value){
    	PreferenceManager.getDefaultSharedPreferences(this).edit().putString(name, value).commit();  
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended():  Trying to reconnect.");
        mGoogleApiClient.connect();
        setViewVisibility();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            // Already resolving
            Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
            return;
        }

        // Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;

            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult, RC_SIGN_IN,
                    getString(R.string.signin_other_error));
        }

        setViewVisibility();
    }

    // Displays your inbox. You will get back onActivityResult where
    // you will need to figure out what you clicked on.
//    public void onCheckGamesClicked(View view) {
//        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
//        showSpinner();
//        startActivityForResult(intent, RC_LOOK_AT_MATCHES);
//    }
//
//    // Open the create-game UI. You will get back an onActivityResult
//    // and figure out what to do.
//    public void onStartMatchClicked(View view) {
//        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
//                1, 7, true);
//        startActivityForResult(intent, RC_SELECT_PLAYERS);
//    }
//
//    // Create a one-on-one automatch game.
//    public void onQuickMatchClicked(View view) {
//
//        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
//                1, 1, 0);
//
//        TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
//                .setAutoMatchCriteria(autoMatchCriteria).build();
//
//        showSpinner();
//
//        // Start the match
//        ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> cb = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
//            @Override
//            public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
//                processResult(result);
//            }
//        };
//        Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(cb);
//    }

    // In-game controls

    // Cancel the game. Should possibly wait until the game is canceled before
    // giving up on the view.
    public void onCancelClicked(View view) {
        showSpinner();
        if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
                        processResult(result);
                    }
                });
        isDoingTurn = false;
        setViewVisibility();
        dismissSpinner();
    }

    // Leave the game during your turn. Note that there is a separate
    // Games.TurnBasedMultiplayer.leaveMatch() if you want to leave NOT on your turn.
    public void onLeaveClicked(View view) {
        showSpinner();
        String nextParticipantId = getNextParticipantId();
        if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        Games.TurnBasedMultiplayer.leaveMatchDuringTurn(mGoogleApiClient, mMatch.getMatchId(),
                nextParticipantId).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
                processResult(result);
            }
        });
        setViewVisibility();
        dismissSpinner();
    }

    // Finish the game. Sometimes, this is your only choice.
    public void onFinish() {
        showSpinner();
        if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });

        isDoingTurn = false;
        setViewVisibility();
        dismissSpinner();
    }


    // Upload your new gamestate, then take a turn, and pass it on to the next
    // player.
//    public void onDoneClicked(View view) {
//        showSpinner();
//
//        String nextParticipantId = getNextParticipantId();
//        // Create the next turn
//        mTurnData.turnCounter += 1;
//        mTurnData.data = mDataView.getText().toString();
//
//        showSpinner();
//
//        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
//                mTurnData.persist(), nextParticipantId).setResultCallback(
//                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
//            @Override
//            public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
//                processResult(result);
//            }
//        });
//
//        mTurnData = null;
//    }

    // Sign-in, Sign out behavior

    // Update the visibility based on what state we're in.
    public void setViewVisibility() {
    
    	cMatch = mMatch;
        boolean isSignedIn = (mGoogleApiClient != null) && (mGoogleApiClient.isConnected());

        if (!isSignedIn) {
            findViewById(R.id.screen_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.button_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.screen_main).setVisibility(View.GONE);
            findViewById(R.id.screen_game).setVisibility(View.GONE);

            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            return;
        }


        //((TextView) findViewById(R.id.name_field)).setText(Games.Players.getCurrentPlayer(
           //     mGoogleApiClient).getDisplayName());
        findViewById(R.id.screen_sign_in).setVisibility(View.GONE);
        if(justWent){
        	
        }
        else if (isDoingTurn) {
            findViewById(R.id.screen_main).setVisibility(View.GONE);
            findViewById(R.id.screen_game).setVisibility(View.VISIBLE);
            
        }
        else if(notTurn){
        	 notTurn = false;
        	 findViewById(R.id.screen_main).setVisibility(View.GONE);
             findViewById(R.id.screen_game).setVisibility(View.VISIBLE);
        }
        else{
        	
        	
            findViewById(R.id.screen_main).setVisibility(View.VISIBLE);
            findViewById(R.id.screen_game).setVisibility(View.GONE);
        }
    }
    public ImageView[] hands;
    // Switch to gameplay view.
    public void viewsGone(){
    	for(ImageView views:hands){
    		views.setVisibility(View.GONE);
    	}
    }
    public void setGameplayUI() {
    	dismissSpinner();
    	justWent = false;
        isDoingTurn = true;
        assignVars();
        setViewVisibility();
        bottomYT.setText("Your Turn");
        Log.d(TAG, "bottomLeft:  " + mTurnData.bottomLeft);
        Log.d(TAG, "bottomRight:  " + mTurnData.bottomRight);
        Log.d(TAG, "topLeft:  " + mTurnData.topLeft);
        Log.d(TAG, "topRight:  " + mTurnData.topRight);
       
        viewsGone();
        switch(mTurnData.topLeft){
        	case 0: zeroTopLeft.setVisibility(View.VISIBLE);
        		break;
        	case 1:	oneTopLeft.setVisibility(View.VISIBLE);
        		break;
        	case 2: twoTopLeft.setVisibility(View.VISIBLE);
        		break; 
        	case 3: threeTopLeft.setVisibility(View.VISIBLE);
        		break;
        	case 4: fourTopLeft.setVisibility(View.VISIBLE);
        		break;
        }
        switch(mTurnData.topRight){
        	case 0: zeroTopRight.setVisibility(View.VISIBLE);
        		break;
        	case 1:	oneTopRight.setVisibility(View.VISIBLE);
        		break;
        	case 2: twoTopRight.setVisibility(View.VISIBLE);
        		break;
        	case 3: threeTopRight.setVisibility(View.VISIBLE);
        		break;
        	case 4: fourTopRight.setVisibility(View.VISIBLE);
        		break;
        }
        switch(mTurnData.bottomLeft){
        	case 0: zeroBottomLeft.setVisibility(View.VISIBLE);
        		break;
        	case 1:	oneBottomLeft.setVisibility(View.VISIBLE);
        		break;
        	case 2: twoBottomLeft.setVisibility(View.VISIBLE);
        		break;
        	case 3: threeBottomLeft.setVisibility(View.VISIBLE);
        		break;
        	case 4: fourBottomLeft.setVisibility(View.VISIBLE);
        		break;
        }
        switch(mTurnData.bottomRight){
        	case 0: zeroBottomRight.setVisibility(View.VISIBLE);
        		break;
        	case 1:	oneBottomRight.setVisibility(View.VISIBLE);
        		break;
        	case 2: twoBottomRight.setVisibility(View.VISIBLE);
        		break;
        	case 3: threeBottomRight.setVisibility(View.VISIBLE);
        		break;
        	case 4: fourBottomRight.setVisibility(View.VISIBLE);
        		break;
        }
        if(ifTopWon()){
        	findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
	    	bottomYT.setText("You Lost");
        }
       
        //mDataView.setText(mTurnData.data);
        //mTurnTextView.setText("Turn " + mTurnData.turnCounter);
    }

    // Helpful dialogs

    public void showSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
    }

    public void dismissSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.GONE);
    }

    // Generic warning/info dialog
    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });

        // create alert dialog
        mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }

    // Rematch dialog
    public void askForRematch() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("Do you want a rematch?");

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Sure, rematch!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                rematch();
                            }
                        })
                .setNegativeButton("No.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            	findViewById(R.id.screen_main).setVisibility(View.VISIBLE);
                                findViewById(R.id.screen_game).setVisibility(View.GONE);
                            }
                        });

        alertDialogBuilder.show();
    }

    // This function is what gets called when you return from either the Play
    // Games built-in inbox, or else the create game built-in interface.
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        //just returned from displaying the leaderboard
        if(request==1337)
            {
        	findViewById(R.id.screen_main).setVisibility(View.VISIBLE);
            return;
            }
        if (request == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (response == Activity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, request, response, R.string.signin_other_error);
            }
        } else if (request == RC_LOOK_AT_MATCHES) {
            // Returning from the 'Select Match' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            TurnBasedMatch match = data
                    .getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) {
                updateMatch(match);
            }

            Log.d(TAG, "Match = " + match);
        } else if (request == RC_SELECT_PLAYERS) {
            // Returned from 'Select players to Invite' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            final ArrayList<String> invitees = data
                    .getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get automatch criteria
            Bundle autoMatchCriteria = null;

            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                		1, 1, 0);
            } else {
                autoMatchCriteria = null;
            }
            keepScreenOn();
            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria).build();
            if(!mGoogleApiClient.isConnected()){
        		mGoogleApiClient.reconnect();
        	}
            // Start the match
            Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                    processResult(result);
                }
            });
            showSpinner();
        }
    }

    // startMatch() happens in response to the createTurnBasedMatch()
    // above. This is only called on success, so we should have a
    // valid match object. We're taking this opportunity to setup the
    // game, saving our initial state. Calling takeTurn() will
    // callback to OnTurnBasedMatchUpdated(), which will show the game
    // UI.
    public void startMatch(TurnBasedMatch match) {
    	justWent = false;
        mTurnData = new SkeletonTurn();
        // Some basic turn data
        mTurnData.topLeft = 1;
        mTurnData.topRight = 1;
        mTurnData.bottomLeft = 1;
        mTurnData.bottomRight = 1;

        mMatch = match;
        if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        showSpinner();
        
        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
                mTurnData.persist(), myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
        
   	
     		  
     	
   	
    }

		
    // If you choose to rematch, then call it and wait for a response.
    public void rematch() {
        showSpinner();
        if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                processResult(result);
            }
        });
        mMatch = null;
        isDoingTurn = false;
    }

    /**
     * Get the next participant. In this function, we assume that we are
     * round-robin, with all known players going before all automatch players.
     * This is not a requirement; players can go in any order. However, you can
     * take turns in any order.
     *
     * @return participantId of next player, or null if automatching
     */
    public String getNextParticipantId() {
        if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        ArrayList<String> participantIds = mMatch.getParticipantIds();

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (mMatch.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of automatch slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully automatched, so null will find a new
            // person to play against.
            return null;
        }
    }

    // This is the main function that gets called when players choose a match
    // from the inbox, or else create a match and want to start it.
    public void updateMatch(TurnBasedMatch match) {
        mMatch = match;
        createSaved("matchid", mMatch.getMatchId());
        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();
       
        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    showWarning(
                            "Complete!",
                            "This game is over; someone finished it, and so did you!  There is nothing to be done.");
                    findViewById(R.id.screen_main).setVisibility(View.VISIBLE);
                    findViewById(R.id.screen_game).setVisibility(View.GONE);	
                    break;
                }

                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
                showWarning("Complete!",
                        "This game is over; someone finished it!  You can only finish it now.");
                findViewById(R.id.screen_main).setVisibility(View.VISIBLE);
                findViewById(R.id.screen_game).setVisibility(View.GONE);
        }
       

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = SkeletonTurn.unpersist(mMatch.getData());
                setGameplayUI();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
//            	mTurnData = SkeletonTurn.unpersist(mMatch.getData());
//                setGameplayUINotTurn();
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Good inititative!",
                        "Still waiting for invitations.\n\nBe patient!");
        }

        mTurnData = null;

        setViewVisibility();
    }
    public boolean notTurn;
    private void setGameplayUINotTurn() {
    	 dismissSpinner();
    	 assignVarsNotTurn();
    	 notTurn = true;
    	 setViewVisibility();
         viewsGone();
         switch(mTurnData.topLeft){
         	case 0: zeroTopLeft.setVisibility(View.VISIBLE);
         		break;
         	case 1:	oneTopLeft.setVisibility(View.VISIBLE);
         		break;
         	case 2: twoTopLeft.setVisibility(View.VISIBLE);
         		break; 
         	case 3: threeTopLeft.setVisibility(View.VISIBLE);
         		break;
         	case 4: fourTopLeft.setVisibility(View.VISIBLE);
         		break;
         }
         switch(mTurnData.topRight){
         	case 0: zeroTopRight.setVisibility(View.VISIBLE);
         		break;
         	case 1:	oneTopRight.setVisibility(View.VISIBLE);
         		break;
         	case 2: twoTopRight.setVisibility(View.VISIBLE);
         		break;
         	case 3: threeTopRight.setVisibility(View.VISIBLE);
         		break;
         	case 4: fourTopRight.setVisibility(View.VISIBLE);
         		break;
         }
         switch(mTurnData.bottomLeft){
         	case 0: zeroBottomLeft.setVisibility(View.VISIBLE);
         		break;
         	case 1:	oneBottomLeft.setVisibility(View.VISIBLE);
         		break;
         	case 2: twoBottomLeft.setVisibility(View.VISIBLE);
         		break;
         	case 3: threeBottomLeft.setVisibility(View.VISIBLE);
         		break;
         	case 4: fourBottomLeft.setVisibility(View.VISIBLE);
         		break;
         }
         switch(mTurnData.bottomRight){
         	case 0: zeroBottomRight.setVisibility(View.VISIBLE);
         		break;
         	case 1:	oneBottomRight.setVisibility(View.VISIBLE);
         		break;
         	case 2: twoBottomRight.setVisibility(View.VISIBLE);
         		break;
         	case 3: threeBottomRight.setVisibility(View.VISIBLE);
         		break;
         	case 4: fourBottomRight.setVisibility(View.VISIBLE);
         		break;
         }

	}

	private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
        dismissSpinner();

        if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = false;

        showWarning("Match",
                "This match is canceled.  All other players will have their game ended.");
    }

    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
    	if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();

        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }

        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }

        startMatch(match);
    }


    private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        
        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
        justWent = false;
        showWarning("Left", "You've left this match.");
    }


    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
    	if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        TurnBasedMatch match = result.getMatch();
        createSaved("matchid", match.getMatchId());
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        if (match.canRematch()) {
            askForRematch();
        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

        setViewVisibility();
    }
    
    String mIncomingInvitationId = null;
    // Handle notification events.
    @Override
    public void onInvitationReceived(Invitation invitation) {
    	if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
        Toast.makeText(
                this,
                "An invitation has arrived from "
                        + invitation.getInviter().getDisplayName(), Toast.LENGTH_SHORT)
                .show();
        	boolean showInvPopup;
	       if (mIncomingInvitationId == null) {
	           // no invitation, so no popup
	           showInvPopup = false;
	       } else if (isDoingTurn) {
	           // if in multiplayer, only show invitation on main screen
	    	   
	           showInvPopup = (findViewById(R.id.screen_main).getVisibility()) == View.VISIBLE;
	       } else {
	           // single-player: show on main screen and gameplay screen
	           showInvPopup = ( ((findViewById(R.id.screen_main).getVisibility()) == View.VISIBLE) || ((findViewById(R.id.screen_game).getVisibility()) == View.VISIBLE) );
	       }
	       LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       View view = inflater.inflate(R.layout.turn, null); //custom_layout is your xml file which contains popuplayout
	       LinearLayout layout = (LinearLayout) view.findViewById(R.id.invitation_popup);
	       if(showInvPopup)
	    	   layout.setVisibility(View.VISIBLE);
	       else
	    	   layout.setVisibility(View.GONE);
	       //findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
    	 mIncomingInvitationId = null;
        Toast.makeText(this, "An invitation was removed.", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch match) {
    	if(!mGoogleApiClient.isConnected()){
    		mGoogleApiClient.reconnect();
    	}
       

    	String matchid = PreferenceManager.getDefaultSharedPreferences(this).getString("matchid", "defaultStringIfNothingFound"); 
      Log.d(TAG, "Trying to update: " + (match.getMatchId().equals(matchid)) + "Current id: " + (matchid) + "Other: " + (match.getMatchId()));
        if(match.getMatchId().equals(matchid)){
        	Toast.makeText(this, "This match was updated.", Toast.LENGTH_SHORT).show();
        	updateMatch(match);	
       }
        else{
        	 Toast.makeText(this, "A match was updated.", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    

    @Override
    public void onTurnBasedMatchRemoved(String matchId) {
        Toast.makeText(this, "A match was removed.", Toast.LENGTH_SHORT).show();

    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode,
            int stringId) {

        showWarning("Warning", getResources().getString(stringId));
    }

    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(
                        this,
                        "Stored action for later.  (Please remove this toast before release.)",
                        Toast.LENGTH_SHORT).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                //showErrorMessage(match, statusCode,
                     //   R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
            	mGoogleApiClient.reconnect();
            	showErrorMessage(match, statusCode,
                       R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(TAG, "Did not have warning or string to deal with: "
                        + statusCode);
        }

        return false;
    }

    @Override
    public void onClick(View v) {
    	Intent intent;
        switch (v.getId()) {
        	case R.id.button_single_player:
        		intent =  new Intent(OnlineActivity.this, SinglePlayerActivity.class);
        		startActivity(intent);
        		break;
        	case R.id.button_offline:
        		intent = new Intent(OnlineActivity.this, MainActivity.class);
        		startActivity(intent);
        		break;
            case R.id.button_sign_in:
                // Check to see the developer who's running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
                }

                mSignInClicked = true;
                mTurnBasedMatch = null;
                mGoogleApiClient.connect();
                break;
            case R.id.button_sign_out:
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                setViewVisibility();
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
            	startMatch(mMatch);
                mIncomingInvitationId = null;
                break;
            case R.id.button_invite_players:
            	if(!mGoogleApiClient.isConnected()){
            		mGoogleApiClient.reconnect();
            	}
            	 intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
                        1, 1, true);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
            	break;
            case R.id.button_see_invitations:
            	if(!mGoogleApiClient.isConnected()){
            		mGoogleApiClient.reconnect();
            	}
            	 intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
                // showSpinner();
                 startActivityForResult(intent, RC_LOOK_AT_MATCHES);
            	break;
            case R.id.button_quick_game:
            	keepScreenOn();
            	Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        1, 1, 0);

                TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                        .setAutoMatchCriteria(autoMatchCriteria).build();

                showSpinner();
                if(!mGoogleApiClient.isConnected()){
            		mGoogleApiClient.reconnect();
            	}
                // Start the match
                ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> cb = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                        processResult(result);
                    }
                };
                Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(cb);
            	break;
            case R.id.button1:
            	stopKeepingScreenOn();
            		createSaved("matchid", "");
            		justWent = false;
            		findViewById(R.id.screen_main).setVisibility(View.VISIBLE);
                    findViewById(R.id.screen_game).setVisibility(View.GONE); 
            	break;
            case R.id.button_leaderboard:
            	if(!mGoogleApiClient.isConnected()){
            		mGoogleApiClient.reconnect();
            	}
            	findViewById(R.id.screen_main).setVisibility(View.GONE);
            	startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
            			 getString(R.string.LEADERBOARD_ID)), 1337);
            	break;
            case R.id.button_tutorial:
            	findViewById(R.id.tutorial_one).setVisibility(View.VISIBLE);
        		findViewById(R.id.screen_sign_in).setVisibility(View.GONE);
            	break;
            case R.id.next_one:
            	findViewById(R.id.tutorial_one).setVisibility(View.GONE);
        		findViewById(R.id.tutorial_two).setVisibility(View.VISIBLE);
            	break;
            case R.id.next_two:
            	findViewById(R.id.tutorial_two).setVisibility(View.GONE);
        		findViewById(R.id.tutorial_three).setVisibility(View.VISIBLE);
            	break;
            case R.id.previous_two:
            	findViewById(R.id.tutorial_one).setVisibility(View.VISIBLE);
        		findViewById(R.id.tutorial_two).setVisibility(View.GONE);
            	break;
            case R.id.next_three:
            	findViewById(R.id.tutorial_three).setVisibility(View.GONE);
        		findViewById(R.id.tutorial_four).setVisibility(View.VISIBLE);
            	break;
            case R.id.previous_three:
            	findViewById(R.id.tutorial_two).setVisibility(View.VISIBLE);
        		findViewById(R.id.tutorial_three).setVisibility(View.GONE);
            	break;
            case R.id.next_four:
            	findViewById(R.id.tutorial_four).setVisibility(View.GONE);
        		findViewById(R.id.tutorial_five).setVisibility(View.VISIBLE);
            	break;
            case R.id.previous_four:
            	findViewById(R.id.tutorial_three).setVisibility(View.VISIBLE);
        		findViewById(R.id.tutorial_four).setVisibility(View.GONE);
            	break;
            case R.id.next_five:
            	findViewById(R.id.tutorial_five).setVisibility(View.GONE);
        		findViewById(R.id.tutorial_six).setVisibility(View.VISIBLE);
            	break;
            case R.id.previous_five:
            	findViewById(R.id.tutorial_four).setVisibility(View.VISIBLE);
        		findViewById(R.id.tutorial_five).setVisibility(View.GONE);
            	break;
            case R.id.next_six:
            	intent =  new Intent(OnlineActivity.this, SinglePlayerActivity.class);
        		startActivity(intent);
            	break;
            case R.id.no:
            	findViewById(R.id.tutorial_six).setVisibility(View.GONE);
        		findViewById(R.id.screen_sign_in).setVisibility(View.VISIBLE);
            	break;
            	
            	
            	
             }

             
    }
    
    public void onBackPressed(){
    	if(findViewById(R.id.tutorial_one).getVisibility() == View.VISIBLE){
    		findViewById(R.id.tutorial_one).setVisibility(View.GONE);
    		findViewById(R.id.screen_sign_in).setVisibility(View.VISIBLE);
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
	    public boolean dontSwitchTop = false;
	    public boolean dontSwitchTopRight = false;
	    public boolean dontSwitchTopLeft = false;
	    public boolean went = false;
	    class MyDragListener implements OnDragListener {
	    	
		    //Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
		   // Drawable normalShape = getResources().getDrawable(R.drawable.shape);

		    @Override
		    public boolean onDrag(View v, DragEvent event) {
		    	went = false;
		    	dontSwitchTop = false;
		    	dontSwitchTopRight = false;
		    	dontSwitchTopLeft = false;
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
		        		went = true;
		        		dontSwitchTop = true;
		        	}
		        }
		        else if (!isBottomTurn&&((container == topRight||container==topLeft) && (view1 == oneTopRight || view1 == twoTopRight || view1 == threeTopRight || view1 == fourTopRight || view1 == zeroTopRight || view1 == oneTopLeft || view1 == twoTopLeft || view1 == threeTopLeft || view1 == fourTopLeft || view1 == zeroTopLeft))){
		        	if (splitTop(view1,container)){
		        		isBottomTurn=!isBottomTurn;
		        		went = true;
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
		        	dontSwitchTopLeft = true;
		        	if(!isBottomTurn)
		        	went = true;
			    }
		    	else if(isBottomTurn && container == topLeft){
		    		attackTopLeft(view1);
		    		dontSwitchTopRight = true;
		    		if(!isBottomTurn)
		    		went = true;
			    }
		    	else if (!isBottomTurn && container == bottomRight){
		    		attackBottomRight(view1);
		    		if(!isBottomTurn)
		    		went = true;
		    	}
		    		
		    	else if (!isBottomTurn && container == bottomLeft){
		    	attackBottomLeft(view1);
		    	if(!isBottomTurn)
		    	went = true;
		    	}
		        		findInvisible();
		        		if(went){
		        			turnFinished();
		        		}
		    	
		      
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
		        	//findViewById(R.id.topYT).setVisibility(View.GONE);
		        }
		        else if (!isBottomTurn){
		        	//findViewById(R.id.topYT).setVisibility(View.VISIBLE);
		        	//findViewById(R.id.bottomYT).setVisibility(View.GONE);
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
	        		   isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
		        	}
	    	}
	    	else if (zeroTopRight.getVisibility() == View.VISIBLE){
	    		isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
		        	}

	    	}
	    	else if (zeroBottomRight.getVisibility() == View.VISIBLE){
	    		isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
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
	    			isBottomTurn=true;
		        	}
	    	}
			 else if (zeroBottomLeft.getVisibility() == View.VISIBLE){
				 isBottomTurn=true;
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
	  			isBottomTurn=true;
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
	  			isBottomTurn=true;
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
	  			isBottomTurn=true;
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
	  			isBottomTurn=true;
		        	}
	  	}
			 else if (zeroTopLeft.getVisibility() == View.VISIBLE){
				 isBottomTurn=true;
	  	}
	    }
	    public void turnFinished(){
	    	 for(ImageView views:hands){
	        		views.setOnTouchListener(null);
	    	 }
	    	 justWent = true;
	    	 showSpinner();

	         String nextParticipantId = getNextParticipantId();
	         // Create the next turn
	         mTurnData.turnCounter += 1;
	         //mTurnData.data = mDataView.getText().toString();
	        
	        
	         updatePositions();
	         Log.d(TAG, "bottomLeft:  " + mTurnData.bottomLeft);
	         Log.d(TAG, "bottomRight:  " + mTurnData.bottomRight);
	         Log.d(TAG, "topLeft:  " + mTurnData.topLeft);
	         Log.d(TAG, "topRight:  " + mTurnData.topRight);
	        
	         showSpinner();
	         if(!mGoogleApiClient.isConnected()){
	     		mGoogleApiClient.reconnect();
	     	}
	         if(ifBottomWon()){
		        	findViewById(R.id.bottomYT).setVisibility(View.VISIBLE);
     	    	bottomYT.setText("You Won");
     	    		int temp = PreferenceManager.getDefaultSharedPreferences(this).getInt("myWins", 0);
		        	createSaved("myWins",temp + 1); 
		        	Games.Leaderboards.submitScoreImmediate(mGoogleApiClient, getString(R.string.LEADERBOARD_ID), PreferenceManager.getDefaultSharedPreferences(this).getInt("myWins", 0)).setResultCallback(new myLeaderBoardSubmitScoreCallback());
		        	Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient,mMatch.getMatchId()).setResultCallback(
			                 new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
			    	             @Override
			    	             public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
			    	                 processResult(result);
			    	             }
			    	         });
		      }
	         else{
	         Log.d(TAG,"Trying to update: " + mMatch.getMatchId());
	         createSaved("matchid", mMatch.getMatchId());
	         Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
	                 mTurnData.persist(), nextParticipantId).setResultCallback(
	                 new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
	             @Override
	             public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
	                 processResult(result);
	             }
	         });
		        
		       
	         bottomYT.setText("Waiting...");
	         showSpinner();
	         mTurnData = null;
	         }
		        
	    }
	    
	    public void createSaved(String name, int value){
	       	PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(name, value).commit();  
	       }
	    public void updatePositions(){
	    	int bottomRight = 0;
	    	int bottomLeft = 0;
	    	int topRight = 0;
	    	int topLeft = 0;
	    	if (dontSwitchTop){
	    		bottomRight = mTurnData.topRight;
	    		bottomLeft = mTurnData.topLeft;
	    	}
	    	else if(dontSwitchTopRight){
	    		 bottomRight = mTurnData.topRight;
	    		 if(zeroTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 0;
		         }
		         else if(oneTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 1;
		         }	
		         else if(twoTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 2;
		         }
		         else if(threeTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 3;
		         }
		         else if(fourTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 4;
		         }
	    	 }
	    	 else if(dontSwitchTopLeft){
	    		 bottomLeft = mTurnData.topLeft;
	    		 if(zeroTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 0;
		         }
		         else if(oneTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 1;
		         }	
		         else if(twoTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 2;
		         }
		         else if(threeTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 3;
		         }
		         else if(fourTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 4;
		         }
	    	 }
	    	 else{
	    		 if(zeroTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 0;
		         }
		         else if(oneTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 1;
		         }	
		         else if(twoTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 2;
		         }
		         else if(threeTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 3;
		         }
		         else if(fourTopLeft.getVisibility() == View.VISIBLE){
		        	 bottomLeft = 4;
		         }
	    		 if(zeroTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 0;
		         }
		         else if(oneTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 1;
		         }	
		         else if(twoTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 2;
		         }
		         else if(threeTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 3;
		         }
		         else if(fourTopRight.getVisibility() == View.VISIBLE){
		        	 bottomRight = 4;
		         }
	    	 }
	    	
	       
	         
	         if(zeroBottomRight.getVisibility() == View.VISIBLE){
	        	 topRight = 0;	
	         }
	         else if(oneBottomRight.getVisibility() == View.VISIBLE){
	        	 topRight = 1;
	         }	
	         else if(twoBottomRight.getVisibility() == View.VISIBLE){
	        	 topRight = 2;
	         }
	         else if(threeBottomRight.getVisibility() == View.VISIBLE){
	        	 topRight = 3;
	         }
	         else if(fourBottomRight.getVisibility() == View.VISIBLE){
	        	 topRight = 4;
	         }
	         
	         if(zeroBottomLeft.getVisibility() == View.VISIBLE){
	        	 topLeft = 0;
	         }
	         else if(oneBottomLeft.getVisibility() == View.VISIBLE){
	        	 topLeft = 1;
	         }	
	         else if(twoBottomLeft.getVisibility() == View.VISIBLE){
	        	 topLeft = 2;
	         }
	         else if(threeBottomLeft.getVisibility() == View.VISIBLE){
	        	 topLeft = 3;
	         }
	         else if(fourBottomLeft.getVisibility() == View.VISIBLE){
	        	 topLeft = 4;
	         }
	         mTurnData.bottomLeft = bottomLeft;
	         mTurnData.bottomRight = bottomRight;
	         mTurnData.topRight = topRight;
	         mTurnData.topLeft = topLeft;
	    }
	    public void assignVarsNotTurn(){
	    	
	    	  	 
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
	    }
   public void assignVars(){
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
    	  hands = new ImageView[]{oneTopLeft,twoTopLeft, threeTopLeft,fourTopLeft,zeroTopLeft,
    	    		oneTopRight,twoTopRight,threeTopRight,fourTopRight,zeroTopRight,
    	    		oneBottomLeft,twoBottomLeft,threeBottomLeft,fourBottomLeft,zeroBottomLeft,
    	    		oneBottomRight,twoBottomRight,threeBottomRight,fourBottomRight,zeroBottomRight};
    	  for(ImageView views:hands){
        		views.setOnTouchListener(new MyTouchListener());
        	  }
      
      
      topLeft.setOnDragListener(new MyDragListener());
      topRight.setOnDragListener(new MyDragListener());
      bottomLeft.setOnDragListener(new MyDragListener());
      bottomRight.setOnDragListener(new MyDragListener());
      
      bottomYT = (TextView) findViewById(R.id.bottomYT);
  	//topYT = (TextView) findViewById(R.id.topYT);

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
   class myLeaderBoardSubmitScoreCallback implements ResultCallback<SubmitScoreResult> {
	    @Override
	    public void onResult(SubmitScoreResult res) {
	        if (res.getStatus().getStatusCode() == 0) {
	            // data sent successfully to server.
	            // display toast.
	            Toast.makeText(OnlineActivity.this, "A score was updated in the Leaderboard.", Toast.LENGTH_SHORT).show();
	        }
	    }
	}
}
