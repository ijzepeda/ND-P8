package com.ijzepeda.friendsknowsbest.widget2;

/**
 * Created by Ivan on 10/17/2016.
 */
/***
 Copyright (c) 2008-2012 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Advanced Android Development_
 http://commonsware.com/AdvAndroid
 */


import android.util.Log;
import android.widget.RemoteViewsService;


        import android.appwidget.AppWidgetManager;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Bundle;
        import android.widget.RemoteViews;

import com.ijzepeda.friendsknowsbest.models.Game;
import com.ijzepeda.friendsknowsbest.R;
import com.ijzepeda.friendsknowsbest.Utils;

import java.util.ArrayList;
import java.util.List;


public class LoremViewsFactory2 implements RemoteViewsService.RemoteViewsFactory {

    private Context ctxt=null;
    private int appWidgetId;

    private List<Game> wWidgetGamesList;

    public LoremViewsFactory2(Context ctxt, Intent intent) {
        this.ctxt=ctxt;
        appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        wWidgetGamesList=new ArrayList<>();
        wWidgetGamesList.addAll(Utils.getInstance().getWidgetGameList());
    }

    @Override
    public void onCreate() {
        // no-op
    }

    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        if((Utils.getInstance().getWidgetGameListCount())==0)
            wWidgetGamesList.clear();

        return(Utils.getInstance().getWidgetGameListCount());
//        return(items.length);
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if(Utils.getInstance().getWidgetGameList().isEmpty()){
            Log.e("FACTORY","getviewat is utils list is empty");
        }
        else {
            wWidgetGamesList.clear();
            wWidgetGamesList.addAll(Utils.getInstance().getWidgetGameList());
        }

        RemoteViews row=new RemoteViews(ctxt.getPackageName(),
                R.layout.widget_row_game);
//                R.layout.row);


        //simple row
//        row.setTextViewText(android.R.id.text1, wWidgetGamesList.get(position).getName());
        //Complete row
        row.setTextViewText(R.id.game_name, wWidgetGamesList.get(position).getName());
        row.setTextViewText(R.id.cardsdrawn,"cards:"+ wWidgetGamesList.get(position).getCurrentCard()+"/"+ wWidgetGamesList.get(position).getNoCards());
        row.setTextViewText(R.id.players, "Players:"+wWidgetGamesList.get(position).getNoUsers());


//        row.setTextViewText(android.R.id.text1, items[position]);

        Intent i=new Intent();
        Bundle extras=new Bundle();
        extras.putParcelable(WidgetProvider2.EXTRA_WORD, wWidgetGamesList.get(position));//in order to do this, I implemented parcelable in Game object
//        extras.putString(WidgetProvider2.EXTRA_WORD, wWidgetGamesList.get(position).getUid());

//        extras.putString(WidgetProvider2.EXTRA_WORD, items[position]);
        i.putExtras(extras);
//        row.setOnClickFillInIntent(android.R.id.text1, i);
        row.setOnClickFillInIntent(R.id.widget_list_item, i);

        return(row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {
        // no-op
    }
}