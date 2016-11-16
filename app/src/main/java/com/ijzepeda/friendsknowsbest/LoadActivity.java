package com.ijzepeda.friendsknowsbest;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ijzepeda.friendsknowsbest.Helpers.GamesRecyclerAdapter;
import com.ijzepeda.friendsknowsbest.models.Game;
import com.ijzepeda.friendsknowsbest.widget2.WidgetProvider2;

import java.util.ArrayList;
import java.util.List;

import static com.ijzepeda.friendsknowsbest.Utils.CHILD_GAMES;
import static com.ijzepeda.friendsknowsbest.Utils.REF_GAME;
import static com.ijzepeda.friendsknowsbest.Utils.REF_USERS;

public class LoadActivity extends AppCompatActivity {

    //RecyclerView loadGameRecyclerView;
    GamesRecyclerAdapter gamesRecyclerAdapter;
    Context context;

    private static String usermail="";
    private static String username="";
    private static String useruid="";
    private RecyclerView ordersRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    public List<Game> gamesList=new ArrayList<>();
    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseGameRef;
    private DatabaseReference databaseUsersRef;
    private DatabaseReference databaseRootRef;
    List<String>userGameList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_load);
        context = getApplicationContext();
       // loadGameRecyclerView=(RecyclerView)findViewById(R.id.loadgameRecyclerView);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));

//RecyclerView
        ordersRecyclerView=(RecyclerView)findViewById(R.id.loadgameRecyclerView);
        linearLayoutManager=new LinearLayoutManager(this);

        //FIREBASE
        app=FirebaseApp.getInstance();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        //        get database
        databaseRootRef=FirebaseDatabase.getInstance().getReference().getRoot();
        // -- reference to table in database
        databaseGameRef =database.getReference(REF_GAME);
        databaseUsersRef =database.getReference(REF_USERS);

        //UserDetails
        //should I save them on the device? or keep veriifying userAuth?
        usermail=auth.getCurrentUser().getEmail();
        username=auth.getCurrentUser().getDisplayName();
        useruid=auth.getCurrentUser().getUid();

// I better create a table for relation. within users add a field for CurrentGames, and store games. then using thos gameids, filetr the games to load
  // READ ALL RECORDS AND SAVE ONLY MATCH?[MORE PETITIONS LESS ACTIONS] or REQUEST MULTIPLE TIMES TO FETCH SPECIFIC RECORDS?[LESS ACTIONS BUT MORE METITIONS]

        databaseUsersRef.child(useruid).child(CHILD_GAMES).
                addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                       userGameList.add(childSnapshot.getValue().toString());



// String gameuid=childSnapshot.getRef().toString();
            databaseGameRef.child(childSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        Game gameTemp = dataSnapshot.getValue(Game.class);
                    gameTemp.setUid(dataSnapshot.getKey());

                        gamesList.add(gameTemp);
                    gamesRecyclerAdapter.notifyDataSetChanged();

                    //Widget: save games to shared prefs.
                    //todo instead of saving them on shared prefs do it on content provider
//                    if(Utils.getInstance().getWidgetGameFromList(gameTemp.getUid())==null){
//                        Utils.getInstance().addGameToWidgetList(gameTemp);//CHECK : WIDGET LIST
                    if (Utils.getInstance().getWidgetGameFromListContentProvider(gameTemp.getUid(),getApplication()) == null) {
                        Utils.getInstance().addGameToWidgetListContentProvider(gameTemp,getApplication());//CHECK : WIDGET LIST
                        {
                            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider2.class));
                            WidgetProvider2 myWidget = new WidgetProvider2();
                            myWidget.onUpdate(getApplication(), AppWidgetManager.getInstance(getApplication()),ids);

                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
                  }
          }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });


        //setting up the adapters
        ordersRecyclerView.setLayoutManager(linearLayoutManager);
        gamesRecyclerAdapter=new GamesRecyclerAdapter(gamesList);
        gamesRecyclerAdapter.notifyDataSetChanged();
        ordersRecyclerView.setAdapter(gamesRecyclerAdapter);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
