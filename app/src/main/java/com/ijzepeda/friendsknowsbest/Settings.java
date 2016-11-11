package com.ijzepeda.friendsknowsbest;

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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class Settings extends AppCompatActivity {
    private static String TAG="Settings";

    //firebase
    private FirebaseApp app;
    private FirebaseDatabase database;

    private FirebaseAuth auth;
    FirebaseUser firebaseUser;

    Button saveBtn;
    ImageView userImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));

        userImage=(ImageView)findViewById(R.id.userImageView);
         saveBtn=(Button)findViewById(R.id.saveBtn);
        ImageButton editBtn=(ImageButton)findViewById(R.id.editImageBtn);

//FIREBASE
        app= FirebaseApp.getInstance();
        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        firebaseUser=auth.getCurrentUser();

        Picasso.with(this).load(firebaseUser.getPhotoUrl()).error(R.drawable.placeholder).into(userImage);

        editBtn.setOnClickListener(new View.OnClickListener() {
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
                if (uploadFinished) {
                    //Update user to firebase
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadUri)
//                            .setDisplayName()
//                        .setPhotoUri(downloadUri)
                            .build();
                    firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated");
                                        //Set photoUrl in Users
                                        Log.d("USER PHOTO"," firebaseUser.getPhotoUrl() IS:"+firebaseUser.getPhotoUrl());//content://media/external/images/media/99898
                                        Log.d("USER PHOTO","downloadUri IS:"+downloadUri.toString());//https://firebasestorage.googleapis.com/v0/b/nd-p7-7d3b1.appspot.com/o/photos%2F99898?alt=media&token=c7b722d3-feb7-474b-a8b6-354b4af1492e
                                        Log.d("USER PHOTO","mFileUri IS:"+mFileUri.toString());//content://media/external/images/media/99898
                                        Map<String,Object> photoUrl=new HashMap<String, Object>();
                                        photoUrl.put("photoUrl",downloadUri.toString());
                                        database.getReference().child("Users").child(firebaseUser.getUid()).updateChildren(photoUrl);//child("photoUrl").setValue(downloadUri.toString());//mFileUri);//TODO UPDATE NOT SETVALUE
                                        //Add pictureUrl to sharedPrefs
                                        Utils.getInstance().save(getApplication(),downloadUri.toString(),getString(R.string.shared_userphotourl_key));

                                    }
                                }
                            });

                finish();
                }
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

//        switch (item.getItemId()) {
//            case android.R.id.home:
////                finish();
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
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
                         userImage.setImageBitmap(newProfilePic);
                        String path = MediaStore.Images.Media.insertImage(getContentResolver(), newProfilePic, "player", null);

                        if (Build.VERSION.SDK_INT >= 23) {
                            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {
                                Log.v(TAG,"Permission is granted");
                                mFileUri= Uri.parse(path);
                                uploadPicture(mFileUri);
                            } else {
                                Log.v(TAG,"Permission is revoked");
                                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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

                        //setButton clicable and with color
                        saveBtn.setClickable(true);
                        saveBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));

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




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
        }
