package com.ijzepeda.friendsknowsbest;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ijzepeda.friendsknowsbest.widget.DetailWidgetProvider;
import com.ijzepeda.friendsknowsbest.widget.LoremViewsFactory;
import com.ijzepeda.friendsknowsbest.widget.WidgetProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.id;
import static java.security.AccessController.getContext;

public class LoadActivity extends AppCompatActivity {

    //RecyclerView loadGameRecyclerView;
    GamesRecyclerAdapter gamesRecyclerAdapter;
    Context context;

    private static String usermail="";
    private static String username="";
    private static String useruid="";
    private static String uid="FAKEUID123";
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
        setContentView(R.layout.activity_load);
        context = getApplicationContext();
       // loadGameRecyclerView=(RecyclerView)findViewById(R.id.loadgameRecyclerView);

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
        databaseGameRef =database.getReference("Game");
        databaseUsersRef =database.getReference("Users");

        //UserDetails
        //should I save them on the device? or keep veriifying userAuth?
        usermail=auth.getCurrentUser().getEmail();
        username=auth.getCurrentUser().getDisplayName();
        useruid=auth.getCurrentUser().getUid();

//todo I better create a table for relation. within users add a field for CurrentGames, and store games. then using thos gameids, filetr the games to load
        //TODO READ ALL RECORDS AND SAVE ONLY MATCH?[MORE PETITIONS LESS ACTIONS] or REQUEST MULTIPLE TIMES TO FETCH SPECIFIC RECORDS?[LESS ACTIONS BUT MORE METITIONS]

        databaseUsersRef.child(useruid).child("games").
                addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                      Log.d("gameA,User key", childSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
                      Log.d("gameA,User ref", childSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
                      Log.d("gameA,User val", childSnapshot.getValue().toString()); //< Contains the whole json:.
                      userGameList.add(childSnapshot.getValue().toString());



// String gameuid=childSnapshot.getRef().toString();
            databaseGameRef.child(childSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        Game gameTemp = dataSnapshot.getValue(Game.class);

                    gameTemp.setUid(dataSnapshot.getKey().toString());//todo need to update

                        gamesList.add(gameTemp);
                    gamesRecyclerAdapter.notifyDataSetChanged();

                    if(Utils.getInstance().getWidgetGameFromList(gameTemp.getUid())==null){
                        Utils.getInstance().addGameToWidgetList(gameTemp);//CHECK : WIDGET LIST
                        {
//it works but triggerson receive
                            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
                            WidgetProvider myWidget = new WidgetProvider();
                            myWidget.onUpdate(getApplication(), AppWidgetManager.getInstance(getApplication()),ids);

                        }
                        Log.e("Saved value", "for widget on loadActivity having Utils.getGame:" + Utils.getInstance().getWidgetGameFromList(gameTemp.getUid()).getName());
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


        //settign up the adapters
        ordersRecyclerView.setLayoutManager(linearLayoutManager);
        gamesRecyclerAdapter=new GamesRecyclerAdapter(gamesList);
        gamesRecyclerAdapter.notifyDataSetChanged();
        ordersRecyclerView.setAdapter(gamesRecyclerAdapter);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent inten=new Intent(this,MainActivity.class);
        startActivity(inten);
        finish();
    }
}
