package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ijzepeda.friendsknowsbest.R.string.username;

public class NewGame extends AppCompatActivity  implements
        GoogleApiClient.OnConnectionFailedListener {
    private static String TAG="NewGameActivity";
    private static final int REQUEST_INVITE = 1;
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    public static final String MESSAGES_CHILD = "messages";

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
    int currentCard=0;
    String deckId="deck00";
    String name;//=gameName.getText().toString();//"First Game";
    int noUsers=1;
    int noCards;
//    private static int defaultNoCards=10;
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
//TODO: create the game first, before sending the invite. to add the game url maybe
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
        databaseGameRef =database.getReference("Game");
        databaseDeckRef =database.getReference("Deck");
        databaseUserRef =database.getReference("Users");

        userUid=Utils.getInstance().getValue(getApplication(),"uid");
        userName=auth.getCurrentUser().getDisplayName();
        if(userName==null || userName.equals(null) || userName.equals("")){
            userName=""+Utils.getInstance().getValue(getApplication(),"username");
        }

        userEmail=Utils.getInstance().getValue(getApplication(),"email");
//        String userName2=""+auth.getCurrentUser().getDisplayName();
//        Toast.makeText(this, "userName:"+userName+", userName2:"+userName2, Toast.LENGTH_SHORT).show();

        if(auth.getCurrentUser().getPhotoUrl()!=null) {
//    LOAD FROM USERS DB or sharedprefs
    userPic =Utils.getInstance().getValue(getApplication(),getResources().getString(R.string.shared_userphotourl_key));

}

//Generate the random UID
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime());
int randomSeed=(int)(Math.random());
//        deckId=deckId+timeStamp+"_"+randomSeed;
//        uid="GAME_"+timeStamp+"_"+randomSeed;
        deckId=deckId+"123";
        uid="GAME123";


        gameNameTV=(TextView)findViewById(R.id.gameNameTextView);
        noCardsTV=(TextView)findViewById(R.id.numberCardsTextView);
        nextBtn=(ImageButton)findViewById(R.id.nextInviteBtn);
        inviteBtn=(Button)findViewById(R.id.inviteFriendsBtn);
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO for now it will send to load game
//                Intent TempIntent= new Intent(NewGame.this, LoadActivity.class);
//                startActivity(TempIntent);
//                finish();
//                return;

              String gameId=  createGame();
                sendInvitation(gameId);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDeck();
//                createGame();// needs to invite friends before set the game

                Intent TempIntent= new Intent(NewGame.this, LoadActivity.class);
                startActivity(TempIntent);
                finish();
                return;
            }
        });

        //TODO: here send the invites to friends, when they respond to them they will be guided to
        //load game, where it is going to be added to their games.



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

    }
public void createDeck(){
    //i was about to put the card text in the back end, BUT
    //i need them for offline
    //also if users have different language, they only will read what is online
//TODO steps
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
    String [] cards=getResources().getStringArray(R.array.category_romantic);//TODO Verifica esto!
    int noOfCardsInArray=cards.length;

    Integer[] randomCardOrder = new Integer[noOfCardsInArray];
    for ( i = 0; i < randomCardOrder.length; i++) {
        randomCardOrder[i] = i;
    }
    Collections.shuffle(Arrays.asList(randomCardOrder));

//OnlineCard

    //todo este codigo semifunciona, genera las cards# pero solo al ultimo le pone los childs
     String cardNo;
//    playerOnCardMap.put(userName,userEmail);
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ hacer solo (userUID,"") como objeto, y dentro ponerle los valores de nombre, fotoURL, votado, mensaje y etc etc
//    playerOnCardMap.put(userUid,userName);//todo commented on 19-10:1116
    playerOnCardMap.put(userUid,userUid);
    Log.e("Track&Delete","    playerOnCardMap.put(userUid,userName):"+userUid+", "+userName);
//    for( i =0;i<noOfCardsInArray;i++) {
    for( i =0;i<noCards;i++) {
         cardNo = "card" + i;
         cardMap.put(cardNo, ""); //esto agrega la cardNo y el numero que tiene. esto debe ir dentro de cardNo{card:#}
         databaseDeckRef.child(deckId).updateChildren(cardMap);
         collectionCardNoMap.put("card", randomCardOrder[i]);
         collectionCardNoMap.put("users", "");
         databaseDeckRef.child(deckId).child(cardNo).updateChildren(collectionCardNoMap);
         databaseDeckRef.child(deckId).child(cardNo).child("users").updateChildren(playerOnCardMap);
        //CREATE UserVote
        Log.e("Adding PLAYER","Creating blank userVote deckId:"+deckId+", userUid:"+userUid+", userName:"+userName+", cardNo"+cardNo+", userPic"+userPic);
        UserVote userVote = new UserVote(userName, userUid, userPic, "", false, "" + "", "","nomineePicUrl",false);
        databaseDeckRef.child(deckId).child(cardNo).child("users").child(userUid).setValue(userVote);


        cardMap.clear();
         collectionCardNoMap.clear();
     }
//    databaseDeckRef.child(deckId).child("card9").child("users").updateChildren(playerOnCardMap);//la puse y funciono!

//    databaseDeckRef.child(deckId).setValue(cardMap);
//    databaseGameRef.child("GAME"+i).setValue(gameObject);
//Updatechildren, no borra
//    children.children. accesa directo a un record

//TODO Update game values, such as: noCARDS or name, if user change it from invite to send. BLOCK
}


    public String createGame(){
  //        databaseGameRef.push().setValue(user);
//        databaseGameRef.updateChildren("Game1");
//        Map<String,Object> users=new HashMap<String,Object>();
//        users.put("users","");
//        databaseGameRef.updateChildren(users);
name=gameNameTV.getText().toString();
        //Todo add Validation to dont leave it empty
        if(Integer.parseInt(noCardsTV.getText().toString())>0) {
            noCards = Integer.parseInt(noCardsTV.getText().toString());
            //
            if(noCards>(getResources().getStringArray(R.array.category_romantic)).length){
                noCards=(getResources().getStringArray(R.array.category_romantic)).length;
            }

        }else{
            noCards=(getResources().getStringArray(R.array.category_romantic)).length;
        }

//        userMap.put(userName,userEmail);
        userMap.put(userUid,userName);
//        Toast.makeText(this, "2userName:"+userName, Toast.LENGTH_SHORT).show();
//TODO CREATE NAME OF UID
        Map<String,Object> map=new HashMap<String,Object>();
        int i=123;
//        map.put("GAME"+i,"");
        map.put(uid,"");
        Game gameObject=new Game(currentCard,deckId,name,noUsers,noCards,uid,unlimitedCounter,userMap);

       databaseGameRef.updateChildren(map);

databaseGameRef.child(uid).push().setValue(gameObject);//Todo what is this for?!
                        //usar el updateChildren con un Map , en vez de un objeto
//                        databaseGameRef.child("GAME"+i).updateChildren(gameObjectMAP);
databaseGameRef.child(uid).setValue(gameObject);
//        databaseGameRef.child("GAME"+i).
//TODO ~~~~~~~~~~~~~~~~~ THis methos needs to be added when user receive the link to be added to a game
        userMap.clear();
//        userMap.put("UserUID2","User NAme 2");
//        userMap.put("UserUID3","User name 3");
        //ADD A NEW USER USING THE GAMEID
       // databaseGameRef.child("GAME"+i).child("users").updateChildren(userMap);
//        databaseGameRef.child("GAME"+i).child("noUsers").setValue(3); //todo get current value and extra 1

        addGameToUser();// maybe just this do the job: databaseUserRef.child(userUid).child("games").updateChildren(gamesMap);
return uid;
    }


    private void addGameToUser(){
        databaseUserRef.child(userUid).child("games").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(uid).exists()) {
//                    addGame(uid);
                    Map<String,Object> gamesMap=new HashMap<String, Object>();
                    gamesMap.put(uid,uid);
//        databaseUsersRef.child(userUid).child("games").setValue(gamesMap);//TODO this will overwrite oldvalues
                    databaseUserRef.child(userUid).child("games").updateChildren(gamesMap);

                }else{
                    Toast.makeText(NewGame.this, "You are in that game", Toast.LENGTH_SHORT).show();
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
        //https://r2qvt.app.goo.gl/?
        Intent intent = new AppInviteInvitation.IntentBuilder("Add some friends")
                .setMessage("Lets play!")
//                .setCallToActionText("Call to action text")
//                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))

//                .setDeepLink(Uri.parse("http://example.com/offer/five_dollar_offer"))//TODO WORKS!~~~~~
//                .setDeepLink(Uri.parse("http://ijzepeda.com/addGame/"+gameid))//TODO WORKS!
//https://r2qvt.app.goo.gl/?link=http://ijzepeda.com/&apn=com.ijzepeda.friendsknowsbest&amv=1&afl=https://play.google.com/store/apps/details?id=com.ijzepeda.fkb&al=http://ijzepeda.com/addGame/GAME123
                .setDeepLink(Uri.parse(""+
getString(R.string.deeplink_domain)+
"?link="+
getString(R.string.deeplink_link)+
"&apn="+
getString(R.string.deeplink_package)+
"&amv=1"+
"&afl="+
getString(R.string.deeplink_not_installed_store_link)+
"&al="+
getString(R.string.deeplink_parse_url_game_id)+
gameid))
//""+
//                        "http://ijzepeda.com/addGame/&apn=com.ijzepeda.fkb&amv=1&afl=https://play.google.com/store/apps/details?id=com.ijzepeda.fkb[&ad=1][&al=android_link]"
//                        +gameid))//TODOWORKS! https://domain/?link=your_deep_link&apn=package_name[&amv=minimum_version][&ad=1][&al=android_link][&afl=fallback_link]


//  .setDeepLink(Uri.parse("https://r2qvt.app.goo.gl/Q8OH"))
                .setCustomImage(Uri.parse("android.resource://com.ijzepeda.friendsknowsbest/mipmap/logo"))//getString(R.string.invitation_custom_image)))

//        .setEmailSubject("You are invited to a FriendsKnowsBest game")
//        .setEmailHtmlContent("Just click %%APPINVITE_LINK_PLACEHOLDER%% and lets play")

                .build();
//        intent.putExtra("prueba","con exito");
        startActivityForResult(intent, REQUEST_INVITE);

        //TODO Once sent, block gamename!

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
