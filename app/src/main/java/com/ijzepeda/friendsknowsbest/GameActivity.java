package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daprlabs.cardstack.SwipeDeck;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ijzepeda.friendsknowsbest.Helpers.PlayersInGameRecyclerAdapter;
import com.ijzepeda.friendsknowsbest.Helpers.SwipeDeckAdapter;
import com.ijzepeda.friendsknowsbest.models.Card;
import com.ijzepeda.friendsknowsbest.models.Game;
import com.ijzepeda.friendsknowsbest.models.UserVote;
import com.ijzepeda.friendsknowsbest.widget2.WidgetProvider2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ijzepeda.friendsknowsbest.Utils.CHILD_CARD;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_CURRENT_CARD;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_NAME;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_USERS;
import static com.ijzepeda.friendsknowsbest.Utils.CHILD_VOTED;
import static com.ijzepeda.friendsknowsbest.Utils.REF_GAME;
import static com.ijzepeda.friendsknowsbest.Utils.SHARED_USERNAME;

public class GameActivity extends AppCompatActivity {
    //Bundle details
String currentGameID;
String currentDeckID;
int gameTotalCards;
int currentDeckCard;

    private static String TAG="MainActivity";


    private static String GAME_ID="game_id";
    private static String DECK_ID="deck_id";
    private static String CURRENT_CARD_ID="current_card_id";
    private static String TOTAL_CARDS_ID="total_card_id";
    private static String CURRENT_DECK_CARD_ID="total_deck_card_id";

    private static String usermail="";
    private static String username="";
    private static String useruid="";
    private static String userPic="picURL";
//    private static Uri userPicUri="picURL";
    Context context;

    //CurrentGame
    private int currentCard;
//    private int totalCards;
    private int totalUsers;
    private Game currentGame;

    private LinearLayoutManager linearLayoutManager;
    public List<Game> gamesList=new ArrayList<>();

    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseGameRef;
    private DatabaseReference databaseDeckRootRef;

    //CardSwipe
    AlertDialog.Builder alertDialog;
    SwipeDeckAdapter adapter;
    SwipeDeck cardStack;
    ArrayList<Card> cards=new ArrayList<>();
    private String[] mQuotes ;
    int displayCardNo=0;
    String category="Misc";

//Views
    TextView commentTextView;
    ImageButton sendBtn;

    //Game Variables
    int totalVotes;
    boolean alreadyVoted=true;
    boolean finishReadingUsers=true;

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //orback to mainactivity/load activity
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_game);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));

//FIREBASE
        app=FirebaseApp.getInstance();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        databaseGameRef =database.getReference(REF_GAME);
        databaseDeckRootRef =database.getReference(REF_DECK);//150317 - was REF_GAME, never found the route, therefore crash


        //retrieveGameDetails
        currentGameID=getIntent().getStringExtra(GAME_ID);
        currentDeckID=getIntent().getStringExtra(DECK_ID);
        currentCard=getIntent().getIntExtra(CURRENT_CARD_ID,0);// check this getCurrentCard());
        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number



//retrievegameDetailsFromWidget
        Game gameToLoad=getIntent().getParcelableExtra(WidgetProvider2.EXTRA_WIDGET_GAME);
if(gameToLoad!=null){
    currentGameID=gameToLoad.getUid();
    currentDeckID=gameToLoad.getDeckId();
    currentCard=gameToLoad.getCurrentCard();
    gameTotalCards=gameToLoad.getNoCards();

}

        //if(gameTotalCards>=0)
        isGameover();


Log.d("GameActivity","Oncreate on  gameuid:"+currentGameID+", card:"+currentCard);// DELETE
       FirebaseUser mFirebaseUser = auth.getCurrentUser();

        if( mFirebaseUser==null){
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
return;
        }




        mQuotes = getResources().getStringArray(R.array.category_romantic);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        context=getApplication();

        //Link Views
        commentTextView=(TextView)findViewById(R.id.commentTV);
        sendBtn=(ImageButton) findViewById(R.id.SendImageButton);
        sendBtn.setClickable(false);//available until it is sure that the user hasnt voted and has selected a nominee


        //UserDetails
        //should I save them on the device? or keep veriifying userAuth?
        usermail=auth.getCurrentUser().getEmail();
        username=auth.getCurrentUser().getDisplayName();
        if(username==null || username.equals(null) || username.equals("")){
            username=Utils.getInstance().getValue(getApplication(),SHARED_USERNAME);
        }
        useruid=auth.getCurrentUser().getUid();
        userPic=auth.getCurrentUser().getPhotoUrl()!=null?auth.getCurrentUser().getPhotoUrl().toString():"";


     //AQUI IBA EL Loader del DECK
     loadDeck();

        //CARDSWIPE
        cardStack.setEventCallback(new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                fetchCard();
                Log.e(TAG, "card was swiped left, position in adapter: " + position);
            }

            @Override
            public void cardSwipedRight(int position) {
                fetchCard();
                Log.e(TAG, "card was swiped right, position in adapter: " + position);
                //testData.add(""+i++);
            }
            @Override
            public void cardsDepleted() {
                Log.e(TAG, "no more cards");
//                alertDialog.show();
//                Intent intent=new Intent(getApplication(),GameActivity.class);//Missing currennt game Bundle
//                startActivity(intent);
//                finish();
            }
            @Override
            public void cardActionDown() {
            }
            @Override
            public void cardActionUp() {
            }
        });

     fetchPlayersFromDeck();

    }




    public  void createCard(int cardNo){
        String quote= mQuotes[cardNo];
        Card cartita=new Card(displayCardNo++, Integer.parseInt(quote.substring(0,1)),quote,category,false);//tenia un 0,1
//        Integer.parseInt(quote.substring(0,1));
        cards.add(cartita);

    }
    public void fetchCard(){
        // cards.add(new Card(0,0,"","",true));
    }


public void loadDeck(){

    databaseDeckRootRef.child(currentDeckID).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int noOfCurrentCardToCreate=0;
            for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
                if(noOfCurrentCardToCreate<currentCard || currentCard==0) {
                    String avoidNullStringToInt=""+childSnapshot.child(CHILD_CARD).getValue();
                    int cardNo =0+( Integer.parseInt(avoidNullStringToInt));
                    createCard(cardNo);
                    if(currentCard==noOfCurrentCardToCreate){
                        currentDeckCard=cardNo;
                    }
                }

                noOfCurrentCardToCreate++;

            }
            Collections.reverse(cards);//from newer
            adapter = new SwipeDeckAdapter(cards, context);
            cardStack.setAdapter(adapter);


        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

}



    //-------    Players RecyclerView
    PlayersInGameRecyclerAdapter playersRecyclerAdapter;
//    RecyclerView playersRecyclerView;// commented on 19-10: 1132
    RecyclerView playersFromDeckRecyclerView;
    public List<String> playersList=new ArrayList<>(); // commented to test user object trecyclerview 19-10
    public List<UserVote> playersListFromDeck=new ArrayList<>();

    public void fetchPlayersFromDeck(){
        playersFromDeckRecyclerView=(RecyclerView)findViewById(R.id.playersRecyclerView);
        linearLayoutManager=new LinearLayoutManager(this);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2,LinearLayoutManager.HORIZONTAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//Load players
        databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentGame = dataSnapshot.getValue(Game.class);

                databaseDeckRootRef.child(currentGame.getDeckId()).child(CHILD_CARD+currentCard).child(CHILD_USERS).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        playersListFromDeck.clear();//  for some reason, clearing delete everything for the next steps, but removing this line, will duplicate users[momentarily]
                        totalUsers=(int)dataSnapshot.getChildrenCount();
                        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
                            UserVote userToRecyclerView=childSnapshot.getValue(UserVote.class);  //CREO QUE ya: mando el uservote, pero debo llenarle los datos de nominee
                            playersListFromDeck.add(userToRecyclerView); // commented to test user object trecyclerview 19-10
                            playersRecyclerAdapter.notifyDataSetChanged();
                        }
                        //Having players fetched continue
                        checkGameAndPlayerStatus();
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


//settign up the adapters

        playersFromDeckRecyclerView.setLayoutManager(gridLayoutManager);
        playersRecyclerAdapter =new PlayersInGameRecyclerAdapter(playersListFromDeck,getApplication());
        playersRecyclerAdapter.notifyDataSetChanged();
        playersFromDeckRecyclerView.setAdapter(playersRecyclerAdapter);

    }







   public void selectedPlayer(final String selectedPlayer,final String selectedPlayerUID,final String selectedPlayerPHOTOURL){
//        commentTextView.setInputType(InputType.TYPE_CLASS_TEXT);
//        commentTextView.setFocusable(true);
       // ~~~~ Error aqui , no esta recibiendo valores!!!
       commentTextView.setHint(getString(R.string.why_would_you_vote_him)+selectedPlayer);
//       sendBtn.setClickable(false);
       sendBtn.setClickable(true);


       sendBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
//                sendBtn.setClickable(false);
               sendBtn.setColorFilter((R.color.bel_lightgrey_text));
               commentTextView.setFocusable(false);
               if(finishReadingUsers){
               if(!alreadyVoted) {
                   alreadyVoted=true;
                   UserVote userVote = new UserVote(username, useruid, userPic, commentTextView.getText().toString(), true, selectedPlayerUID, selectedPlayer,selectedPlayerPHOTOURL,false);
                   databaseDeckRootRef.child(currentDeckID).child(CHILD_CARD + currentGame.getCurrentCard()).child(CHILD_USERS).child(useruid).setValue(userVote);
               }else{
                   Toast.makeText(context, R.string.wait_player_vote, Toast.LENGTH_SHORT).show();
                   if (totalVotes >= totalUsers) {
                       showResult();
                   }
               }
               }
           }
       });
   }


    public void checkGameAndPlayerStatus(){
        totalVotes=0;
        databaseDeckRootRef.child(currentDeckID).child(CHILD_CARD+currentCard).child(CHILD_USERS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 alreadyVoted=false; //startloading and return to false, if found the match, then is true
                playersRecyclerAdapter.notifyDataSetChanged();

                //determine if all players have voted
                boolean userVoted=false;
                if(dataSnapshot.child(CHILD_VOTED).exists()) {
                    userVoted = Boolean.parseBoolean(dataSnapshot.child(CHILD_VOTED).getValue().toString());
                    if (userVoted) {

                        if (dataSnapshot.child(CHILD_NAME).equals(username)) {
                            alreadyVoted = true;
                            finishReadingUsers=false;
                            commentTextView.setFocusable(false);
                            sendBtn.setClickable(false);
                            sendBtn.setColorFilter((R.color.bel_lightgrey_text));

                        }
                        totalVotes++;
                    }


                    finishReadingUsers=true;
                }  //hasta aqui lo expandi
                if (totalVotes >= totalUsers) {
                    showResult();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                 alreadyVoted=false; //startloading and return to false, if found the match, then is true
                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean readingUserVoted=false;
                if(dataSnapshot.child(CHILD_VOTED).exists()) {//expandido hasta abajo
                    readingUserVoted = Boolean.parseBoolean(dataSnapshot.child(CHILD_VOTED).getValue().toString());

                    if (readingUserVoted) {

                        if (dataSnapshot.child(CHILD_NAME).equals(username)) {
                            alreadyVoted = true;

                            commentTextView.setFocusable(false);
                            sendBtn.setClickable(false);
                            sendBtn.setColorFilter((R.color.bel_lightgrey_text));
                        }
                        totalVotes++;
                    }


                } //se expadio hasta aqui
                //trigger next move

                if (totalVotes >= totalUsers) {
                    showResult();
                }
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

public void showResult(){

    Intent intent=new Intent(this,ResultsActivity.class);
    intent.putExtra(GAME_ID, currentGameID);
    intent.putExtra(DECK_ID,  currentDeckID+"");
    intent.putExtra(CURRENT_CARD_ID,  currentCard);
    intent.putExtra(TOTAL_CARDS_ID,  gameTotalCards);
    intent.putExtra(CURRENT_DECK_CARD_ID,  currentDeckCard);
    //////////
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//closing next activity
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//
    // inside your activity (if you did not enable transitions in your theme)
/////    getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
// set an exit transition
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getWindow().setExitTransition(new Explode());
    }

    startActivity(intent);
    finish();

}

    //
    //Fill Widget Data!
    public void fillWidgetData(){

    }



    int fetchedCurrentCard;
    public int getCurrentCard(){
        fetchedCurrentCard=0;
        databaseGameRef.child(currentGameID).child(CHILD_CURRENT_CARD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fetchedCurrentCard=Integer.parseInt(dataSnapshot.getValue().toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return fetchedCurrentCard;
    }

    public boolean isGameover(){

        if(currentCard>=gameTotalCards) {
            Intent intent = new Intent(this, GameOverResults.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//closing next activity
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//
            startActivity(intent);
            finish();

        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
