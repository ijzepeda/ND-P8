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
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.message;

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
        context=this;
        //retrieveGameDetails
        currentGameID=getIntent().getStringExtra(GAME_ID);
        currentDeckID=getIntent().getStringExtra(DECK_ID);
        currentCard=getIntent().getIntExtra(CURRENT_CARD_ID,0);//Todo check this getCurrentCard());
        currentDeckCard=getIntent().getIntExtra(CURRENT_DECK_CARD_ID,0);
        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number
//        gameTotalCards=getIntent().getIntExtra(TOTAL_CARDS_ID, (getResources().getStringArray(R.array.category_romantic)).length);//tdod CHECk it may cause a problem if this value is different, but still no probaility to fetch null number
Log.e("ResultsActivity","currentCard from bundle is:"+currentCard);

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
        if(username==null || username.equals(null) || username.equals("")){
            username=Utils.getInstance().getValue(getApplication(),"username");
        }
        useruid=auth.getCurrentUser().getUid();
        userPic=auth.getCurrentUser().getPhotoUrl().toString();
        userPicURI=auth.getCurrentUser().getPhotoUrl();

        Log.e("ResultsActivity","Oncreate on  gameuid:"+currentGameID+", card:"+currentCard);


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
                databaseDeckRef.child(currentDeckID).child("card"+currentCard).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        totalUsers=(int)dataSnapshot.getChildrenCount();//todo added on 19-10-0003
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
                winningPlayerPic=""+userVote.getNomineePicUrl();
//                winningPlayerIndex=index;
                winningPlayer=userVote;
            }
            index++;
        }



        refreshWinnerDetails();

//        CheckPlayersAcceptanceStatus();

    }


    public void refreshWinnerDetails(){
        //TODO CHECK I dont have to se a userVote, Because I fecth from acomment and not from the winning user
winnerNameTextView.setText(winningPlayerName);//winningPlayer.getNomineeName());
   Picasso.with(this).load(winningPlayerPic).error(R.drawable.placeholder).into(winnerPic);
        String[] mQuotes ;//= getResources().getStringArray(R.array.category_romantic);
        mQuotes = getResources().getStringArray(R.array.category_romantic);
        String quote= mQuotes[currentDeckCard];
        String completeMessage;
        String winnerTitle;
//        if(username.equals(winningPlayerName)) { //TODO CHANGE TO UID
        if(useruid.equals(winningPlayerUID)) { //TODO CHANGE TO UID
            completeMessage=getString(R.string.im_more_likely_to) + quote.substring(2);
            winnerTitle=getString(R.string.you_won);
            }else{
            completeMessage = "Your friends thought that " + winningPlayerName + " is more likely to :" + quote.substring(2);
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


        databaseDeckRef.child(currentDeckID).child("card"+currentCard).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
        if(childSnapshot.child("nomineeName").getValue().toString().equals(winningPlayerName)) {
            Log.e("~~~~~~>fetchcomment","for winner childSnapshot.child(\"name\").getValue()"+childSnapshot.child("name").getValue().toString()+", voto por:"+childSnapshot.child("nomineeName").getValue().toString()+" message:"+childSnapshot.child("message"));

//            playersList.add(childSnapshot.child("useruid").getValue().toString());//childSnapshot.getValue().toString());
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
//    RecyclerView playersRecyclerView;
    RecyclerView playersFromDeckRecyclerView;
    public List<String> playersList=new ArrayList<>();
//    public List<UserVote> playersListFromDeck=new ArrayList<>();
//    public List<String> playersThatCommentedToWinnerList=new ArrayList<>();//TODO!!!!!!!!
    private LinearLayoutManager linearLayoutManager;

    public void viewComment(String selectedPlayer, String selectedPlayerUid, String message){
        int userVotePosition=0;
        for(int i=0;i<userVotesList.size();i++){
//            if(userVotesList.get(i).getName().equals(selectedPlayer)) {
            if(userVotesList.get(i).getUseruid().equals(selectedPlayerUid)) {  //Si voto por el?
                userVotePosition = i;
            }
        }

//Drawable newIcon=new BitmapDrawable(String.valueOf(android.R.drawable.ic_dialog_alert));//getDrawable(android.R.drawable.ic_dialog_alert);
//Picasso.with(this).load(winningPlayerPic).placeholder(newIcon);
        Log.e("Result:ViewComment","viewcomment() message received:"+message+", message parsed from userVotesList:"+userVotesList.get(userVotePosition).getMessage());
        if(!message.equals(""))
        alertDialog=  new AlertDialog.Builder(this)
                .setTitle(selectedPlayer+ " says...")
//                .setMessage("String selectedPlayer is going to be the UserVote object, and parse elements here: userVote.getMessage()")
                .setMessage(message+"!!!")
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
//        playersRecyclerView=(RecyclerView)findViewById(R.id.playerCommentsRecyclerView);
        playersFromDeckRecyclerView=(RecyclerView)findViewById(R.id.playerCommentsRecyclerView);
        linearLayoutManager=new LinearLayoutManager(this);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2,LinearLayoutManager.HORIZONTAL,false);
        GridLayoutManager gridLayoutVerticalManager=new GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        GridLayoutManager gridLayoutManager=new GridLayoutManager(GameActivity.this, 2,LinearLayoutManager.HORIZONTAL,false);

//Load players
        databaseGameRef.child(currentGameID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               ////todo testin  currentGame = dataSnapshot.getValue(Game.class);
              /////todo testing  totalUsers=currentGame.getUsers().size();
Log.e("FETCHING USERS","~~~~~~~~~~PLAYERS LOADED ARE totalUsers:"+totalUsers);
                CheckPlayersAcceptanceStatus();
//                totalUsers=(int)dataSnapshot.getChildrenCount();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /**

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

        Log.e("FETCHING USERS","~~~~~~~~~~PLAYERS LOADED ARE totalUsers:"+totalUsers);
        CheckPlayersAcceptanceStatus();
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
        playersFromDeckRecyclerView.setLayoutManager(gridLayoutVerticalManager);
//        playersRecyclerAdapter =new PlayersInGameRecyclerAdapter(playersList,getApplication(),true);
        playersRecyclerAdapter =new PlayersInGameRecyclerAdapter(userVotesList,getApplication());
        playersRecyclerAdapter.notifyDataSetChanged();
        playersFromDeckRecyclerView.setAdapter(playersRecyclerAdapter);

    }

    //useful to continue
    int totalUsers;
    int totalAccepted;
    boolean alreadyAccepted=false;
    public void CheckPlayersAcceptanceStatus(){
        Log.e("checkGame()","About to start reading changes");
        totalAccepted=0;
//        totalUsers=currentGame.getNoUsers();//is it null?
        databaseDeckRef.child(currentDeckID).child("card"+currentCard).child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                alreadyAccepted=false; //startloading and return to false, if found the match, then is true
                //refresh recyclerview adapter.
                playersRecyclerAdapter.notifyDataSetChanged();//it was commented on 18-10-16.2344
                //determine if all players have voted
                boolean userAccepted=false;
                if(dataSnapshot.child("acceptResult").exists()) { //extended to bottom
                    userAccepted = Boolean.parseBoolean(dataSnapshot.child("acceptResult").getValue().toString());
                    if (userAccepted) {
                        if (dataSnapshot.child("name").equals(username)) {
                            alreadyAccepted = true;
                        }
                        totalAccepted++;
                    }
                    if (totalAccepted >= totalUsers) {
                        Log.e("~~~RESULTS","~~~~~~~~~~Calling endcard from Added totalAccepted:"+totalAccepted+" totalUsers:"+totalUsers);

                        endCard();
                    }


                    /*if (userAccepted) {
                        //TODO CMABIAR QUE CHEQUE POR NOMBRE A UID!!!!!!!~~~~~~~~~~~~~~~~####################################************5t
                        if (dataSnapshot.child("name").equals(username)) {
                            alreadyAccepted = true;
                        } else {
                            alreadyAccepted = true;//you just voted!
                            totalAccepted++;
                        }
                        Log.e("ResultActivity","onChildAdded Parsing uservotes, totalAccepted"+totalAccepted+", totalUsers:"+totalUsers);

                        if (totalAccepted >= totalUsers) {
                            Log.e(">>checkGame...()", "onChildAdded All players have accepted! continue");
//                    showResult();
                            endCard();

                        }
                    }*/

                }//extended up tohere
            }


            /**Snapshot retrives only the child [in this case the USer] that changed. therefore, I just need to look for voted, not users/voted*/
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                alreadyAccepted=false; //startloading and return to false, if found the match, then is true

//                playersRecyclerAdapter.notifyDataSetChanged();
                //determine if all players have voted
                boolean userAccepted=false;
                if(dataSnapshot.child("acceptResult").exists()) {//extend to bottom
                    userAccepted = Boolean.parseBoolean(dataSnapshot.child("acceptResult").getValue().toString());
                    if (userAccepted) {
                        if (dataSnapshot.child("name").equals(username)) {
                            alreadyAccepted = true;
                        }
                        totalAccepted++;
                    }
                    if (totalAccepted >= totalUsers) {
                        Log.e("~~~RESULTS","~~~~~~~~~~Calling endcard from Changed totalAccepted:"+totalAccepted+" totalUsers:"+totalUsers);
                        endCard();
                    }
                    /** if (userAccepted) {
                        if (dataSnapshot.child("name").equals(username)) {
                            alreadyAccepted = true;
                        } else {
                            alreadyAccepted = true;//you just voted!
                            totalAccepted++;
                        }
//AHUEVO DEBE CHECAR SI ESTE USER HA VOTADO, si aun no vota, todavia no votan todos
                        Log.e("ResultActivity","onChildChanged Parsing uservotes, totalAccepted"+totalAccepted+", totalUsers:"+totalUsers);
                        if (totalAccepted >= totalUsers) {
                            Log.e(">>checkGame...()", "onChildChanged All players have accepted! continue");
//                    showResult();
                            endCard();
                        }
                    }*/
                } //extended up to here
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
        //TODO debo quitar este ffalse para terminar la prueba
//if(false)
        Log.e("End card","gameTotalCards:"+gameTotalCards+", currentCard:"+currentCard);
        //Si es igual o mayor, el numero de cartas, si llegue al tope, mandar a game over
        currentCard++;
if(currentCard<=gameTotalCards) {
    databaseGameRef.child(currentGameID).child("currentCard").setValue(currentCard);
    Intent intent = new Intent(this, GameActivity.class);
    intent.putExtra(GAME_ID, currentGameID);
    intent.putExtra(DECK_ID, currentDeckID + "");
    intent.putExtra(CURRENT_CARD_ID, currentCard );//+1
    intent.putExtra(TOTAL_CARDS_ID, gameTotalCards);
    intent.putExtra(CURRENT_DECK_CARD_ID, currentDeckCard);
    startActivity(intent);
    finish();
}else{
    Intent intent=new Intent(this,GameOverResults.class);
//    Intent intent=new Intent(this,MainActivity.class);
    startActivity(intent);
    finish();
}
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
