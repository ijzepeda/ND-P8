package com.ijzepeda.friendsknowsbest.Helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ijzepeda.friendsknowsbest.GameActivity;
import com.ijzepeda.friendsknowsbest.R;
import com.ijzepeda.friendsknowsbest.models.Game;

import java.util.List;

/**
 * Created by Ivan on 10/15/2016.
 */

public class GamesRecyclerAdapter extends RecyclerView.Adapter<GamesRecyclerAdapter.ViewHolder> {
    public static final String ACTION_DATA_UPDATED =
            "com.ijzepeda.friendsknowsbest.ACTION_DATA_UPDATED";
    List<Game> gamesList;
private static String GAME_ID="game_id";
    private static String DECK_ID="deck_id";
    private static String CURRENT_CARD_ID="current_card_id";
    private static String TOTAL_CARDS_ID="total_card_id";

    Game game;


    public GamesRecyclerAdapter(List mGamesList){
        gamesList=mGamesList;
    }


    @Override
    public GamesRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext())
               .inflate(R.layout.loadgamecard,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GamesRecyclerAdapter.ViewHolder holder, int position) {
        game=gamesList.get(position);
        holder.noUsersTV.setText("Players: "+game.getNoUsers());
        holder.gameNameTV.setText("Game: "+game.getName());
        //missing deck > "DeckId: "+game.getDeckId()
        holder.deckIdTV.setText(game.getDeckId());
        holder.gameidTV.setText(game.getUid());
        holder.gameidTV.setVisibility(View.GONE);


        String cardDrawn=game.getCurrentCard()+"/"+game.getNoCards();
        holder.cardsDrawn.setText("Cards drawn:"+cardDrawn);



    }

    @Override
    public int getItemCount() {
        return gamesList.size();
    }


    //Provide reference to views of each data item
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
TextView gameidTV,deckIdTV,gameNameTV,cardsDrawn, noUsersTV;
        ImageButton deleteGameBtn;
        Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            gameidTV=(TextView)itemView.findViewById(R.id.game_id);
            deckIdTV=(TextView)itemView.findViewById(R.id.deck_id);
            gameNameTV=(TextView)itemView.findViewById(R.id.game_name);
            cardsDrawn=(TextView)itemView.findViewById(R.id.cardsdrawn);
            noUsersTV =(TextView)itemView.findViewById(R.id.players);
            deleteGameBtn=(ImageButton)itemView.findViewById(R.id.deleteGameBtn);

            //add a new onclicklistener here, or by implements on view
//            itemView.setOnClickListener(this);
            context =itemView.getContext();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent gameIntent = new Intent(context, GameActivity.class);
                    gameIntent.putExtra(GAME_ID, gameidTV.getText());
                    gameIntent.putExtra(DECK_ID,  deckIdTV.getText()+"");
                    gameIntent.putExtra(CURRENT_CARD_ID,  game.getCurrentCard());
                    gameIntent.putExtra(TOTAL_CARDS_ID,  game.getNoCards());
                    context.startActivity(gameIntent);
                }
            });

            deleteGameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle (context.getString(R.string.leave_game))
                            .setMessage (context.getString(R.string.are_you_sure) + String.valueOf(getAdapterPosition()))
                            .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    builder.create().show();
                }
            });
        }

        @Override
        public void onClick(View view) {
        }
    }



}
