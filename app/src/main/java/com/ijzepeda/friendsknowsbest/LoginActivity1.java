package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity1 extends AppCompatActivity {
    static String TAG= "LoginActivity1";
EditText emailEditText, passwordEditText;
    Button registerBtn, loginBtn;
    LoginButton facebookBtn;
    FirebaseApp firebaseApp;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
CallbackManager callbackManager;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login1);

        context =getApplication();
        emailEditText=(EditText)findViewById(R.id.emailEditText);
        passwordEditText=(EditText)findViewById(R.id.passwordEditText);
        loginBtn=(Button)findViewById(R.id.loginBtn);
        registerBtn=(Button)findViewById(R.id.registerBtn);
        facebookBtn=(LoginButton)findViewById(R.id.fbLogin);
//        facebookBtn=(Button)findViewById(R.id.facebookBtn);

        firebaseApp=FirebaseApp.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        mAuthListener=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user !=null){
                    //user is signed in
                    Log.d("login","onAuthStateChanfed:signed_in"+user.getUid());
                }else{
                    //user is segned out
                    Log.d("Login","onAuthStateChanged:signedOut");
                }
            }
        };


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(emailEditText.getText().toString()) && TextUtils.isEmpty(passwordEditText.getText().toString()) ){
                    Toast.makeText(getApplication(),"Please fill all fields",Toast.LENGTH_SHORT).show();
                }
                else{
                    register(emailEditText.getText().toString(),passwordEditText.getText().toString());

                }
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if(!validateForm()){
            return;//skip step
        }
        login(emailEditText.getText().toString(),passwordEditText.getText().toString());

    }
});



        callbackManager= CallbackManager.Factory.create();
        facebookBtn.setReadPermissions("email","public_profile");
        facebookBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess"+loginResult);
                facebookLogin(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
//        facebookBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                facebookLogin(loginResult.getAccessToken());
//            }
//        });
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
                            Toast.makeText(LoginActivity1.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(LoginActivity1.this, "Authentication with Facebook Success!",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
private void logout(){
    FirebaseAuth.getInstance().signOut();
}


    private void login(String email,String password){
firebaseAuth.signInWithEmailAndPassword(email,password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
           if(!task.isSuccessful()){
               Toast.makeText(context,"Something went wrong, please try again!",Toast.LENGTH_SHORT).show();
           }
                else{
               Toast.makeText(context,"Success!",Toast.LENGTH_SHORT).show();

           }

            }
        });
    }

    public void showProgressDialog(){}
    private void register(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        // [START sign_in_with_email]
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmailAndPassword:onComplete:" + task.isSuccessful());

                        // If register fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "createUserWithEmailAndPassword:failed", task.getException());
                            Toast.makeText(LoginActivity1.this,"auth_failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
//                            mStatusTextView.setText(R.string.auth_failed);
                            Toast.makeText(LoginActivity1.this,"startExclude",
                                    Toast.LENGTH_SHORT).show();
                        }
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }
    private boolean validateForm() {
        boolean valid = true;

        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Valid email required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password) || password.length()<6) {
            passwordEditText.setError("6 character password required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
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
