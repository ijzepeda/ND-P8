package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class NewGame extends AppCompatActivity {
ImageButton nextBtn,inviteBtn;

    //Firebase



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

//        create a game object, and invite with the gameobject


        nextBtn=(ImageButton)findViewById(R.id.nextInviteBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO for now it will send to load game
                Intent TempIntent= new Intent(NewGame.this, LoadActivity.class);
                startActivity(TempIntent);
                finish();
                return;
            }
        });

        //TODO: here send the invites to friends, when they respond to them they will be guided to
        //load game, where it is going to be added to their games.



    }
}
