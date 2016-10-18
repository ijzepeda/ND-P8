package com.ijzepeda.friendsknowsbest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
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

import static android.Manifest.permission.READ_CONTACTS;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static String TAG="LOGIN";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask2 mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mEmailTextView;
    private View mProgressView;
    private View mLoginFormView;
    //Facebook
    LoginButton facebookBtn;

    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    private DatabaseReference databaseRootRef;
    private StorageReference storageRef;
    DatabaseReference firebaseDatabaseRootReference;
    CallbackManager callbackManager;
    FirebaseApp firebaseApp;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser mFirebaseUser;
    FirebaseUser firebaseUser;

    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);

        //FIREBASE
        app=FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        // -- reference to table in database
//    databaseRef=database.getReference("Ordenes");
//        get database
        databaseRootRef=FirebaseDatabase.getInstance().getReference().getRoot();
        firebaseDatabaseRootReference = FirebaseDatabase.getInstance().getReference().child("Users");


        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        mEmailTextView=(EditText)findViewById(R.id.email);

        //FACEBOOK
        facebookBtn=(LoginButton)findViewById(R.id.fbLogin);
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
        mFirebaseUser = auth.getCurrentUser();
//        Log.e("MainActivity","mFirebaseUser is:"+mFirebaseUser.getDisplayName());
        if (mFirebaseUser != null) {
            // Not signed in, launch the Sign In activity
//seems it doesnt work with FACEBOOK loginned
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });



        //Todo load user session from sharedPrefs, if exists, it was logged in, then skip
        Button mEmailLogInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                attemptRegister();

                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                register(email,password);
            }
        });
        mEmailLogInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();

                login(email,password);
//                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        firebaseApp=FirebaseApp.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        mAuthListener=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user !=null){
                    //user is signed in
                    Log.d("login","onAuthStateChanfed:signed_in:"+user.getUid());
                    Utils.getInstance().save(getApplication(),user.getUid(),"uid");
                    Utils.getInstance().save(getApplication(),user.getDisplayName(),"username");
                    Utils.getInstance().save(getApplication(),user.getEmail(),"email");
                    Log.e("mauthlistener","name is:"+user.getDisplayName());
                    Log.e("mauthlistener","email is:"+user.getEmail());
                    Log.e("mauthlistener","uid is:"+user.getUid());
                    Log.e("mauthlistener","provider is:"+user.getProviderId());
                    Log.e("mauthlistener","photourl is:"+user.getPhotoUrl());
                    //No need to ask for email again, go directly
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    showProgress(false);
                    finish();
                    startActivity(intent);

                }else{
                    //user is segned out
                    Log.d("Login","onAuthStateChanged:signedOut");
                }
            }
        };


        if(isEmailValid(Utils.getInstance().getValue(this,"email")) ){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            showProgress(false);
            finish();
            startActivity(intent);

        }

//GETSHA for facebook //TODO DELETE
       // **
//         try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.ijzepeda.friendsknowsbest",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("facebook KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (NoSuchAlgorithmException e) {
//
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        // * /
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
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }else{

                        //TODO somehow verify that if the user exists, then create/fill the user details in database

                            Toast.makeText(LoginActivity.this, R.string.suth_facebook_success,
                                    Toast.LENGTH_SHORT).show();

//                            FirebaseUser firebaseUser=auth.getCurrentUser();
                             firebaseUser=auth.getCurrentUser();
                            Map<String,Object> friendsMap=new HashMap<String, Object>();
                            Map<String,Object>cardsMap=new HashMap<String, Object>();
                            Map<String,Object>gamesMap=new HashMap<String, Object>();

                            //TODO DELETE HARDCODED STUFF
                            friendsMap.put("Friend0","");
                            cardsMap.put("FavCard1","");
                            gamesMap.put("game0","GAME123");//GameUID123
                             user=new User(firebaseUser.getDisplayName().toString(),"0","0",firebaseUser.getEmail().toString(),firebaseUser.getUid(),firebaseUser.getPhotoUrl()!=null?firebaseUser.getPhotoUrl().toString():"FOTOURL",
                                    friendsMap,cardsMap,gamesMap);
                            databaseRef=database.getReference("Users");


//TODO if user doesnt exist, then create it
                            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.hasChild(firebaseUser.getUid())){
                                                                    Map<String,Object> map=new HashMap<String,Object>();
                            map.put(firebaseUser.getUid(),"");
                            databaseRef.updateChildren(map);
                            //Each login all user values get replaced, then I need to update, not set
                            databaseRef.child(firebaseUser.getUid()).setValue(user);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            //TODO KEY CANT BE AN EMAIL!
//                            Map<String,Object> map=new HashMap<String,Object>();
//                            map.put(firebaseUser.getUid(),"");
//                            databaseRef.updateChildren(map);
//                            //Each login all user values get replaced, then I need to update, not set
//                            databaseRef.child(firebaseUser.getUid()).setValue(user);




                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            showProgress(false);
                            finish();
                            startActivity(intent);

                        }
                    }
                });
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mEmailTextView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailTextView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailTextView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailTextView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            /**TODO: THIS WAS on the base activity
             *
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
             */
//        startLogin();
            login(email, password);

        }
    }
//    private void attemptRegister(){
////        if (mAuthTask != null) {
////            return;
////        }
//
//        // Reset errors.
//        mEmailView.setError(null);
//        mPasswordView.setError(null);
//
//        // Store values at the time of the login attempt.
//        String email = mEmailView.getText().toString();
//        String password = mPasswordView.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            mEmailView.setError(getString(R.string.error_field_required));
//            focusView = mEmailView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true);
//            /**TODO: THIS WAS on the base activity
//             *
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
//             */
////        startLogin();
//            register(email, password);
//
//        }
//    }



    private boolean validateForm() {
        boolean valid = true;
        // Store values at the time of the login attempt.
        String email = mEmailTextView.getText().toString();
//        String password = mPasswordView.getText().toString();

//        String email = mail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailTextView.setError("Email invlaido.");
            valid = false;
        } else {
            mEmailTextView.setError(null);
        }

        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(password) || password.length()<6) {
            mPasswordView.setError("Minimo 6 caracteres.");
            valid = false;
        } else {
            mPasswordView.setError(null);
        }

        return valid;
    }

    private void login(String email,String password){
        final String mail=email;

        Log.d(TAG, "login():" + email);
        if (!validateForm()) {
            return;
        }
        showProgress(true);


        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            showProgress(false);
                        }

                        else{
                            saveUserSession(mail,task.toString()); //directly preferences
                            getUser(mail);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthWeakPasswordException) {
                    ((FirebaseAuthException) e).getErrorCode();
                    Toast.makeText(LoginActivity.this, R.string.weak_password, Toast.LENGTH_SHORT).show();
                }else
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    ((FirebaseAuthException) e).getErrorCode();
                    Toast.makeText(LoginActivity.this, R.string.incorrect_data, Toast.LENGTH_SHORT).show();

                }else
                if (e instanceof FirebaseAuthUserCollisionException) {
                    ((FirebaseAuthException) e).getErrorCode();
                    Toast.makeText(LoginActivity.this, R.string.already_registered, Toast.LENGTH_SHORT).show();

                }else
                if (e instanceof FirebaseAuthException) {
                    ((FirebaseAuthException) e).getErrorCode();
                }
            }
    });

    }




    private void register(String email, String password) {
        final String mail=email;
        Log.d(TAG, "register():" + email);
        if (!validateForm()) {
            return;
        }
        showProgress(true);

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
                            Log.w(TAG, "message is:"+ task.getException().getMessage());
                            Toast.makeText(LoginActivity.this,getString(R.string.auth_failed)+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            showProgress(false);

                        }else {
                            saveUserSession(mail,task.toString());
                            getUser(mail);

                            Intent intent = new Intent(getApplicationContext(), RegisterUser.class);
                            showProgress(false);

                            startActivity(intent);
                            finish();
                        }
//                        hideProgressDialog();
                    }
                });
        // [END sign_in_with_email]
    }

public void getUser(String mail){



    firebaseDatabaseRootReference.orderByChild(getString(R.string.email)).equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                String name = (String) childSnapshot.child(getString(R.string.name)).getValue();
//                String mail = (String) childSnapshot.child("mail").getValue();///todo added on 109161617
               Utils utils= Utils.getInstance();
                utils.save(getApplication(),name,getString(R.string.username));
//                utils.save(getApplication(),mail,"email");

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                showProgress(false);
                startActivity(intent);
                finish();

            }}

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

            }

private void saveUserSession(String usermail,String resultTask){
//todo checar el result task, si regresa el que paso? y como obtener el uid
Utils utils= Utils.getInstance();
    utils.save(this,usermail,"email");

}


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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

boolean loading=false;
    @Override
    public void onBackPressed() {
      if(loading) {
          showProgress(false);
          Toast.makeText(this, R.string.press_back_to_leave, Toast.LENGTH_SHORT).show();
          loading=false;
      }
      else
        super.onBackPressed();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        loading=true;
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
//     */
//    public class UserLoginTask2 extends AsyncTask<Void, Void, Boolean> {
//
//        private final String mEmail;
//        private final String mPassword;
//
//        UserLoginTask2(String email, String password) {
//            mEmail = email;
//            mPassword = password;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            // TODO: attempt authentication against a network service.
//
//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
//
//            // TODO: register the new account here.
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;
//            showProgress(false);
//
//            if (success) {
//                finish();
//            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            mAuthTask = null;
//            showProgress(false);
//        }
//    }
}

