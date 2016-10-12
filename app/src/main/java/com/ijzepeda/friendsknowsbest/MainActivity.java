package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class MainActivity extends AppCompatActivity {
FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
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

        setContentView(R.layout.activity_main);

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
            //it is going to be automatic login
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//            return;
        } else {
                    Log.e("MainActivity","mFirebaseUser is:"+mFirebaseUser.getDisplayName());

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

        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("","---------------psolsalgo");
                logout();
            }
        });
        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent offlineMode=new Intent(getApplication(), LoginActivity.class);
                startActivity(offlineMode);
                finish();
                return;
//                Toast.makeText(getApplication(),getString(R.string.login_first),Toast.LENGTH_SHORT).show();
            }
        });
        quickGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent offlineMode=new Intent(getApplication(), Offline.class);
                startActivity(offlineMode);
                finish();
                return;
            }
        });

    }


    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Utils.getInstance().clearSharedPreference(this);//.save(this,usermail,"email");

//        firebaseAuth.signOut();// if you are in this part, you can only logout
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//        finish();
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }
}
