package com.ijzepeda.friendsknowsbest.Helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ijzepeda.friendsknowsbest.GameActivity;
import com.ijzepeda.friendsknowsbest.R;
import com.ijzepeda.friendsknowsbest.ResultsActivity;
import com.ijzepeda.friendsknowsbest.models.UserVote;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.ijzepeda.friendsknowsbest.R.id.alreadyVotedPlayer;

/**
 * Created by Ivan on 10/15/2016.
 */

public class PlayersInGameRecyclerAdapter extends RecyclerView.Adapter<PlayersInGameRecyclerAdapter.ViewHolder> {
    List<UserVote> playersList;
Context activityContext;

    private int lastCheckedPosition = -1;


    public PlayersInGameRecyclerAdapter(List<UserVote> mplayersList, Context context){
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
        String player=playersList.get(position).getName();
        holder.playerNameTV.setText(player);
        holder.selectPlayerCheckBox.setChecked(position == lastCheckedPosition);
         Picasso.with(activityContext).load(playersList.get(position).getPicUrl()).error(R.drawable.placeholder).into(holder.playerImageView);
         if(playersList.get(position).isVoted()){
            holder.alreadyVotedPlayerImageView.setVisibility(View.VISIBLE);
        }


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
            //add a new onclicklistener here, or by implements on view
//            itemView.setOnClickListener(this);
            context =itemView.getContext();

            itemView.setOnClickListener(this);
            selectPlayerCheckBox.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {

                    selectPlayerCheckBox.setChecked (true);
//                    selectPlayerRadioButton.setChecked (true);

            lastCheckedPosition = getAdapterPosition();
            notifyItemRangeChanged(0, playersList.size());
            if(context instanceof GameActivity){
                ((GameActivity)context).selectedPlayer(playersList.get(lastCheckedPosition).getName(),playersList.get(lastCheckedPosition).getUseruid(),playersList.get(lastCheckedPosition).getPicUrl());

                    }
            if(context instanceof ResultsActivity){
                ((ResultsActivity)context).viewComment(playersList.get(lastCheckedPosition).getName(),playersList.get(lastCheckedPosition).getUseruid(),playersList.get(lastCheckedPosition).getMessage());

            }
        }
    }



}
