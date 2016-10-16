package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.daprlabs.cardstack.SwipeDeck;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
String currentGameID;
String currentDeckID;
    private static String GAME_ID="game_id";
    private static String DECK_ID="deck_id";
    private static String usermail="";
    private static String username="";
    private static String useruid="";
    Context context;

    //CurrentGame
    private int currentCard;
    private int totalCards;
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
    private DatabaseReference databaseDeckRef;
    private DatabaseReference databaseRootRef;

    //CardSwipe
    AlertDialog.Builder alertDialog;
     SwipeDeckAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        currentGameID=getIntent().getStringExtra(GAME_ID);
        currentDeckID=getIntent().getStringExtra(DECK_ID);
        mQuotes = getResources().getStringArray(R.array.category_romantic);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        context=getApplication();

        //DIALOG
        alertDialog=  new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You should start a new game")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
//                        loadDeck(getString(R.string.categoryromantic));
                        /*cards.add(new Card(0,0,"","",true));
                        cards.add(new Card(0,0,"","",true));
                        cards.add(new Card(0,0,"","",true));*/
//                        final SwipeDeckAdapter adapter = new SwipeDeckAdapter(cards, getApplication());
//                        cardStack.setAdapter(adapter);
                        Intent intent=new Intent(getApplication(),Offline.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        Intent intent=new Intent(getApplication(),MainActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        //FIREBASE
        app=FirebaseApp.getInstance();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        //        get database
        databaseRootRef=FirebaseDatabase.getInstance().getReference().getRoot();
        // -- reference to table in database
        databaseGameRef =database.getReference("Game");
        databaseDeckRootRef =database.getReference("Deck");
//        databaseDeckRef =databaseDeckRootRef.child(currentGameID);

        //UserDetails
        //should I save them on the device? or keep veriifying userAuth?
        usermail=auth.getCurrentUser().getEmail();
        username=auth.getCurrentUser().getDisplayName();
        useruid=auth.getCurrentUser().getUid();

        //GetCurrentGame details
     /**   databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
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
     //AQUI IBA EL Loader del DECK
        databaseDeckRootRef.child(currentDeckID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
                    Log.e(">>~~>>","Posible condition to stop on current card:"+childSnapshot.getKey());
                    int cardNo=Integer.parseInt(""+childSnapshot.child("card").getValue());
                    createCard(cardNo);
                }
                adapter = new SwipeDeckAdapter(cards, context);
                cardStack.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    }

    SwipeDeck cardStack;
    ArrayList<Card> cards=new ArrayList<>();
   private String[] mQuotes ;//= getResources().getStringArray(R.array.category_romantic);
int displayCardNo=0;
    String category="Misc";

    public  void createCard(int cardNo){
        String quote= mQuotes[cardNo];
        Card cartita=new Card(displayCardNo++, Integer.parseInt(quote.substring(0,1)),quote,category,false);
//        Integer.parseInt(quote.substring(0,1));
        cards.add(cartita);
//TODO no puedo notificar        adapter.notifyDataSetChanged();

    }
    public void fetchCard(){
        // cards.add(new Card(0,0,"","",true));
    }


}
