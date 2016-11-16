package com.ijzepeda.friendsknowsbest;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ijzepeda.friendsknowsbest.models.Game;
import com.ijzepeda.friendsknowsbest.widget2.WidgetProvider2;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class MainActivity extends AppCompatActivity   implements
        GoogleApiClient.OnConnectionFailedListener {
FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
private static String TAG="MainActivity";
    private GoogleApiClient mGoogleApiClient;
    String mUsername;
    String mPhotoUrl;
    Button logoutBtn;
    Button onlineBtn;
    Button quickGameBtn;
    Button loadGameBtn, newGameBtn, settingsBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));
//user
        loading=true; //handle backpress first click

        //Bind Views
        loadGameBtn=(Button)findViewById(R.id.loadGameBtn);
        newGameBtn=(Button)findViewById(R.id.newGameBtn);
        settingsBtn=(Button)findViewById(R.id.settingsBtn);
        logoutBtn=(Button)findViewById(R.id.logoutBtn);
        onlineBtn=(Button)findViewById(R.id.online);
        quickGameBtn =(Button)findViewById(R.id.offline);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            //hide buttons
            loadGameBtn.setVisibility(View.GONE);
            newGameBtn.setVisibility(View.GONE);
            settingsBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.GONE);
            onlineBtn.setVisibility(View.VISIBLE);


            //check delete widget data
//            Utils.getInstance().clearWidgetGamesList();//changed on 14-11
            Utils.getInstance().clearWidgetGamesList(getApplication());

        } else {
            Utils.getInstance();
                    Log.d("MainActivity","mFirebaseUser is:"+mFirebaseUser.getDisplayName());
            Utils.getInstance().save(getApplication(),mFirebaseUser.getUid(),"uid");
            Utils.getInstance().save(getApplication(),mFirebaseUser.getDisplayName(),"name");
            Utils.getInstance().save(getApplication(),mFirebaseUser.getEmail(),"email");

            //show buttons
            loadGameBtn.setVisibility(View.VISIBLE);
            newGameBtn.setVisibility(View.VISIBLE);
            settingsBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.VISIBLE);
            onlineBtn.setVisibility(View.GONE);
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
//            fillWidgetGameList();
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent offlineMode=new Intent(getApplication(), LoginActivity.class);
                startActivity(offlineMode);
//                finish();
                return;
            }
        });
        quickGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent offlineMode=new Intent(getApplication(), Offline.class);
                startActivity(offlineMode);
//                finish();
                return;
            }
        });

        newGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newGame=new Intent(getApplication(), NewGame.class);

                startActivity(newGame);
//                finish();
                return;
            }
        });

        loadGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loadGame=new Intent(getApplication(), LoadActivity.class);
                startActivity(loadGame);
//                finish();
                return;
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplication(), Settings.class);
                startActivity(intent);
//                finish();
                return;
            }
        });

        //GET INVITATION
        //THIS PART IS HANDLED IN ADDPLAYERTOGAMEACTIVITY
        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();
        boolean autoLaunchDeepLink = false;//true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);
                                    Log.e(TAG, "getInvitation:deepLink:" + deepLink);
                                    Log.e(TAG, "getInvitation:invitationId:" + invitationId);
                                    // Because autoLaunchDeepLink = true we don't have to do anything
                                    // here, but we could set that to false and manually choose
                                    // an Activity to launch to handle the deep link here.
                                    // ...
//                                  startActivity(intent);//<< is this same one intent/activity
                                }
                            }
                        });

    }


    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Utils.getInstance().clearSharedPreference(this);//.save(this,usermail,"email");

        //Clear Widget data
//        /------------------------------------------------
        Log.e("Widget Logout","Cleaning data");
//        Utils.getInstance().clearWidgetGamesList();//changed on 14-11
        Utils.getInstance().clearWidgetGamesList(getApplication());
//it works but triggerson receive
//Not sure if I need this????..~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider2.class));
                WidgetProvider2 myWidget = new WidgetProvider2();
                myWidget.onUpdate(getApplication(), AppWidgetManager.getInstance(getApplication()),ids);
//        /------------------------------------------------



        LoginManager.getInstance().logOut();
//        firebaseAuth.signOut();// if you are in this part, you can only logout
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//        finish();
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

    }



    public void fillWidgetGameList(){

        FirebaseDatabase.getInstance().getReference("User").child(mFirebaseUser.getUid()).child("games").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Log.d("gameA,User key", childSnapshot.getKey());//  D/User key: -KSZqD6W_kjmOPKwh3i8
                            Log.d("gameA,User ref", childSnapshot.getRef().toString());//   D/User ref: https://tatt-5dc00.firebaseio.com/Ordenes/-KSZqD6W_kjmOPKwh3i8
                            Log.d("gameA,User val", childSnapshot.getValue().toString()); //< Contains the whole json:.
//                            userGameList.add(childSnapshot.getValue().toString());


// String gameuid=childSnapshot.getRef().toString();
                            FirebaseDatabase.getInstance().getReference("Games").child(childSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Game gameTemp = dataSnapshot.getValue(Game.class);

                                    gameTemp.setUid(dataSnapshot.getKey());

//                                    gamesList.add(gameTemp);
//                                    gamesRecyclerAdapter.notifyDataSetChanged();

                                    //Widget: save games to shared prefs.
                                    // instead of saving them on shared prefs do it on content provider
//                                    if (Utils.getInstance().getWidgetGameFromList(gameTemp.getUid()) == null) {
//                                        Utils.getInstance().addGameToWidgetList(gameTemp);//CHECK : WIDGET LIST
                                        if (Utils.getInstance().getWidgetGameFromListContentProvider(gameTemp.getUid(),getApplication()) == null) {
                                            Utils.getInstance().addGameToWidgetListContentProvider(gameTemp,getApplication());//CHECK : WIDGET LIST
                                        {
//it works but triggerson receive
                                            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider2.class));
                                            WidgetProvider2 myWidget = new WidgetProvider2();
                                            myWidget.onUpdate(getApplication(), AppWidgetManager.getInstance(getApplication()), ids);

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
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }




    boolean loading=true;
    @Override
    public void onBackPressed() {
//        if(loading) {
//            Toast.makeText(this, R.string.press_back_to_leave, Toast.LENGTH_SHORT).show();
//            loading=false;
//        }
//        else {
            super.onBackPressed();
//            loading=true;
            finish();
//        }
    }

}
