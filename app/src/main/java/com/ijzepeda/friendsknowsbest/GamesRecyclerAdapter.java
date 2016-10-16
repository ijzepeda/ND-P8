package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import static android.R.attr.name;
import static com.ijzepeda.friendsknowsbest.R.id.gameNameTextView;
import static com.ijzepeda.friendsknowsbest.R.id.nameTV;

/**
 * Created by Ivan on 10/15/2016.
 */

public class GamesRecyclerAdapter extends RecyclerView.Adapter<GamesRecyclerAdapter.ViewHolder> {
    List<Game> gamesList;
private static String GAME_ID="game_id";

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
Game game=gamesList.get(position);

        holder.noUsersTV.setText("Players: "+game.getNoUsers());
        holder.gameNameTV.setText("Game: "+game.getName());
        holder.gameidTV.setText("DeckId: "+game.getDeckId());
holder.gameidTV.setVisibility(View.INVISIBLE);


        String cardDrawn=game.getCurrentCard()+"/"+game.getNoCards();
        holder.cardsDrawn.setText("Cards drawn:"+cardDrawn);



    }

    @Override
    public int getItemCount() {
        return gamesList.size();
    }


    //Provide reference to views of each data item
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
TextView gameidTV,gameNameTV,cardsDrawn, noUsersTV;
        ImageButton deleteGameBtn;
        Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            gameidTV=(TextView)itemView.findViewById(R.id.game_id);
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
//            gameIntent.putExtra()
                    context.startActivity(gameIntent);
                }
            });

            deleteGameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle ("Hello Dialog")
                            .setMessage ("Are you sure " + String.valueOf(getAdapterPosition()))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    builder.create().show();
                }
            });
        }

        @Override
        public void onClick(View view) {
//            Context context=itemView.getContext();
Log.e("Clicked view","view is:"+view.getId());
Log.e("Clicked view","deleteGameBtn is:"+deleteGameBtn.getId());
Log.e("Clicked view","game_name is:"+R.id.game_name);
            if(view.getId()==deleteGameBtn.getId()){
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle ("Hello Dialog")
                        .setMessage ("Are you sure " + String.valueOf(getAdapterPosition()))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                builder.create().show();
//                return true;
            }
            else if(view.getId()== R.id.game_name){
                Intent gameIntent = new Intent(context, GameActivity.class);
                gameIntent.putExtra(GAME_ID, gameidTV.getText());
//            gameIntent.putExtra()
                context.startActivity(gameIntent);
            }
        }
    }



}
