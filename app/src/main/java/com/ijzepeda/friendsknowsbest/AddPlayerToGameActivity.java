package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ijzepeda.friendsknowsbest.models.UserVote;

import java.util.HashMap;
import java.util.Map;

import static com.ijzepeda.friendsknowsbest.Utils.CHILD_DECK_ID;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_GAMES;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_NAME;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_NO_CARDS;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_NO_USERS;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_USERS;
import static com.ijzepeda.friendsknowsbest.Utils.REF_DECK;
import static com.ijzepeda.friendsknowsbest.Utils.REF_GAME;
import static com.ijzepeda.friendsknowsbest.Utils.REF_USERS;

public class AddPlayerToGameActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener  {

    private static String TAG="AddPlayerToGameActivity";




    private GoogleApiClient mGoogleApiClient;
    TextView inviteTv;

    String deepLink ;
    String invitationId;
    String gameId;

    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    private DatabaseReference databaseUsersRef;
    private DatabaseReference databaseGameRef;
    private DatabaseReference databaseDeckRef;
    FirebaseUser firebaseUser;

    //MISC
    double newNoUsers,oldNoUsers;
    double noOfCards;
    Map<String,Object> userMap=new HashMap<String,Object>();
    Map<String,Object> cardMap=new HashMap<String,Object>();//Number of card in Deck
    Map<String,Object> collectionCardNoMap=new HashMap<String,Object>();//number of card within collection

    String deckId;
    String userName;
    String userUid;
    String userPic;

    int currentCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player_to_game);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));

        inviteTv=(TextView)findViewById(R.id.inviteResultTextView);

        app= FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseUsersRef=database.getReference(REF_USERS);
        databaseGameRef=database.getReference(REF_GAME);
        databaseDeckRef=database.getReference(REF_DECK);
        firebaseUser=auth.getCurrentUser();

        userName=firebaseUser.getDisplayName();
        userUid=firebaseUser.getUid();
        if(firebaseUser.getPhotoUrl()!=null){
            userPic=firebaseUser.getPhotoUrl().toString();
        }

        //GET INVITATION
        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();
        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    Intent intent = result.getInvitationIntent();
                                     deepLink = AppInviteReferral.getDeepLink(intent);
                                     invitationId = AppInviteReferral.getInvitationId(intent);

                                    String deeplinkbase=getString(R.string.deeplink_domain)+
                                            "?link="+
                                    getString(R.string.deeplink_link)+
                                            "&apn="+
                                            getString(R.string.deeplink_package)+
                                            "&amv=1"+
                                            "&afl="+
                                            getString(R.string.deeplink_not_installed_store_link)+
                                            "&al="+
                                            getString(R.string.deeplink_parse_url_game_id);



                                    Log.d(TAG, "getInvitation:deepLink:" + deepLink);//:http://ijzepeda.com/addGame/GAME123
                                    Log.d(TAG, "getInvitation:invitationId:" + invitationId);//963353948393-d5a521f4-b0e0-47d7-8f6e-986dbc76c348
                                    gameId=deepLink.replace(deeplinkbase,"");
                                    inviteTv.setText(getString(R.string.your_invite_to_join)+gameId);

//------------------------------------------------------------------------------------------------------------------
                                    // Alert and accept to add to game:
                                    // if user is already in the game take him directly to GameActivity(gameId)
                                    //If i dont block to add the same user again , his messages will be deleted [i guess]
                                    //verify on user.game, becaus ehe might want to be added again, if left
                                    databaseUsersRef.child(userUid).child(CHILD_GAMES).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(gameId).exists()) {
                                                addGame(gameId);

                                            }else{
                                                Toast.makeText(AddPlayerToGameActivity.this, R.string.you_are_in_that_game, Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(AddPlayerToGameActivity.this,LoadActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }
                        });



    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

    }

    Map<String,Object> playerOnCardMap=new HashMap<String,Object>();//number of card within collection



    //Reuse this method on NewGame
    public void addGame(String gameIDNoNeed){


        //Addgame to UsersDetails-----------------------
        Map<String,Object> gamesMap=new HashMap<String, Object>();
        gamesMap.put(gameId,gameId);
        databaseUsersRef.child(userUid).child(CHILD_GAMES).updateChildren(gamesMap);


databaseGameRef.child(gameId).child(CHILD_NO_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
         oldNoUsers=Double.parseDouble(dataSnapshot.getValue().toString());
        databaseGameRef.child(gameId).child(CHILD_USERS).setValue(oldNoUsers+1);

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});
        userMap.put(userUid,userName);

        databaseGameRef.child(gameId).child(CHILD_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                inviteTv.setText(getString(R.string.your_invite_to_join)+dataSnapshot.getValue());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        databaseGameRef.child(gameId).child(CHILD_USERS).updateChildren(userMap);
        userMap.clear();

        //get NoOfCards
        databaseGameRef.child(gameId).child(CHILD_NO_CARDS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noOfCards=Double.parseDouble(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //AddUser to Deck-------------------------------------------------
        //getDeckId from gameId
        databaseGameRef.child(gameId).child(CHILD_DECK_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCard=Integer.parseInt(dataSnapshot.getValue().toString());// added on 161016-1411
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseGameRef.child(gameId).child(CHILD_DECK_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deckId=dataSnapshot.getValue().toString();
                playerOnCardMap.put(userUid,userName);
                databaseDeckRef.child(deckId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for( int i =currentCard;i<noOfCards;i++) {
                           String cardNo = getString(R.string.card) + i;
                            cardMap.put(cardNo, ""); //esto agrega la cardNo y el numero que tiene. esto debe ir dentro de cardNo{card:#}
                            databaseDeckRef.child(deckId).child(cardNo).child(CHILD_USERS).updateChildren(playerOnCardMap);

                            //CREATE UserVote
                            Log.e("Adding PLAYER","Creating blank userVote deckId:"+deckId+", userUid:"+userUid+", userName:"+userName+", cardNo"+cardNo);

                              UserVote userVote = new UserVote(userName, userUid, userPic, "", false, "" + "", "","",false);
                            databaseDeckRef.child(deckId).child(cardNo).child(CHILD_USERS).child(userUid).setValue(userVote);

                            cardMap.clear();
//                            collectionCardNoMap.clear();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
     Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
