package com.ijzepeda.friendsknowsbest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class GameOverResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over_results);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent main=new Intent(this,MainActivity.class);
        startActivity(main);
        finish();
    }
}
