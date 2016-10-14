package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class NewGame extends AppCompatActivity  implements
        GoogleApiClient.OnConnectionFailedListener {
    private static String TAG="NewGameActivity";
    private static final int REQUEST_INVITE = 1;
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    public static final String MESSAGES_CHILD = "messages";

ImageButton nextBtn,inviteBtn;

    //Firebase
private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

//        create a game object, and invite with the gameobject

        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();

        nextBtn=(ImageButton)findViewById(R.id.nextInviteBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO for now it will send to load game
//                Intent TempIntent= new Intent(NewGame.this, LoadActivity.class);
//                startActivity(TempIntent);
//                finish();
//                return;
                sendInvitation();
            }
        });

        //TODO: here send the invites to friends, when they respond to them they will be guided to
        //load game, where it is going to be added to their games.



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

    }


    private void sendInvitation(){
        Intent intent = new AppInviteInvitation.IntentBuilder("TITLE of INvite")
                .setMessage("MEssage ")
                .setCallToActionText("Call to action text")
                .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                ", resultCode=" + resultCode);
        if(requestCode==REQUEST_INVITE){
            if(resultCode==RESULT_OK){
                //check no. of invitations sent
                String[] ids= AppInviteInvitation
                        .getInvitationIds(resultCode,data);
                Log.d(TAG,"Invitations sent:"+ids.length);
            }else{
                //failed or cancelled
                Log.d(TAG,"Failed to send invitations");
            }
        }


    }
}
