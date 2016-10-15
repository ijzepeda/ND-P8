package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class LoadActivity extends AppCompatActivity {
RecyclerView loadGameRecyclerView;
    RecyclerView.Adapter recyclerViewAdapter;
    RecyclerView.LayoutManager recylerViewLayoutManager;

    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        context = getApplicationContext();
        String[] subjects =
                {
                        "ANDROID",
                        "PHP",
                        "BLOGGER",
                        "WORDPRESS",
                        "JOOMLA",
                        "ASP.NET",
                        "JAVA",
                        "C++",
                        "MATHS",
                        "HINDI",
                        "ENGLISH"};

        loadGameRecyclerView=(RecyclerView)findViewById(R.id.loadgameRecyclerView);
        recylerViewLayoutManager = new LinearLayoutManager(context);

        loadGameRecyclerView.setLayoutManager(recylerViewLayoutManager);

        recyclerViewAdapter = new RecyclerViewAdapter(context, subjects);

        loadGameRecyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent inten=new Intent(this,MainMenuActivity.class);
        startActivity(inten);
    }
}
