package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ijzepeda.friendsknowsbest.models.UserVote;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ijzepeda.friendsknowsbest.Utils.CHILD_ACCEPTED_RESULT;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_CARD;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_CURRENT_CARD;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_NAME;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_NOMINEE_NAME;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_USERS;
import static com.ijzepeda.friendsknowsbest.Utils.REF_DECK;
import static com.ijzepeda.friendsknowsbest.Utils.REF_GAME;
import static com.ijzepeda.friendsknowsbest.Utils.SHARED_USERNAME;

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

   // private Game currentGame;

    //Results Variables & elements
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
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));
        context=this;
        //retrieveGameDetails
        currentGameID=getIntent().getStringExtra(GAME_ID);
        currentDeckID=getIntent().getStringExtra(DECK_ID);
        currentCard=getIntent().getIntExtra(CURRENT_CARD_ID,0);
        currentDeckCard=getIntent().getIntExtra(CURRENT_DECK_CARD_ID,0);
        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number

        //FIREBASE
        app=FirebaseApp.getInstance();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseGameRef =database.getReference(REF_GAME);
        databaseDeckRef =database.getReference(REF_DECK);


        //UserDetails
        //should I save them on the device? or keep veriifying userAuth?
        usermail=auth.getCurrentUser().getEmail();
        username=auth.getCurrentUser().getDisplayName();
        if(username==null || username.equals(null) || username.equals("")){
            username=Utils.getInstance().getValue(getApplication(),SHARED_USERNAME);
        }
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


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueBtn.setClickable(false);
                if(!alreadyAccepted) {
                    alreadyAccepted=true;
                    databaseDeckRef.child(currentDeckID).child(CHILD_CARD +currentCard).child(CHILD_USERS).child(useruid).child(CHILD_ACCEPTED_RESULT).setValue(alreadyAccepted);
                }else{
                    Toast.makeText(context, R.string.wait_player_see_result, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //this one just populate the recyclerview
        fetchPlayersFromGame();

        fetchUserVotes();

//        CheckPlayersAcceptanceStatus(); //Moved to inside of fetchUserVotes()>Processvotes()

    }


    public void fetchUserVotes(){
//        userVotesList
                databaseDeckRef.child(currentDeckID).child(CHILD_CARD+currentCard).child(CHILD_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        totalUsers=(int)dataSnapshot.getChildrenCount();// added on 19-10-0003
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
int maxVotes=0;

        Map<String, Integer> instances = new HashMap<String, Integer>();
//        int index=0;
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
                winningPlayerPic=""+userVote.getNomineePicUrl();
//                winningPlayerIndex=index;
                winningPlayer=userVote;
            }
//            index++;
        }



        refreshWinnerDetails();

//        CheckPlayersAcceptanceStatus();

    }


    public void refreshWinnerDetails(){
winnerNameTextView.setText(winningPlayerName);//winningPlayer.getNomineeName());
   Picasso.with(this).load(winningPlayerPic).error(R.drawable.placeholder).into(winnerPic);
        String[] mQuotes ;//= getResources().getStringArray(R.array.category_romantic);
        mQuotes = getResources().getStringArray(R.array.category_romantic);
        String quote= mQuotes[currentDeckCard];
        String completeMessage;
        String winnerTitle;
        if(useruid.equals(winningPlayerUID)) {
            completeMessage=getString(R.string.im_more_likely_to) + quote.substring(2);
            winnerTitle=getString(R.string.you_won);
            }else{
            completeMessage = getString(R.string.your_friends_though_that) + winningPlayerName + getString(R.string.is_more_likely_to) + quote.substring(2);
            winnerTitle=winningPlayerName+getString(R.string.friend_won);
        }
        cardWinnerTextView.setText(completeMessage);
        winnerNameTextView.setText(winnerTitle);

        fetchCommentsForWinner();

    }


    public void fetchCommentsForWinner(){

//        childSnapshot.getValue().toString()
//        playersList.clear();//NO MORE
        userVotesList.clear();
        playersRecyclerAdapter.notifyDataSetChanged();

        databaseDeckRef.child(currentDeckID).child(CHILD_CARD+currentCard).child(CHILD_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
        if(childSnapshot.child(CHILD_NOMINEE_NAME).getValue().toString().equals(winningPlayerName)) {
            userVotesList.add(childSnapshot.getValue(UserVote.class));
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
    RecyclerView playersFromDeckRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    public void viewComment(String selectedPlayer, String selectedPlayerUid, String message){
        int userVotePosition=0;
        for(int i=0;i<userVotesList.size();i++){
            if(userVotesList.get(i).getUseruid().equals(selectedPlayerUid)) {  //Si voto por el?
                userVotePosition = i;
            }
        }
  if(!message.equals("")) {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle(selectedPlayer + getString(R.string.says))
                    .setMessage(message + getString(R.string.exclamation_marks))
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
    }

List<UserVote>userVotesList=new ArrayList<>();
    public void fetchPlayersFromGame(){
        playersFromDeckRecyclerView=(RecyclerView)findViewById(R.id.playerCommentsRecyclerView);
        linearLayoutManager=new LinearLayoutManager(this);
        GridLayoutManager gridLayoutVerticalManager=new GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

//Load players
        databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              CheckPlayersAcceptanceStatus();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//settign up the adapters
        playersFromDeckRecyclerView.setLayoutManager(gridLayoutVerticalManager);
        playersRecyclerAdapter =new PlayersInGameRecyclerAdapter(userVotesList,getApplication());
        playersRecyclerAdapter.notifyDataSetChanged();
        playersFromDeckRecyclerView.setAdapter(playersRecyclerAdapter);

    }

    //useful to continue
    int totalUsers;
    int totalAccepted;
    boolean alreadyAccepted=false;
    public void CheckPlayersAcceptanceStatus(){
        totalAccepted=0;
        databaseDeckRef.child(currentDeckID).child(CHILD_CARD+currentCard).child(CHILD_USERS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                alreadyAccepted=false; //startloading and return to false, if found the match, then is true
                //refresh recyclerview adapter.
                playersRecyclerAdapter.notifyDataSetChanged();//it was commented on 18-10-16.2344
                //determine if all players have voted
                boolean userAccepted=false;
                if(dataSnapshot.child(CHILD_ACCEPTED_RESULT).exists()) { //extended to bottom
                    userAccepted = Boolean.parseBoolean(dataSnapshot.child(CHILD_ACCEPTED_RESULT).getValue().toString());
                    if (userAccepted) {
                        if (dataSnapshot.child(CHILD_NAME).equals(username)) {
                            alreadyAccepted = true;
                        }
                        totalAccepted++;
                    }
                    if (totalAccepted >= totalUsers) {
                        endCard();
                    }

                }//extended up tohere
            }


            /**Snapshot retrives only the child [in this case the USer] that changed. therefore, I just need to look for voted, not users/voted*/
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                alreadyAccepted=false; //startloading and return to false, if found the match, then is true

//                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean userAccepted=false;
                if(dataSnapshot.child(CHILD_ACCEPTED_RESULT).exists()) {//extend to bottom
                    userAccepted = Boolean.parseBoolean(dataSnapshot.child(CHILD_ACCEPTED_RESULT).getValue().toString());
                    if (userAccepted) {
                        if (dataSnapshot.child(CHILD_NAME).equals(username)) {
                            alreadyAccepted = true;
                        }
                        totalAccepted++;
                    }
                    if (totalAccepted >= totalUsers) {
                        endCard();
                    }

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
        currentCard++;
if(currentCard<=gameTotalCards) {
    databaseGameRef.child(currentGameID).child(CHILD_CURRENT_CARD).setValue(currentCard);
    Intent intent = new Intent(this, GameActivity.class);
    intent.putExtra(GAME_ID, currentGameID);
    intent.putExtra(DECK_ID, currentDeckID + "");
    intent.putExtra(CURRENT_CARD_ID, currentCard );//+1
    intent.putExtra(TOTAL_CARDS_ID, gameTotalCards);
    intent.putExtra(CURRENT_DECK_CARD_ID, currentDeckCard);

    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//closing next activity
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//
    startActivity(intent);
    finish();
}else{
    Intent intent=new Intent(this,GameOverResults.class);
//    Intent intent=new Intent(this,MainActivity.class);

    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//closing next activity
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//
    startActivity(intent);
    finish();
}
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
