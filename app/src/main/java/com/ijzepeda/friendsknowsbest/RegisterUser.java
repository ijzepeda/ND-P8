package  com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;


public class RegisterUser extends AppCompatActivity {
    private static String TAG="RegisterUSer";
TextView nameTV;
    String uid;
    User user;
    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        //create user object
nameTV=(TextView)findViewById(R.id.nameTV);

        app= FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseRef=database.getReference("Users");

uid=Utils.getInstance().getValue(getApplication(),"uid");

        ((Button)findViewById(R.id.guardarBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.getInstance().save(getApplication(),"username",nameTV.getText().toString());
                Utils.getInstance().save(getApplication(),"uid",uid);

               Utils utils=Utils.getInstance();
                utils.save(getApplication(),nameTV.getText().toString(),"username");
                Utils.getInstance().save(getApplication(),uid,"uid");

//Update user to firebase
                FirebaseUser firebaseUser=auth.getCurrentUser();
                UserProfileChangeRequest profileUpdates=new UserProfileChangeRequest.Builder()
                        .setDisplayName(nameTV.getText().toString())

                        //    .setPhotoUri(Uri.fromFile())//TODO CREATE THE BUTTON TO PICKUP the PHOTO
                        .build();
                firebaseUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>(){
                            @Override
                            public void onComplete(@NonNull Task<Void> task){
                                if(task.isSuccessful()){
                                    Log.d(TAG,"User profile updated");
                                }
                            }
                        });

                Intent intent=new Intent(getApplicationContext(),MainMenuActivity.class);
                startActivity(intent);
            }
        });


    }


    public void updateUser(){
       user.setName(((TextView)findViewById(R.id.nameTV)).getText().toString());
        databaseRef.push().setValue(user);

    }
    private static int PICK_IMAGE=1001;
    public void pickImage(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }
}
