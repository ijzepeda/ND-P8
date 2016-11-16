package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ijzepeda.friendsknowsbest.models.Game;
import com.ijzepeda.friendsknowsbest.models.User;
import com.ijzepeda.friendsknowsbest.models.UserVote;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ijzepeda.friendsknowsbest.Utils.CHILD_CARD;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_GAMES;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_USERS;
import static com.ijzepeda.friendsknowsbest.Utils.DEEPLINK_LOGO;
import static com.ijzepeda.friendsknowsbest.Utils.REF_DECK;
import static com.ijzepeda.friendsknowsbest.Utils.REF_GAME;
import static com.ijzepeda.friendsknowsbest.Utils.REF_USERS;
import static com.ijzepeda.friendsknowsbest.Utils.SHARED_EMAIL;
import static com.ijzepeda.friendsknowsbest.Utils.SHARED_UID;
import static com.ijzepeda.friendsknowsbest.Utils.SHARED_USERNAME;

public class NewGame extends AppCompatActivity  implements
        GoogleApiClient.OnConnectionFailedListener {
    private static String TAG="NewGameActivity";
    private static final int REQUEST_INVITE = 1;

    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseGameRef;
    private DatabaseReference databaseDeckRef;
    private DatabaseReference databaseUserRef;

    //users
    String userUid;
    String userName;
    String userEmail;
    String userPic;
    User user;

    //GameElements
    static String UID_GAME_CONSTRUCT_PATH="GAME_";
    int currentCard=0;
    String deckId="deck00";
    String name;
    int noUsers=1;
    int noCards;
    String uid="";
    boolean unlimitedCounter=true;
    Map<String,Object> userMap=new HashMap<String,Object>();

ImageButton nextBtn;
Button inviteBtn;
    TextView gameNameTV, noCardsTV;

    //Firebase
private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));
//        create a game object, and invite with the gameobject
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();

        app= FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseGameRef =database.getReference(REF_GAME);
        databaseDeckRef =database.getReference(REF_DECK);
        databaseUserRef =database.getReference(REF_USERS);

        userUid=Utils.getInstance().getValue(getApplication(),SHARED_UID);
        userName=auth.getCurrentUser().getDisplayName();
        if(userName==null || userName.equals(null) || userName.equals("")){
            userName=""+Utils.getInstance().getValue(getApplication(),SHARED_USERNAME);
        }

        userEmail=Utils.getInstance().getValue(getApplication(),SHARED_EMAIL);

        if(auth.getCurrentUser().getPhotoUrl()!=null) {
//    LOAD FROM USERS DB or sharedprefs
    userPic =Utils.getInstance().getValue(getApplication(),getResources().getString(R.string.shared_userphotourl_key));

}

//Generate the random UID
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime());
int randomSeed=(int)(Math.random());
        deckId=deckId+timeStamp+"_"+randomSeed;
        uid=UID_GAME_CONSTRUCT_PATH+timeStamp+"_"+randomSeed;
//        deckId=deckId+"123";
//        uid="GAME123";


        gameNameTV=(TextView)findViewById(R.id.gameNameTextView);
        noCardsTV=(TextView)findViewById(R.id.numberCardsTextView);
        nextBtn=(ImageButton)findViewById(R.id.nextInviteBtn);
        inviteBtn=(Button)findViewById(R.id.inviteFriendsBtn);
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              String gameId=  createGame();
                sendInvitation(gameId);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDeck();
                Intent TempIntent= new Intent(NewGame.this, LoadActivity.class);
                startActivity(TempIntent);
                finish();
                return;
            }
        });


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

    }
public void createDeck(){
    //i was about to put the card text in the back end, BUT
    //i need them for offline
    //also if users have different language, they only will read what is online

// steps
    //generate deckID  - unique value-> gameID?
//get length of chosen array: category_romantic so far
    //create a MAP within a FOR add the cards number, for order [so RANDOM]
    //inside create another for// or at least another map// to create userVotes
    //Maybe userVotes, get created ONCE in the game. per card, for all users

    //CREATE DECKID IN DATABASE
    Map<String,Object> map=new HashMap<String,Object>();
    int i=123;
    map.put(deckId,"");
    databaseDeckRef.updateChildren(map);

   //POPULATE DECK
    Map<String,Object> cardMap=new HashMap<String,Object>();//Number of card in Deck
    Map<String,Object> collectionCardNoMap=new HashMap<String,Object>();//number of card within collection
    Map<String,Object> playerOnCardMap=new HashMap<String,Object>();//number of card within collection
    String [] cards=getResources().getStringArray(R.array.category_romantic);// Verifica esto!
    int noOfCardsInArray=cards.length;

    Integer[] randomCardOrder = new Integer[noOfCardsInArray];
    for ( i = 0; i < randomCardOrder.length; i++) {
        randomCardOrder[i] = i;
    }
    Collections.shuffle(Arrays.asList(randomCardOrder));

//OnlineCard

     String cardNo;
//    playerOnCardMap.put(userName,userEmail);

//    playerOnCardMap.put(userUid,userName);// commented on 19-10:1116
    playerOnCardMap.put(userUid,userUid);
//    for( i =0;i<noOfCardsInArray;i++) {
    for( i =0;i<noCards;i++) {
         cardNo = CHILD_CARD + i;
         cardMap.put(cardNo, ""); //esto agrega la cardNo y el numero que tiene. esto debe ir dentro de cardNo{card:#}
         databaseDeckRef.child(deckId).updateChildren(cardMap);
         collectionCardNoMap.put(CHILD_CARD, randomCardOrder[i]);
         collectionCardNoMap.put(CHILD_USERS, "");
         databaseDeckRef.child(deckId).child(cardNo).updateChildren(collectionCardNoMap);
         databaseDeckRef.child(deckId).child(cardNo).child(CHILD_USERS).updateChildren(playerOnCardMap);
        //CREATE UserVote
        Log.e("Adding PLAYER","Creating blank userVote deckId:"+deckId+", userUid:"+userUid+", userName:"+userName+", cardNo"+cardNo+", userPic"+userPic);
        UserVote userVote = new UserVote(userName, userUid, userPic, "", false, "" + "", "","",false);//"nomineePicUrl"
        databaseDeckRef.child(deckId).child(cardNo).child(CHILD_USERS).child(userUid).setValue(userVote);


        cardMap.clear();
         collectionCardNoMap.clear();
     }
}


    public String createGame(){
name=gameNameTV.getText().toString();
        if(Integer.parseInt(noCardsTV.getText().toString())>0) {
            noCards = Integer.parseInt(noCardsTV.getText().toString());
             if(noCards>(getResources().getStringArray(R.array.category_romantic)).length){
                noCards=(getResources().getStringArray(R.array.category_romantic)).length;
            }

        }else{
            noCards=(getResources().getStringArray(R.array.category_romantic)).length;
        } userMap.put(userUid,userName);
     Map<String,Object> map=new HashMap<String,Object>();
        map.put(uid,"");
        Game gameObject=new Game(currentCard,deckId,name,noUsers,noCards,uid,unlimitedCounter,userMap);
       databaseGameRef.updateChildren(map);
databaseGameRef.child(uid).push().setValue(gameObject);
databaseGameRef.child(uid).setValue(gameObject);
        userMap.clear();
 addGameToUser();// maybe just this do the job: databaseUserRef.child(userUid).child("games").updateChildren(gamesMap);
return uid;
    }


    private void addGameToUser(){
        databaseUserRef.child(userUid).child(CHILD_GAMES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(uid).exists()) {
                    Map<String,Object> gamesMap=new HashMap<String, Object>();
                    gamesMap.put(uid,uid);
                    databaseUserRef.child(userUid).child(CHILD_GAMES).updateChildren(gamesMap);
                }else{
                    Toast.makeText(NewGame.this, getString(R.string.you_are_in_that_game), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NewGame.this,LoadActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void sendInvitation(String gameid){
        //Remember : to have the correct Json for the testing/release build, on this i am using the storeReleaseRelease
        //https://r2qvt.app.goo.gl/?
        String deeplink=getString(R.string.deeplink_domain)+
                "?link="+
                getString(R.string.deeplink_link)+
                "&apn="+
                getString(R.string.deeplink_package)+
                "&amv=1"+
                "&afl="+
                getString(R.string.deeplink_not_installed_store_link)+
                "&al="+
                getString(R.string.deeplink_parse_url_game_id)+
                gameid;
        Log.e("<<deeplink>>","is:"+deeplink);
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.add_friends_dl_main))
                .setMessage(getString(R.string.lets_play_main))
                .setDeepLink(Uri.parse(deeplink))
                .setCustomImage(Uri.parse(DEEPLINK_LOGO))//getString(R.string.invitation_custom_image)))
//                .setDeepLink(Uri.parse("http://ijzepeda.com/addGame/"+gameid))// WORKS!
//                  https://r2qvt.app.goo.gl/?link=http://ijzepeda.com/&apn=com.ijzepeda.friendsknowsbest&amv=1&afl=https://play.google.com/store/apps/details?id=com.ijzepeda.fkb&al=http://ijzepeda.com/addGame/GAME123
//              .setEmailSubject("You are invited to a FriendsKnowsBest game")
//              .setEmailHtmlContent("Just click %%APPINVITE_LINK_PLACEHOLDER%% and lets play")
                .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                ", resultCode=" + resultCode);
        if(requestCode==REQUEST_INVITE){
            if(resultCode==RESULT_OK){
                //check no. of invitations sent
                String[] ids= AppInviteInvitation
                        .getInvitationIds(resultCode,data);
                Log.d(TAG,"Invitations sent:"+ids.length);
            }else{
                //failed or cancelled
                Log.d(TAG,"Failed to send invitations");
            }
        }


    }
}
