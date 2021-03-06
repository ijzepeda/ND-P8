package com.ijzepeda.friendsknowsbest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.daprlabs.cardstack.SwipeDeck;
import com.ijzepeda.friendsknowsbest.Helpers.SwipeDeckAdapter;
import com.ijzepeda.friendsknowsbest.models.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

//import com.andtinder.model.CardModel;
//import com.andtinder.view.CardContainer;
//import com.andtinder.view.SimpleCardStackAdapter;

public class Offline extends AppCompatActivity {

    static String TAG= "Offline";
int i=4;
    SwipeDeck cardStack;
    ArrayList<Card> cards;
    AlertDialog.Builder alertDialog;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_offline);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.title));


        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);


        //Not that useful
       alertDialog=  new AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_over))
                .setMessage(R.string.should_start_new_game)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                       Intent intent=new Intent(getApplication(),Offline.class);
                        startActivity(intent);
                        finish();
return;
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        Intent intent=new Intent(getApplication(),MainActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);


loadDeck(getString(R.string.categoryromantic));

        cardStack.setEventCallback(new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                fetchCard();
                Log.e(TAG, "card was swiped left, position in adapter: " + position);
            }

            @Override
            public void cardSwipedRight(int position) {
                fetchCard();
                Log.e(TAG, "card was swiped right, position in adapter: " + position);
                //testData.add(""+i++);
            }
            @Override
            public void cardsDepleted() {
                Log.e(TAG, "no more cards");
                alertDialog.show();
            }
            @Override
            public void cardActionDown() {
            }
            @Override
            public void cardActionUp() {
            }
        });




        final SwipeDeckAdapter adapter = new SwipeDeckAdapter(cards, this);
        cardStack.setAdapter(adapter);

    }
    public void fetchCard(){
       // cards.add(new Card(0,0,"","",true));
    }

    public void loadDeck(String category){

        cards= new ArrayList<>();
int i=0;
//        if(category.equals(R.string.categoryromantic)) {
        String[] mQuotes = getResources().getStringArray(R.array.category_romantic);
            category=getString(R.string.romantic_cat);
//        }

        //random list
        long seed = System.nanoTime();
        Collections.shuffle(Arrays.asList(mQuotes), new Random(seed));



for(String quote: mQuotes){
    Card cartita=new Card(i++, Integer.parseInt(quote.substring(0,1)),quote,category,false);
    Integer.parseInt(quote.substring(0,1));
    cards.add(cartita);
}

    }


}
