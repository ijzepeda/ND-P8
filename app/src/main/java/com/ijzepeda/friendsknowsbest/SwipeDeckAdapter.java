package com.ijzepeda.friendsknowsbest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
/**
 * Created by ivan.zepeda on 12/07/2016.
 */

public class SwipeDeckAdapter extends BaseAdapter {

    private List<Card> data;
    private Context context;

    public SwipeDeckAdapter(List<Card> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if(v == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            LayoutInflater inflater = getLayoutInflater();
            // normally use a viewholder
            v = inflater.inflate(R.layout.cardview, parent, false);
        }
        Log.e("~~~~~","qaqui"+data.get(position).getNumber());
        ((TextView) v.findViewById(R.id.cardNumber)).setText(data.get(position).getNumber()+"");
        ((TextView) v.findViewById(R.id.cardText)).setText(data.get(position).getQuote());

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return v;
    }
}