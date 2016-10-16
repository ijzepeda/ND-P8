package  com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;


public class RegisterUser extends AppCompatActivity {
    private static String TAG="RegisterUSer";
TextView nameTV;
    ImageView profilePic;
    Button saveBtn;
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
saveBtn=(Button)findViewById(R.id.saveBtn);
        profilePic=(ImageView)findViewById(R.id.userProfilePicView);

        app= FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseRef=database.getReference("Users");

uid=Utils.getInstance().getValue(getApplication(),"uid");

        ((Button)findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener() {
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


                //TODO BEFORE GOING TO MENU AGAIN, CREATE USER IN DATABSE, That includes game
                Map<String,Object> friendsMap=new HashMap<String, Object>();
                Map<String,Object>cardsMap=new HashMap<String, Object>();
                Map<String,Object>gamesMap=new HashMap<String, Object>();
                friendsMap.put("Friend0","");
                cardsMap.put("FavCard1","");
//                gamesMap.put("game0","GameUID123");
                gamesMap.put("GameUID123","GameUID123");

                String userName="";
                String userMail="";

                if(firebaseUser.getDisplayName()!=null){
                    userName=firebaseUser.getDisplayName().toString();
                }else{
                    userName=nameTV.getText().toString();
                }

                User user=new User(userName,"0","0",firebaseUser.getEmail().toString(),firebaseUser.getUid(),
                        friendsMap,cardsMap,gamesMap);
                databaseRef=database.getReference("Users");

                //TODO KEY CANT BE AN EMAIL!
                Map<String,Object> map=new HashMap<String,Object>();
                map.put(firebaseUser.getUid(),"");
                databaseRef.updateChildren(map);
                databaseRef.child(firebaseUser.getUid()).setValue(user);



                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
                return;
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
