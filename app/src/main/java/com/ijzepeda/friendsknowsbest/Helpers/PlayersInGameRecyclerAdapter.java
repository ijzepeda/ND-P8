package com.ijzepeda.friendsknowsbest.Helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ijzepeda.friendsknowsbest.Game;
import com.ijzepeda.friendsknowsbest.GameActivity;
import com.ijzepeda.friendsknowsbest.GamesRecyclerAdapter;
import com.ijzepeda.friendsknowsbest.R;
import com.ijzepeda.friendsknowsbest.ResultsActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.R.id.list;
import static com.ijzepeda.friendsknowsbest.R.id.alreadyVotedPlayer;
import static com.ijzepeda.friendsknowsbest.R.id.deleteGameBtn;

/**
 * Created by Ivan on 10/15/2016.
 */

public class PlayersInGameRecyclerAdapter extends RecyclerView.Adapter<PlayersInGameRecyclerAdapter.ViewHolder> {
//    List<UserVote> playersList;
    List<String> playersList;
    private static String GAME_ID="game_id";
    private static String DECK_ID="deck_id";
Context activityContext;
    private RadioButton lastCheckedRB = null;

    private int lastCheckedPosition = -1;

    public PlayersInGameRecyclerAdapter(List mplayersList, Context context){
        playersList=mplayersList;
        activityContext=context;
    }


    @Override
    public PlayersInGameRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_game_card,parent,false);

        return new PlayersInGameRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String player=playersList.get(position);
        holder.playerNameTV.setText(player);

//        holder.selectPlayerRadioButton.setChecked(position == lastCheckedPosition);
        holder.selectPlayerCheckBox.setChecked(position == lastCheckedPosition);
//        holder.gameNameTV.setText("Game: "+game.getName());
//TODO `~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//TODO: holder.playerImageView.PICASO CREATE IMAGE BY URL

        Picasso.with(context).load("userObj.getPhoto").error(R.drawable.placeholder).into(holder.playerImageView);



//TODO `~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//TODO: holder.alreadyVotedPlayer read if voted is true and mark it
        /**
         if(playerlist.get(position).isVoted()){
            holder.alreadyVotedPlayerImageView.setVisibility(View.VISIBLE);
        }*/


        //checbox to select only 1 player
//        View.OnClickListener rbClick = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RadioButton checked_rb = (RadioButton) v;
//                if(lastCheckedRB != null){
//                    lastCheckedRB.setChecked(false);
//                }
//                lastCheckedRB = checked_rb;
//            }
//        };
//        holder.price1m.setOnClickListener(rbClick);
//        holder.price3m.setOnClickListener(rbClick);
//        holder.price6m.setOnClickListener(rbClick);

    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    Context context;
    //Provide reference to views of each data item
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView playerNameTV;
        ImageView playerImageView;
        ImageView alreadyVotedPlayerImageView;
        CheckBox selectPlayerCheckBox;
//        RadioButton selectPlayerRadioButton;


        public ViewHolder(View itemView) {
            super(itemView);
            playerNameTV=(TextView)itemView.findViewById(R.id.playerName);
            playerImageView=(ImageView)itemView.findViewById(R.id.playerImage);
            alreadyVotedPlayerImageView=(ImageView)itemView.findViewById(alreadyVotedPlayer);
            selectPlayerCheckBox=(CheckBox)itemView.findViewById(R.id.selectPlayerCheckBox);
//            selectPlayerRadioButton=(RadioButton)itemView.findViewById(R.id.selectPlayerRadioButton);

            //add a new onclicklistener here, or by implements on view
//            itemView.setOnClickListener(this);
            context =itemView.getContext();

            itemView.setOnClickListener(this);
            selectPlayerCheckBox.setOnClickListener(this);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //el checbox es para marcar el estado si ya voto o no
//                    selectPlayerCheckBox.setSelected(true);
//                    selectPlayerRadioButton.setSelected(true);
//                    if(context instanceof GameActivity){
//                        ((GameActivity)context).selectedPlayer(playerNameTV.getText().toString());
//
//                    }
//                }
//            });
//            selectPlayerCheckBox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //el checbox es para marcar el estado si ya voto o no
//                    selectPlayerRadioButton.setSelected(true);
//                    if(context instanceof GameActivity){
//                        ((GameActivity)context).selectedPlayer(playerNameTV.getText().toString());
//
//                    }
//                }
//            });

        }

        @Override
        public void onClick(View view) {

                    selectPlayerCheckBox.setChecked (true);
//                    selectPlayerRadioButton.setChecked (true);

            lastCheckedPosition = getAdapterPosition();
            notifyItemRangeChanged(0, playersList.size());
//TODO#####~~~~~~~~~~~~~~~~~CHANGE USERNAME TO USERUID #######################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-***********************************######################
//TODO#####~~~~~~~~~~~~~~~~~CHANGE USERNAME TO USERUID #######################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-***********************************######################
//TODO#####~~~~~~~~~~~~~~~~~CHANGE USERNAME TO USERUID #######################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-***********************************######################
            if(context instanceof GameActivity){
                //send object of user //uid,name,image.
                        ((GameActivity)context).selectedPlayer(playerNameTV.getText().toString());

                    }
            if(context instanceof ResultsActivity){
                //                                      send playerUID or UserVote
                ((ResultsActivity)context).viewComment(playerNameTV.getText().toString());

            }
        }
    }



}
