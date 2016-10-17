package com.ijzepeda.friendsknowsbest;

import android.app.Activity;
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
import android.view.View;
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

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.attr.process;
import static android.R.attr.y;
import static android.R.id.message;
import static android.R.string.no;
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
//    String winnerName;
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
//        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number


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


//Log.e("playerUri","is: "+userPicURI.toString());
//        Picasso.with(this).load(userPic).into(winnerPic);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueBtn.setClickable(false);
                if(!alreadyAccepted) {
                    alreadyAccepted=true;
//                    UserVote userVote = new UserVote(username, useruid, userPic, commentTextView.getText().toString(), true, selectedPlayer + "UID", selectedPlayer,selectedPlayer+"userPic",false);
                    databaseDeckRef.child(currentDeckID).child("card" +currentCard).child("users").child(useruid).child("acceptResult").setValue(alreadyAccepted);
                }else{

                }
            }
        });


        //this one just populate the recyclerview
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
//        playersThatCommentedToWinnerList
//      childSnapshot.getValue().toString()

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
        if(username.equals(winningPlayerName)) { //TODO CHANGE TO UID
            completeMessage="Your Friend thout that You are more likely to " + quote.substring(2);
            winnerTitle="You won";
            }else{
            completeMessage = "Your friends thought that " + winningPlayerName + " is more likely to :" + quote.substring(2);
            winnerTitle=winningPlayerName+" won";
        }
        cardWinnerTextView.setText(completeMessage);
        winnerNameTextView.setText(winnerTitle);

        fetchCommentsForWinner();

    }


    public void fetchCommentsForWinner(){

//        childSnapshot.getValue().toString()
        playersList.clear();
        playersRecyclerAdapter.notifyDataSetChanged();


        databaseDeckRef.child(currentDeckID).child("card"+currentCard).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
Log.e("~~~~~~>fetchcomment","for winner childSnapshot.child(\"name\").getValue()"+childSnapshot.child("name").getValue().toString());
        if(childSnapshot.child("nomineeName").getValue().toString().equals(winningPlayerName)) {
            playersList.add(childSnapshot.child("name").getValue().toString());//childSnapshot.getValue().toString());
            playersRecyclerAdapter.notifyDataSetChanged();
        }

        }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    //-------    Players RecyclerView
    PlayersInGameRecyclerAdapter playersRecyclerAdapter;
    RecyclerView playersRecyclerView;
    public List<String> playersList=new ArrayList<>();
    public List<String> playersThatCommentedToWinnerList=new ArrayList<>();
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
        GridLayoutManager gridLayoutVerticalManager=new GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        GridLayoutManager gridLayoutManager=new GridLayoutManager(GameActivity.this, 2,LinearLayoutManager.HORIZONTAL,false);

//Load players
        /**
        databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                for(DataSnapshot childSnapshot:dataSnapshot.getChildren()) {

//                    gameUserNamesList.add(childSnapshot.getValue());
                    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //TODO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //TODO CHECAR": Posiblemente deba cambiar el nombre por el uid en DECK, y hacer un llamado aqui. y llenar los usuarios , para obtener nombre y foto, y enviarlos al adapter

//                    if(childSnapshot.getValue().toString())
                    {
                    playersList.add(childSnapshot.getValue().toString());
                    Log.e("databaseGameRef", "playerlist just added" + childSnapshot.getValue().toString());
                    Log.e("databaseGameRef", "playerlist just added" + playersList.get(0));
                    playersRecyclerAdapter.notifyDataSetChanged();
                }
//              todo      teniendo esa lista, hacer un llamado a USERS y sacar objetos de usuarios, y mandarlos al recycler, no la lista de nombres... que serian uids no names

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/
//settign up the adapters
//        playersRecyclerView.setLayoutManager(linearLayoutManager);
//        playersRecyclerView.setLayoutManager(gridLayoutManager);
        playersRecyclerView.setLayoutManager(gridLayoutVerticalManager);
        playersRecyclerAdapter =new PlayersInGameRecyclerAdapter(playersList,getApplication());
        playersRecyclerAdapter.notifyDataSetChanged();
        playersRecyclerView.setAdapter(playersRecyclerAdapter);

    }

    //useful to continue
    int totalUsers;
    int totalAccepted;
    boolean alreadyAccepted=false;
    public void CheckPlayersAcceptanceStatus(){
        Log.e("checkGame()","About to start reading changes");
        totalAccepted=0;
        totalUsers=currentGame.getNoUsers();//is it null?
        databaseDeckRef.child(currentDeckID).child("card"+currentCard).child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                alreadyAccepted=false; //startloading and return to false, if found the match, then is true
                //refresh recyclerview adapter.
//                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean userAccepted=false;
                if(dataSnapshot.child("acceptResult").exists())
                    userAccepted=Boolean.parseBoolean(dataSnapshot.child("acceptResult").getValue().toString());
                if(userAccepted){
                    //TODO CMABIAR QUE CHEQUE POR NOMBRE A UID!!!!!!!~~~~~~~~~~~~~~~~####################################************5t
                    if(dataSnapshot.child("name").equals(username)){
                        alreadyAccepted=true;
                    }else{
                        alreadyAccepted=true;//you just voted!
                        totalAccepted++;
                    }
                }
                if(totalAccepted>=totalUsers){
                    Log.e(">>checkGame...()","onChildAdded All players have accepted! continue");
//                    showResult();
endCard();

                }
            }


            /**Snapshot retrives only the child [in this case the USer] that changed. therefore, I just need to look for voted, not users/voted*/
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                alreadyAccepted=false; //startloading and return to false, if found the match, then is true

//                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean userAccepted=false;
                if(dataSnapshot.child("acceptResult").exists())
                    userAccepted=Boolean.parseBoolean(dataSnapshot.child("acceptResult").getValue().toString());
                if(userAccepted){
                    if(dataSnapshot.child("name").equals(username)){
                        alreadyAccepted=true;
                    }else{
                        alreadyAccepted=true;//you just voted!
                        totalAccepted++;
                    }
                }

                if(totalAccepted>=totalUsers){
                    Log.e(">>checkGame...()","onChildChanged All players have accepted! continue");
//                    showResult();
                    endCard();
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

    public void endCard(){


if(gameTotalCards<currentCard) {
    databaseGameRef.child(currentGameID).child("currentCard").setValue(currentCard + 1);
    Intent intent = new Intent(this, GameActivity.class);
    intent.putExtra(GAME_ID, currentGameID);
    intent.putExtra(DECK_ID, currentDeckID + "");
    intent.putExtra(CURRENT_CARD_ID, currentCard + 1);
    intent.putExtra(TOTAL_CARDS_ID, gameTotalCards);
    intent.putExtra(CURRENT_DECK_CARD_ID, currentDeckCard);
    startActivity(intent);
    finish();
}else{
//    Intent intent=new Intent(this,GameOver.class);
    Intent intent=new Intent(this,MainActivity.class);
    startActivity(intent);
}
    }


}
