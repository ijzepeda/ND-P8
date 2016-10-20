package  com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class RegisterUser extends AppCompatActivity {
    private static String TAG="RegisterUser";
TextView nameTV;
    ImageView profilePic;
    Button saveBtn;
    ImageButton editImageBtn;
    String uid;
    User user;
    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        //create user object
nameTV=(TextView)findViewById(R.id.nameTV);
saveBtn=(Button)findViewById(R.id.saveBtn);
        profilePic=(ImageView)findViewById(R.id.userProfilePicView);
        editImageBtn=(ImageButton)findViewById(R.id.editImageBtn);

        app= FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        storage= FirebaseStorage.getInstance();
        // -- reference to table in database
        databaseRef=database.getReference("Users");
         firebaseUser = auth.getCurrentUser();

uid=Utils.getInstance().getValue(getApplication(),"uid");


        editImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("scale", true);
                intent.putExtra("outputX", 256);
                intent.putExtra("outputY", 256);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, 1);
            }
        });

        saveBtn.setClickable(false);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uploadFinished){
                Utils.getInstance().save(getApplication(), "username", nameTV.getText().toString());
                Utils.getInstance().save(getApplication(), "uid", uid);

                Utils utils = Utils.getInstance();
                utils.save(getApplication(), nameTV.getText().toString(), "username");
                Utils.getInstance().save(getApplication(), uid, "uid");

                //Update user to firebase


//                FirebaseUser firebaseUser = auth.getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(nameTV.getText().toString())
                        .setPhotoUri(mFileUri)
                        .build();
                firebaseUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated");
                                    //Set photoUrl in Users
                                    database.getReference().child("Users").child(firebaseUser.getUid()).child("photoUrl").setValue(downloadUri.toString());//mFileUri);//

                                }

                            }

                        });
            }


                // BEFORE GOING TO MENU AGAIN, CREATE USER IN DATABSE, That includes game
                Map<String,Object> friendsMap=new HashMap<String, Object>();
                Map<String,Object>cardsMap=new HashMap<String, Object>();
                Map<String,Object>gamesMap=new HashMap<String, Object>();
                friendsMap.put("Friend0","");
                cardsMap.put("FavCard1","");

                String userName="";
                String userMail="";

                if(firebaseUser.getDisplayName()!=null){
                    userName=firebaseUser.getDisplayName().toString();
                }else{
                    userName=nameTV.getText().toString();
                }

                User user=new User(userName,"0","0",firebaseUser.getEmail().toString(),firebaseUser.getUid(),firebaseUser.getPhotoUrl()!=null?firebaseUser.getPhotoUrl().toString():"FOTOURL",
                        friendsMap,cardsMap,gamesMap);
                databaseRef=database.getReference("Users");

                //TODO KEY CANT BE AN EMAIL!
                Map<String,Object> map=new HashMap<String,Object>();
                map.put(firebaseUser.getUid(),"");
                databaseRef.updateChildren(map);
                databaseRef.child(firebaseUser.getUid()).setValue(user);

//update firebase auth user
    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 1) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                //Get image
                Bitmap newProfilePic = extras.getParcelable("data");
                profilePic.setImageBitmap(newProfilePic);
                 String path = MediaStore.Images.Media.insertImage(getContentResolver(), newProfilePic, "player", null);

                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG,"Permission is granted");
                        mFileUri= Uri.parse(path);
                        uploadPicture(mFileUri);
                    } else {
                        Log.v(TAG,"Permission is revoked");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }
                else { //permission is automatically granted on sdk<23 upon installation
                    Log.v(TAG,"Permission is granted");
                    mFileUri= Uri.parse(path);
                    uploadPicture(mFileUri);
                }
            }
        }
    }

    private StorageReference mStorageRef;
    private Uri mFileUri = null;
    boolean uploadFinished;
    Uri downloadUri;
    //source of this method: https://github.com/firebase/quickstart-android/blob/master/storage/app/src/main/java/com/google/firebase/quickstart/firebasestorage/MainActivity.java
public void uploadPicture(Uri fileUri){
    mStorageRef = FirebaseStorage.getInstance().getReference();

// Get a reference to store file at photos/<FILENAME>.jpg
    final StorageReference photoRef = mStorageRef.child("photos").child(fileUri.getLastPathSegment());

    // Upload file to Firebase Storage
    Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());

    photoRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Upload succeeded
                    Log.d(TAG, "uploadFromUri:onSuccess");

                    // Get the public download URL
                     downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                    uploadFinished=true;


                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Upload failed
                    Log.w(TAG, "uploadFromUri:onFailure", exception);

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



    @Override
    public void onBackPressed() {
        //Avoid user to leave this activity until the user finish
//        super.onBackPressed();
//        finish();
    }

}
