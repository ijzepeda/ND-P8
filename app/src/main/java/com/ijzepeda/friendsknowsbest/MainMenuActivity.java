package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainMenuActivity extends AppCompatActivity {
//    LoginButton facebookBtn;
    CallbackManager callbackManager;
final static String TAG="mainMenuActivity";
    FirebaseApp firebaseApp;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference firebaseDatabaseRootReference;
    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    private DatabaseReference databaseRootRef;
    private StorageReference storageRef;

    boolean connected=false;
Button logoutBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main_menu);

        //FIREBASE
        app=FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        firebaseApp=FirebaseApp.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        // -- reference to table in database
//    databaseRef=database.getReference("Ordenes");
//        get database
        databaseRootRef=FirebaseDatabase.getInstance().getReference().getRoot();
        firebaseDatabaseRootReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuthListener=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.e("2xxxxx","onauthstatechanged");



//return;
                if(user !=null){
                    //user is signed in
                    Log.e("mainmenuactivity", "segunda instancia del listener");
//                    Log.d("2xlogin","onAuthStateChanfed:signed_in:"+user.getUid());
//                    Utils.getInstance().save(getApplication(),user.getUid(),"uid");
//                    Log.e("2xmauthlistener","name is:"+user.getDisplayName());
//                    Log.e("2xmauthlistener","email is:"+user.getEmail());
//                    Log.e("x2mauthlistener","uid is:"+user.getUid());
//                    Log.e("x2mauthlistener","provider is:"+user.getProviderId());
//                    Log.e("2xmauthlistener","photourl is:"+user.getPhotoUrl());
                    //No need to ask for email again, go directly
                    connected=true;
                }else{
                    //user is segned out
                    Log.d("Login","facebook onAuthStateChanged:signedOut");
//                  logout();
                }
            }
        };
        //FACEBOOK
//        facebookBtn=(LoginButton)findViewById(R.id.fbLogin);
//        callbackManager= CallbackManager.Factory.create();
//        facebookBtn.setReadPermissions("email","public_profile");
//        facebookBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Log.e(TAG, "facebook:onSuccess"+loginResult);
//                facebookLogin(loginResult.getAccessToken());
//            }
//            @Override
//            public void onCancel() {
//                Log.d(TAG, "facebook:onCancel");
//            }
//            @Override
//            public void onError(FacebookException error) {
//                Log.d(TAG, "facebook:onError", error);
//            }
//        });

logoutBtn=(Button)findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("","---------------psolsalgo");
                logout();
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
    private void facebookLogin(AccessToken accessToken){
        Log.d(TAG,"facebookLogin Handling");
        AuthCredential credential= FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG,"signInWithCredential:onComplete"+task.isSuccessful());
                        if(!task.isSuccessful()){
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainMenuActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(MainMenuActivity.this, "Authentication with Facebook Success!",
                                    Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
//                            finish();
//                            startActivity(intent);

                        }
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

}
