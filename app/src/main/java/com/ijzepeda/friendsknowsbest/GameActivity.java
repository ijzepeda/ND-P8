package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IntegerRes;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.ijzepeda.friendsknowsbest.widget.WidgetProvider;
import com.ijzepeda.friendsknowsbest.widget2.WidgetProvider2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {
    //Bundle details
String currentGameID;
String currentDeckID;
int gameTotalCards;
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
//    private DatabaseReference databaseDeckRef;
//    private DatabaseReference databaseRootRef;

    //CardSwipe
    AlertDialog.Builder alertDialog;
    SwipeDeckAdapter adapter;
    SwipeDeck cardStack;
    ArrayList<Card> cards=new ArrayList<>();
    private String[] mQuotes ;//= getResources().getStringArray(R.array.category_romantic);
    int displayCardNo=0;
    String category="Misc";

//int currentCard; //Priority to Update and sync
//Views
    TextView commentTextView;
    ImageButton sendBtn;

    //Game Variables
    int totalVotes;
    boolean alreadyVoted=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);



//FIREBASE
        app=FirebaseApp.getInstance();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        //        get database
//        databaseRootRef=FirebaseDatabase.getInstance().getReference().getRoot();
        // -- reference to table in database
        databaseGameRef =database.getReference("Game");
        databaseDeckRootRef =database.getReference("Deck");
//        databaseDeckRef =databaseDeckRootRef.child(currentGameID);


        //retrieveGameDetails
        currentGameID=getIntent().getStringExtra(GAME_ID);
        currentDeckID=getIntent().getStringExtra(DECK_ID);
        currentCard=getIntent().getIntExtra(CURRENT_CARD_ID,0);//Todo check this getCurrentCard());
        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number

//retrievegameDetailsFromWidget
//        Game gameToLoad=getIntent().getParcelableExtra(WidgetProvider.EXTRA_GAME);
        Game gameToLoad=getIntent().getParcelableExtra(WidgetProvider2.EXTRA_WORD);
if(gameToLoad!=null){
    currentGameID=gameToLoad.getUid();
    currentDeckID=gameToLoad.getDeckId();
    currentCard=gameToLoad.getCurrentCard();
    gameTotalCards=gameToLoad.getNoCards();

}
Log.e("GameActivity","Oncreate on  gameuid:"+currentGameID+", card:"+currentCard);//TODO CHECK BORRAR DELETE
       FirebaseUser mFirebaseUser = auth.getCurrentUser();
//        if (mFirebaseUser == null) {
        Log.e("is null","mFirebaseUser"+mFirebaseUser);

        if( mFirebaseUser==null){
            Log.e("insidemFirebaseUser","mFirebaseUser"+mFirebaseUser);

//            currentGameID==null ||
//            return to main activity .getEmail()
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
//        commentTextView.setFocusable(false);//avoid writting message before selecting

        //DIALOG
//        alertDialog=  new AlertDialog.Builder(this)
//                .setTitle("Game Over")
//                .setMessage("You should start a new game")
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        Intent intent=new Intent(getApplication(),NewGame.class);
//                        startActivity(intent);
////                        finish();
//                        return;
//                    }
//                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
////                        Intent intent=new Intent(getApplication(),MainActivity.class);
////                        startActivity(intent);
////                        finish();
////                        return;
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert);



        //UserDetails
        //should I save them on the device? or keep veriifying userAuth?
        usermail=auth.getCurrentUser().getEmail();
        username=auth.getCurrentUser().getDisplayName();
        useruid=auth.getCurrentUser().getUid();
        userPic=auth.getCurrentUser().getPhotoUrl()!=null?auth.getCurrentUser().getPhotoUrl().toString():"";

        //GetCurrentGame details
//        databaseGameRef.child(currentGameID)
      loadGame();

     //AQUI IBA EL Loader del DECK
     loadDeck();

        //CARDSWIPE
        cardStack.setEventCallback(new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                fetchCard();
                Log.e("MainActivity", "card was swiped left, position in adapter: " + position);
            }

            @Override
            public void cardSwipedRight(int position) {
                fetchCard();
                Log.e("MainActivity", "card was swiped right, position in adapter: " + position);
                //testData.add(""+i++);
            }
            @Override
            public void cardsDepleted() {
                Log.e("MainActivity", "no more cards");
                alertDialog.show();
            }
            @Override
            public void cardActionDown() {
            }
            @Override
            public void cardActionUp() {
            }
        });

//       adapter = new SwipeDeckAdapter(cards, this);
//        cardStack.setAdapter(adapter);

        //FETCHPLAYERS
        fetchPlayers();

        //StartGAME Interaction
//        checkGameAndPlayerStatus();//Load after fectch player is done
    }




    public  void createCard(int cardNo){
        String quote= mQuotes[cardNo];
        Card cartita=new Card(displayCardNo++, Integer.parseInt(quote.substring(0,1)),quote,category,false);//tenia un 0,1
//        Integer.parseInt(quote.substring(0,1));
        cards.add(cartita);
//TODO no puedo notificar        adapter.notifyDataSetChanged();

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
                    int cardNo =0+( Integer.parseInt("" + childSnapshot.child("card").getValue()));
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

    public void loadGame(){

        //GetCurrentGame details

//        databaseGameRef.child(currentGameID)

        //-----*****
        /**
         databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
        //                for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
        Log.e("~~~>>>>","About to place GAME object from DB");
        currentGame = dataSnapshot.getValue(Game.class);
        Log.e("~~~>>>>","Just place GAME object from DB:"+currentGame.getName());

        //--
        //Aqui puse el LOADER del DECK
        databaseDeckRootRef.child(currentGame.getDeckId()).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
        Log.e(">>~~>>","Posible condition to stop on current card:"+childSnapshot.getKey());
        int cardNo=(int) childSnapshot.child("card").getValue();
        createCard(cardNo);
        }
        adapter = new SwipeDeckAdapter(cards, context);
        cardStack.setAdapter(adapter);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
        });
        //--
        //                }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
        });
         */
        /////---------------------------------****

    }

    //-------    Players RecyclerView
    PlayersInGameRecyclerAdapter playersRecyclerAdapter;
    RecyclerView playersRecyclerView;
    public List<String> playersList=new ArrayList<>();

    public void fetchPlayers(){
        fetchPlayersCommented();
        playersRecyclerView=(RecyclerView)findViewById(R.id.playersRecyclerView);
        linearLayoutManager=new LinearLayoutManager(this);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2,LinearLayoutManager.HORIZONTAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        GridLayoutManager gridLayoutManager=new GridLayoutManager(GameActivity.this, 2,LinearLayoutManager.HORIZONTAL,false);

//Load players
        databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
                currentGame = dataSnapshot.getValue(Game.class);
                Map<String,Object> gameUsers= currentGame.getUsers();
  }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
//final List gameUserNamesList=new ArrayList();
        databaseGameRef.child(currentGameID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalUsers=(int)dataSnapshot.getChildrenCount(); //TODO is giving me error.. actually returns 0
                Log.e("totalUsers","totalUsers cant be 0, but now is:"+totalUsers+" -----------------------------------------------------------");
                Log.e("totalUsers","dataSnapshot.getChildrenCount()"+dataSnapshot.getChildrenCount()+" -----------------------------------------------------------");
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

                //Having players fetched continue
                checkGameAndPlayerStatus();

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
public void fetchPlayersCommented(){
    //currentGameID
//        databaseGameRef.child("GAME123").addListenerForSingleValueEvent(new ValueEventListener() {
    /**Log.e("~~~>","currentGameID;"+currentGameID);
     databaseGameRef.child(currentGameID+"").addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
    //                currentGame = dataSnapshot.getValue(Game.class);
    //                Log.e("~~~>>>>","Just place GAME object from DB:"+currentGame.getName());
    Log.e("~~~>>>>","Just place GAME object from DB:"+dataSnapshot.getKey().toString());
    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
    Log.d("gameA,User key", childSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
    Log.d("gameA,User ref", childSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
    Log.d("gameA,User val", childSnapshot.getValue().toString()); //< Contains the whole json:.
    }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
    });*/

}

    public void selectedPlayer(final String selectedPlayer){
//        commentTextView.setInputType(InputType.TYPE_CLASS_TEXT);
//        commentTextView.setFocusable(true);
        commentTextView.setHint("Why would you vote for \n"+selectedPlayer);
        sendBtn.setClickable(true);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sendBtn.setClickable(false);
                sendBtn.setColorFilter((R.color.bel_lightgrey_text));
                commentTextView.setFocusable(false);
                if(!alreadyVoted) {
                    alreadyVoted=true;
                    UserVote userVote = new UserVote(username, useruid, userPic, commentTextView.getText().toString(), true, selectedPlayer + "UID", selectedPlayer,selectedPlayer+"userPic",false);
                    databaseDeckRootRef.child(currentDeckID).child("card" + currentGame.getCurrentCard()).child("users").child(useruid).setValue(userVote);
                }else{
                    Toast.makeText(context, R.string.wait_player_vote, Toast.LENGTH_SHORT).show();
                    if (totalVotes >= totalUsers) {
                        Log.e("clicked button", "verify if the users have voted");
                        showResult();
                    }
                }
            }
        });
    }


    /**
     * Using this method i might encounter a problem if i edit a value and leave. because noone will notice the change on the already voted users
     * I can use a read all voted, and then the listener for changes. I believe added, is in this instance. so I dont need to do it twice*/
    public void checkGameAndPlayerStatus(){
        Log.e("checkGame()","About to start reading changes");
        totalVotes=0;
        databaseDeckRootRef.child(currentDeckID).child("card"+currentCard).child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 alreadyVoted=false; //startloading and return to false, if found the match, then is true
                playersRecyclerAdapter.notifyDataSetChanged();

                //determine if all players have voted
                boolean userVoted=false;
                if(dataSnapshot.child("voted").exists()) {
                    userVoted = Boolean.parseBoolean(dataSnapshot.child("voted").getValue().toString());
                    Log.e("GAME ACTIVITY","checkgame&players onchild added userVoted:"+userVoted);
//                } extiendo este if, hasta abajo... si no existe el userVote, no checar ni subir, ni avanzar... esperar hasta que lo haga
                    if (userVoted) {
                        Log.e("GAME ACTIVITY","checkg onchild Added: inside if because he VOTED"+userVoted);
//                       else
//                        {
//                            alreadyVoted = true;//you just voted!
//                          //  totalVotes++;
//                        }
//                        Log.e("GameActivity","checkGameAndPlayerStatus onChildChanged totalVotes:"+totalVotes+", totalUsers:"+totalUsers);
//                    }else{
                        if (dataSnapshot.child("name").equals(username)) {
                            alreadyVoted = true;
                        }
                        totalVotes++;
                    }
                    //                    if (!userVoted) {
//                        //TODO CMABIAR QUE CHEQUE POR NOMBRE A UID!!!!!!!~~~~~~~~~~~~~~~~####################################************5t
//                        if (dataSnapshot.child("name").equals(username)) {
//                            alreadyVoted = true;
//                        } else {
//                            alreadyVoted = true;//you just voted!
//                            totalVotes++;
//                        }
//                    }
//                    Log.e("GameActivity","checkGameAndPlayerStatus onChildAdded totalVotes:"+totalVotes+", totalUsers:"+totalUsers);
//                    if (totalVotes >= totalUsers) {
//                        Log.e(">>checkGame...()", "onChildAdded All players have voted! continue**********************************************");
//                        showResult();
//                    }
                }  //hasta aqui lo expandi
//                156165165
                Log.e("GameActivity","checkGameAndPlayerStatus onChildAdded totalVotes:"+totalVotes+", totalUsers:"+totalUsers);
                if (totalVotes >= totalUsers) {
                    Log.e(">>checkGame...()", "onChildAdded All players have voted! continue**********************************************");
                    showResult();
                }
            }

//Asunto aqui
/**Snapshot retrives only the child [in this case the USer] that changed. therefore, I just need to look for voted, not users/voted*/
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("checkGameAndPlayer","onChildChanged,User key"+ dataSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
                Log.d("checkGameAndPlayer","onChildChanged,User ref"+ dataSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
                Log.d("checkGameAndPlayer","onChildChanged,User val"+ dataSnapshot.getValue().toString()); //< Contains the whole json:.


                alreadyVoted=false; //startloading and return to false, if found the match, then is true

                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean readingUserVoted=false;
                if(dataSnapshot.child("voted").exists()) {//expandido hasta abajo
                    readingUserVoted = Boolean.parseBoolean(dataSnapshot.child("voted").getValue().toString());
                    Log.e("GAME ACTIVITY","checkgame&players onchild Changed userVoted:"+readingUserVoted);

                    if (readingUserVoted) {
//                       else
//                        {
//                            alreadyVoted = true;//you just voted!
//                          //  totalVotes++;
//                        }
//                        Log.e("GameActivity","checkGameAndPlayerStatus onChildChanged totalVotes:"+totalVotes+", totalUsers:"+totalUsers);
//                    }else{
                        if (dataSnapshot.child("name").equals(username)) {
                            alreadyVoted = true;
                        } totalVotes++;
                    }
//                        if (totalVotes >= totalUsers) {
//                            Log.e(">>checkGame...()", "onChildChanged All players have voted! continue****************************************");
//                            showResult();
//                        }

                   //delete erase check debug todo el if totalvotes>=totalusers iba aqui
                } //se expadio hasta aqui
                //trigger next move
                if (totalVotes >= totalUsers) {
                    Log.e(">>checkGame...()", "onChildChanged All players have voted! continue****************************************");
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
    startActivity(intent);///todo not enble to debug
    finish();

}

    //todo
    //Fill Widget Data!
    public void fillWidgetData(){

    }



    int fetchedCurrentCard;
    public int getCurrentCard(){
        fetchedCurrentCard=0;
        //Load the card from Child.
        //TODO FIX ERROR Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'com.google.firebase.database.DatabaseReference com.google.firebase.database.DatabaseReference.child(java.lang.String)' on a null object reference
//    at com.ijzepeda.friendsknowsbest.GameActivity.getCurrentCard(GameActivity.java:277)

        databaseGameRef.child(currentGameID).child("currentCard").addListenerForSingleValueEvent(new ValueEventListener() {
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


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
