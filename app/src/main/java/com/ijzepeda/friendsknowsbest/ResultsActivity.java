package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ijzepeda.friendsknowsbest.Helpers.PlayersInGameRecyclerAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.name;
import static android.R.attr.process;
import static android.R.id.message;
import static com.ijzepeda.friendsknowsbest.R.id.favBtn;
import static com.ijzepeda.friendsknowsbest.R.id.playersRecyclerView;
import static com.ijzepeda.friendsknowsbest.R.id.winnerTextView;

public class ResultsActivity extends AppCompatActivity {
    //Bundle details
    String currentGameID;
    String currentDeckID;
    int gameTotalCards;
    int currentCard;
    int currentDeckCard;
    private static String GAME_ID="game_id";
    private static String DECK_ID="deck_id";
    private static String CURRENT_CARD_ID="current_card_id";
    private static String TOTAL_CARDS_ID="total_card_id";
    private static String CURRENT_DECK_CARD_ID="total_deck_card_id";
    private static String usermail="";
    private static String username="";
    private static String useruid="";
    private static String userPic="picURL";
    private Uri userPicURI;
    Context context;

    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseGameRef;
    private DatabaseReference databaseDeckRef;

    private Game currentGame;

    //Results Variables & elements
    String winnerName;
    String winnerImagePic;
    String winnerCard;
    String currentCardText;
    AlertDialog.Builder alertDialog;
//Views
    TextView cardWinnerTextView;
    TextView winnerNameTextView;
    ImageView winnerPic;
    Button counterBtn, continueBtn;
    ImageButton favoriteBtn, shareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        //retrieveGameDetails
        currentGameID=getIntent().getStringExtra(GAME_ID);
        currentDeckID=getIntent().getStringExtra(DECK_ID);
        currentCard=getIntent().getIntExtra(CURRENT_CARD_ID,0);//Todo check this getCurrentCard());
        currentDeckCard=getIntent().getIntExtra(CURRENT_DECK_CARD_ID,0);
        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number
        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number


        //FIREBASE
        app=FirebaseApp.getInstance();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseGameRef =database.getReference("Game");
        databaseDeckRef =database.getReference("Deck");

        //UserDetails
        //should I save them on the device? or keep veriifying userAuth?
        usermail=auth.getCurrentUser().getEmail();
        username=auth.getCurrentUser().getDisplayName();
        useruid=auth.getCurrentUser().getUid();
        userPic=auth.getCurrentUser().getPhotoUrl().toString();
        userPicURI=auth.getCurrentUser().getPhotoUrl();

         winnerPic=(ImageView)findViewById(R.id.winnerPicImageView);
         cardWinnerTextView=(TextView)findViewById(R.id.cardWinnerTextView);
        winnerNameTextView=(TextView)findViewById(R.id.winnerTextView);
continueBtn=(Button)findViewById(R.id.continueBtn);
        counterBtn=(Button)findViewById(R.id.counterBtn);
        shareBtn=(ImageButton)findViewById(R.id.shareBtn);
        favoriteBtn=(ImageButton)findViewById(R.id.favBtn);


Log.e("playerUri","is: "+userPicURI.toString());

//        Picasso.with(this).load(userPic).into(winnerPic);

        fetchPlayersFromGame();
        fetchUserVotes();
        CheckPlayersAcceptanceStatus();

    }


    public void fetchUserVotes(){
//        userVotesList
                databaseDeckRef.child(currentDeckID).child("card"+currentCard).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int i=0;
                        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
                            Log.e("fetchUserVotes,User key", childSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
                            Log.e("fetchUserVotes,User ref", childSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
                            Log.e("fetchUserVotes,User val", childSnapshot.getValue().toString()); //< Contains the whole json:.

                            Log.e("!@#~~~~~","Aqui hay error, "+childSnapshot.getValue(UserVote.class));
//                            dataSnapshot.getValue(Game.class)
                            userVotesList.add(i++,childSnapshot.getValue(UserVote.class));
                        }
                        processVotes();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    UserVote winningPlayer;
    String winningPlayerUID;
    String winningPlayerName;
    String winningPlayerPic;
    public void processVotes(){
//        Picasso.with(this).load(userPic).into(winnerPic);
        //int occurrences = Collections.frequency(animals, "bat");
int maxVotes=0,winningPlayerIndex=0;

        for(int i=0;i<userVotesList.size();i++){
//            if(userVotesList.get(i).getNomineeName()) {
//            }

        }


        Map<String, Integer> instances = new HashMap<String, Integer>();
        int index=0;
        for(UserVote userVote : userVotesList) {
                Integer value = instances.get(userVote.getNomineeUID());
                if (value == null) {
                    value = new Integer(0);
                    instances.put(userVote.getNomineeUID(), value);
                }
                instances.put(userVote.getNomineeUID(), value++);
            if(value>=maxVotes){
                maxVotes=value;
                winningPlayerUID=userVote.getNomineeUID();
                winningPlayerName=userVote.getNomineeName();
                winningPlayerPic=userVote.getNomineePicUrl();
//                winningPlayerIndex=index;
                winningPlayer=userVote;
            }
            index++;
        }


//        winningPlayer=userVotesList.get(winningPlayerIndex);
//        userVotesList.contains()


        refreshWinnerDetails();
    }


    public void refreshWinnerDetails(){
        //TODO CHECK I dont have to se a userVote, Because I fecth from acomment and not from the winning user
winnerNameTextView.setText(winningPlayerName);//winningPlayer.getNomineeName());
   Picasso.with(this).load(winningPlayerPic).into(winnerPic);
        String[] mQuotes ;//= getResources().getStringArray(R.array.category_romantic);
        mQuotes = getResources().getStringArray(R.array.category_romantic);
        String quote= mQuotes[currentDeckCard];
        String completeMessage;
        String winnerTitle;
        if(username.equals(winnerName)) { //TODO CHANGE TO UID
            completeMessage="Your Friend thout that You are more likely to " + quote.substring(2);
            winnerTitle="You won";
            }else{
            completeMessage = "Your friends thought that " + winningPlayerName + " is more likely to :" + quote.substring(2);
            winnerTitle=winningPlayerName+" won";
        }
        cardWinnerTextView.setText(completeMessage);
        winnerNameTextView.setText(winnerTitle);

    }

    //-------    Players RecyclerView
    PlayersInGameRecyclerAdapter playersRecyclerAdapter;
    RecyclerView playersRecyclerView;
    public List<String> playersList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    public void viewComment(String selectedPlayer){
        int userVotePosition=0;
        for(int i=0;i<userVotesList.size();i++){
            if(userVotesList.get(i).getName().equals(selectedPlayer)) {
                userVotePosition = i;
            }
        }

//Drawable newIcon=new BitmapDrawable(String.valueOf(android.R.drawable.ic_dialog_alert));//getDrawable(android.R.drawable.ic_dialog_alert);
//Picasso.with(this).load(winningPlayerPic).placeholder(newIcon);

        alertDialog=  new AlertDialog.Builder(this)
                .setTitle(selectedPlayer+ " says...")
//                .setMessage("String selectedPlayer is going to be the UserVote object, and parse elements here: userVote.getMessage()")
                .setMessage(userVotesList.get(userVotePosition).getMessage()+"!!!")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                })
                .setIcon(android.R.drawable.ic_dialog_info);
//                .setIcon(newIcon);
alertDialog.show();
    }

List<UserVote>userVotesList=new ArrayList<>();
    public void fetchPlayersFromGame(){
        playersRecyclerView=(RecyclerView)findViewById(R.id.playerCommentsRecyclerView);
        linearLayoutManager=new LinearLayoutManager(this);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2,LinearLayoutManager.HORIZONTAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        GridLayoutManager gridLayoutManager=new GridLayoutManager(GameActivity.this, 2,LinearLayoutManager.HORIZONTAL,false);

//Load players
        databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("!@#~~~~~","Aqui NO! hay error, "+dataSnapshot.getValue(Game.class));
                currentGame = dataSnapshot.getValue(Game.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseGameRef.child(currentGameID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalUsers=(int)dataSnapshot.getChildrenCount();
                for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){

//                    gameUserNamesList.add(childSnapshot.getValue());
                    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //TODO CHECAR": Posiblemente deba cambiar el nombre por el uid en DECK, y hacer un llamado aqui. y llenar los usuarios , para obtener nombre y foto, y enviarlos al adapter
                    playersList.add(childSnapshot.getValue().toString());
                    Log.e("databaseGameRef","playerlist just added"+childSnapshot.getValue().toString());
                    Log.e("databaseGameRef","playerlist just added"+playersList.get(0));
                    playersRecyclerAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//settign up the adapters
//        playersRecyclerView.setLayoutManager(linearLayoutManager);
        playersRecyclerView.setLayoutManager(gridLayoutManager);
        playersRecyclerAdapter =new PlayersInGameRecyclerAdapter(playersList,getApplication());
        playersRecyclerAdapter.notifyDataSetChanged();
        playersRecyclerView.setAdapter(playersRecyclerAdapter);

    }

int totalUsers;
int totalVotes;
    boolean alreadyVoted=false;
    public void CheckPlayersAcceptanceStatus(){
        Log.e("checkGame()","About to start reading changes");
        totalVotes=0;
        databaseDeckRef.child(currentDeckID).child("card"+currentCard).child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                alreadyVoted=false; //startloading and return to false, if found the match, then is true
                //refresh recyclerview adapter.
//                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean userVoted=false;
                if(dataSnapshot.child("voted").exists())
                    userVoted=Boolean.parseBoolean(dataSnapshot.child("voted").getValue().toString());
                if(userVoted){
                    //TODO CMABIAR QUE CHEQUE POR NOMBRE A UID!!!!!!!~~~~~~~~~~~~~~~~####################################************5t
                    if(dataSnapshot.child("name").equals(username)){
                        alreadyVoted=true;
                    }else{
                        alreadyVoted=true;//you just voted!
                        totalVotes++;
                    }
                }
                if(totalVotes>=totalUsers){
                    Log.e(">>checkGame...()","onChildAdded All players have voted! continue");
//                    showResult();


                }
            }


            /**Snapshot retrives only the child [in this case the USer] that changed. therefore, I just need to look for voted, not users/voted*/
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                alreadyVoted=false; //startloading and return to false, if found the match, then is true

//                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean userVoted=false;
                if(dataSnapshot.child("voted").exists())
                    userVoted=Boolean.parseBoolean(dataSnapshot.child("voted").getValue().toString());
                if(userVoted){
                    if(dataSnapshot.child("name").equals(username)){
                        alreadyVoted=true;
                    }else{
                        alreadyVoted=true;//you just voted!
                        totalVotes++;
                    }
                }

                if(totalVotes>=totalUsers){
                    Log.e(">>checkGame...()","onChildChanged All players have voted! continue");
//                    showResult();
                }
                //trigger next move
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
