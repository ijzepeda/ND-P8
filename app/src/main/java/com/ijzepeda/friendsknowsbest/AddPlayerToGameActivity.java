package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.id.edit;

public class AddPlayerToGameActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener  {

    private static String TAG="AddPlayerToGameActivity";
    private GoogleApiClient mGoogleApiClient;
    TextView inviteTv;

    String deepLink ;
    String invitationId;
    String extraString;
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

    int currentCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player_to_game);
        inviteTv=(TextView)findViewById(R.id.inviteResultTextView);

        app= FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseUsersRef=database.getReference("Users");
        databaseGameRef=database.getReference("Game");
        databaseDeckRef=database.getReference("Deck");
        firebaseUser=auth.getCurrentUser();

        userName=firebaseUser.getDisplayName();
        userUid=firebaseUser.getUid();

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

                                    Log.e(TAG, "getInvitation:deepLink:" + deepLink);//:http://ijzepeda.com/addGame/GAME123
                                    Log.e(TAG, "getInvitation:invitationId:" + invitationId);//963353948393-d5a521f4-b0e0-47d7-8f6e-986dbc76c348
                                    extraString=""+intent.getStringExtra("prueba");
                                    inviteTv.setText("Your invite from:"+extraString);
gameId=deepLink.replace("http://ijzepeda.com/addGame/","");
                                    Log.e("~~~~AddGame","removing base I got gameid:"+gameId);

//TODO------------------------------------------------------------------------------------------------------------------
                                    //todo Alert and accept to add to game:
                                    //TODO if user is already in the game take him directly to GameActivity(gameId)
                                    //If i dont block to add the same user again , his messages will be deleted [i guess]
                                    //verify on user.game, becaus ehe might want to be added again, if left
                                    databaseUsersRef.child(userUid).child("games").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                            if (!dataSnapshot.child(gameId).exists()) {
//                                                addGame(gameId);
//
//                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    addGame(gameId);
//TODO------------------------------------------------------------------------------------------------------------------


                                    // Because autoLaunchDeepLink = true we don't have to do anything
                                    // here, but we could set that to false and manually choose
                                    // an Activity to launch to handle the deep link here.
                                    // ...
                                }
                            }
                        });



    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

    }

    Map<String,Object> playerOnCardMap=new HashMap<String,Object>();//number of card within collection

    public void addGame(String gameIDNoNeed){


        //Addgame to UsersDetails-----------------------
        Map<String,Object> gamesMap=new HashMap<String, Object>();
//        gamesMap.put("game"+1,gameID);
        gamesMap.put(gameId,gameId);
        databaseUsersRef.child(userUid).child("games").setValue(gamesMap);

        //Add user to Game------------------------------------------
        //Get whole GAME object, edit and reupload
//        Game gameObject=new Game(currentCard,deckId,name,noUsers,noCards,uid,unlimitedCounter,userMap);
//        databaseGameRef.updateChildren(map);
//        databaseGameRef.child(uid).push().setValue(gameObject);//Todo what is this for?!
//        databaseGameRef.child(uid).setValue(gameObject);

        //edit noUsers
//databaseGameRef.child(gameID).child("users").child(userUid)
//databaseGameRef.child(gameId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
databaseGameRef.child(gameId).child("noUsers").addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
         oldNoUsers=Double.parseDouble(dataSnapshot.getValue().toString());
        databaseGameRef.child(gameId).child("noUsers").setValue(oldNoUsers+1);

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});
        userMap.put(userUid,userName);

        databaseGameRef.child(gameId).child("users").updateChildren(userMap);
        userMap.clear();

        //get NoOfCards
        databaseGameRef.child(gameId).child("noCards").addListenerForSingleValueEvent(new ValueEventListener() {
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
        databaseGameRef.child(gameId).child("currentCard").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCard=Integer.parseInt(dataSnapshot.getValue().toString());//todo added on 161016-1411
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseGameRef.child(gameId).child("deckId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("getDeckId", dataSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
                Log.d("getDeckId", dataSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
                Log.d("getDeckId", dataSnapshot.getValue().toString()); //< Contains the whole json:.
                deckId=dataSnapshot.getValue().toString();
                playerOnCardMap.put(userUid,userName);
                databaseDeckRef.child(deckId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        currentGame.getCurrentCard()//todo: no empezar de 0 sino de current card, asi no hay error en cartas anteriores cuando no estaba el jugador
//                        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
//                            childSnapshot.child("users").updateChildren(playerOnCardMap);
//                        }
//                        for( int i =0;i<noOfCards;i++) {
                        for( int i =currentCard;i<noOfCards;i++) {
                           String cardNo = "card" + i;
                            cardMap.put(cardNo, ""); //esto agrega la cardNo y el numero que tiene. esto debe ir dentro de cardNo{card:#}
//                            databaseDeckRef.child(deckId).updateChildren(cardMap);
//                            collectionCardNoMap.put("card", randomCardOrder[i]);//NO NEED ~~DO NOT~~ tTO UPDATE THIS VALUE
//                            collectionCardNoMap.put("users", "");//create the tructure
//                            databaseDeckRef.child(deckId).child(cardNo).updateChildren(collectionCardNoMap);
                            databaseDeckRef.child(deckId).child(cardNo).child("users").updateChildren(playerOnCardMap);
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
//        playerOnCardMap.put(userUid,userName);
//        databaseDeckRef.child(deckId)








    }

}
