package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadActivity extends AppCompatActivity {

    RecyclerView loadGameRecyclerView;
    RecyclerView.Adapter recyclerViewAdapter;
    RecyclerView.LayoutManager recylerViewLayoutManager;
    GamesRecyclerAdapter gamesRecyclerAdapter;
    Context context;

    private static String usermail="";
    private static String username="";
    private static String useruid="";
    private static String uid="FAKEUID123";
    private RecyclerView ordersRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    public List<Game> gamesList=new ArrayList<>();
//    private OrdersRecyclerViewAdapter recyclerAdapter;
    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseGameRef;
    private DatabaseReference databaseUsersRef;
    private DatabaseReference databaseRootRef;
    private StorageReference storageRef;
    DatabaseReference firebaseDatabaseRootReference;
    List<String>userGameList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        context = getApplicationContext();
        loadGameRecyclerView=(RecyclerView)findViewById(R.id.loadgameRecyclerView);


        String[] subjects =
                {
                        "ANDROID",
                        "PHP",
                        "BLOGGER",
                        "WORDPRESS",
                        "JOOMLA",
                        "ASP.NET",
                        "JAVA",
                        "C++",
                        "MATHS",
                        "HINDI",
                        "ENGLISH"};

//        recylerViewLayoutManager = new LinearLayoutManager(context);
//        loadGameRecyclerView.setLayoutManager(recylerViewLayoutManager);
//        recyclerViewAdapter = new RecyclerViewAdapter(context, subjects);
//        loadGameRecyclerView.setAdapter(recyclerViewAdapter);


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

                      //--
//                      for(String game:userGameList){
            databaseGameRef.child(childSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
//            databaseGameRef.child("GAME123").addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        Game gameTemp = dataSnapshot.getValue(Game.class);
                        gamesList.add(gameTemp);
                        gamesRecyclerAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
//        }
                      /**
                      databaseGameRef.child(childSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {

                          @Override
                          public void onDataChange(DataSnapshot dataSnapshot) {
                              for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                  Log.d("ordenesA,User key", childSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
                                  Log.d("ordenesA,User ref", childSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
                                  Log.d("ordenesA,User val", childSnapshot.getValue().toString()); //< Contains the whole json:.
//todo                    Order orderTemp= dataSnapshot.getValue(Order.class); //Incluye el referenceID<<
                                  Game gameTemp= dataSnapshot.child(childSnapshot.getKey()).getValue(Game.class); //Incluye el referenceID<<
//                    gameTemp.setOrderNo(childSnapshot.getKey());//todo actualizar su orderNo, al id de orden> //NO BORRAR se puede usar despues
                                  gamesList.add(gameTemp);
                                  Log.e("ordenesActivity", "Agregue orderTemp a ordersList, esto tieneorderTemp:"+gamesList.toString());
                                  Log.e("ordenesActivity", "Agregue orderTemp a ordersList, orderslist tiene:"+gamesList.size());
                              }
                              gamesRecyclerAdapter.notifyDataSetChanged();
                          }
                          @Override
                          public void onCancelled(DatabaseError databaseError) {
                          }
                      });
                      */
                      //--

Log.e("AddedGame","to list:"+childSnapshot.getValue().toString());
                      }
          }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });


Log.e("LoadActivity","About to load games from usergamelist");

/**
//        for(String game:userGameList){
//            databaseGameRef.child(game).addListenerForSingleValueEvent(new ValueEventListener() {
////            databaseGameRef.child("GAME123").addListenerForSingleValueEvent(new ValueEventListener() {
//
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                        Game gameTemp = dataSnapshot.getValue(Game.class);
//                        gamesList.add(gameTemp);
//                        gamesRecyclerAdapter.notifyDataSetChanged();
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                }
//            });
//        }
        */
        Log.e("LoadActivity","finished loading games from usergamelist");

        //settign up the adapters
        ordersRecyclerView.setLayoutManager(linearLayoutManager);
        gamesRecyclerAdapter=new GamesRecyclerAdapter(gamesList);
        gamesRecyclerAdapter.notifyDataSetChanged();
        ordersRecyclerView.setAdapter(gamesRecyclerAdapter);

/**
//        databaseGameRef.child("users").orderByChild("mail").equalTo(usermail).addListenerForSingleValueEvent(new ValueEventListener() {
        databaseGameRef.child("users").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
//        databaseGameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    Log.d("ordenesA,User key", childSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
                    Log.d("ordenesA,User ref", childSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
                    Log.d("ordenesA,User val", childSnapshot.getValue().toString()); //< Contains the whole json:.
//                    String name = (String) childSnapshot.child("name").getValue();
//                    String message = (String) childSnapshot.child("message").getValue();
//                    Log.e("~~ORdenes","ORden:"+((String)childSnapshot.child("originStreet").getValue())+", orderName:"+((String)childSnapshot.child("orderName").getValue()));
//                    orderName.setText((String) childSnapshot.child("mail").getValue());

//todo                    Order orderTemp= dataSnapshot.getValue(Order.class); //Incluye el referenceID<<
                    Game gameTemp= dataSnapshot.child(childSnapshot.getKey()).getValue(Game.class); //Incluye el referenceID<<
//                    gameTemp.setOrderNo(childSnapshot.getKey());//todo actualizar su orderNo, al id de orden> //NO BORRAR se puede usar despues
                    gamesList.add(gameTemp);
                    Log.e("ordenesActivity", "Agregue orderTemp a ordersList, esto tieneorderTemp:"+gamesList.toString());
                    Log.e("ordenesActivity", "Agregue orderTemp a ordersList, orderslist tiene:"+gamesList.size());
                }


//if No adapter attached; skipping layout it is because it didnt found anything
//Outside this method wont work
                ordersRecyclerView.setLayoutManager(linearLayoutManager);
                gamesRecyclerAdapter=new GamesRecyclerAdapter(gamesList);
                gamesRecyclerAdapter.notifyDataSetChanged();
                ordersRecyclerView.setAdapter(gamesRecyclerAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent inten=new Intent(this,MainMenuActivity.class);
        startActivity(inten);
    }
}
