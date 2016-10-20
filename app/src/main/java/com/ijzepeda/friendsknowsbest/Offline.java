package com.ijzepeda.friendsknowsbest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.daprlabs.cardstack.SwipeDeck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

//import com.andtinder.model.CardModel;
//import com.andtinder.view.CardContainer;
//import com.andtinder.view.SimpleCardStackAdapter;

public class Offline extends AppCompatActivity {

//    CardModel card,card2,card3,newCard;
//    SimpleCardStackAdapter adapter;
//    int i=4;
//    CardModel.OnCardDimissedListener dismissListener;
//    CardContainer mCardContainer;
    //    @TargetApi(Build.VERSION_CODES.LOLLIPOP)

int i=4;
    SwipeDeck cardStack;
    ArrayList<Card> cards;
    AlertDialog.Builder alertDialog;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
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
                Log.e("MainActivity", "card was swiped left, position in adapter: " + position);
            }

            @Override
            public void cardSwipedRight(int position) {
                fetchCard();
                Log.e("MainActivity", "card was swiped right, position in adapter: " + position);
                //testData.add(""+i++);
            }
            @Override
            public void cardsDepleted() {
                Log.e("MainActivity", "no more cards");
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
            category="Romantic";
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


/**
         mCardContainer = (CardContainer) findViewById(R.id.layoutview);
        mCardContainer.setOrientation(Orientations.Orientation.fromIndex(0));
         adapter = new SimpleCardStackAdapter(this);

        card = new CardModel("Title1", "Description goes here", ContextCompat.getDrawable(getApplication(), R.drawable.picture1));//getDrawable(R.drawable.picture1));
        card3 = new CardModel("Title3", "DisLiked or Lefted",  ContextCompat.getDrawable(getApplication(),R.drawable.picture3));
        card2 = new CardModel("Title2", "Liked or Righted", ContextCompat.getDrawable(getApplication(),R.drawable.picture2));

         dismissListener=new CardModel.OnCardDimissedListener(){
            @Override
            public void onLike() {

                newCard = new CardModel("Title"+i++, "Liked or Righted", ContextCompat.getDrawable(getApplication(),R.drawable.picture2));
                newCard.setOnCardDimissedListener(dismissListener);
                adapter = new SimpleCardStackAdapter(getApplication());
                adapter.add(newCard);
//                mCardContainer/
                mCardContainer.setAdapter(adapter);


//                adapter.add(new CardModel("Title"+i++, "Description goes here", getDrawable(R.drawable.picture1)));
                //.setOnCardDimissedListener(dismissListener);
            }

            @Override
            public void onDislike() {

                newCard = new CardModel("Title"+i++, "Liked or Righted", ContextCompat.getDrawable(getApplication(),R.drawable.picture2));
                newCard.setOnCardDimissedListener(dismissListener);
                adapter.add(newCard);

                adapter.notifyDataSetChanged();
//                adapter.add(new CardModel("Title"+i++, "Description goes here", getDrawable(R.drawable.picture1)));

            }
        };


        card.setOnCardDimissedListener(dismissListener);
        / *new CardModel.OnCardDimissedListener() {
            @Override
            public void onLike() {

                Log.e("Swipeable Card", "I liked it");
                card.setCardDislikeImageDrawable(getDrawable(R.drawable.like));
                adapter.add(new CardModel("Title"+i++, "Description goes here", getDrawable(R.drawable.picture1)));

            }

            @Override
            public void onDislike() {

                Log.e("Swipeable Card", "I did not liked it");
                card.setCardDislikeImageDrawable(getDrawable(R.drawable.dislike));
                adapter.add(new CardModel("Title"+i++, "Description goes here", getDrawable(R.drawable.picture1)));


            }
        });
        * /
        card.setOnClickListener(new CardModel.OnClickListener() {
            @Override
            public void OnClickListener() {
                Log.i("Swipeable Cards","I am pressing the card");
            }
        });




        adapter.add(new CardModel("Title0", "Description goes here", getDrawable(R.drawable.picture1)));
        adapter.add(card);
        adapter.add(card2);
        adapter.add(card2);   adapter.pop();
        adapter.add(card2);
        adapter.add(card3);
        mCardContainer.setAdapter(adapter);

    }
*/

}
