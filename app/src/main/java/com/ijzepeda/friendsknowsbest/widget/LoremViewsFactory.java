package com.ijzepeda.friendsknowsbest.widget;

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
        import android.widget.RemoteViewsService;

import com.ijzepeda.friendsknowsbest.Game;
import com.ijzepeda.friendsknowsbest.R;
import com.ijzepeda.friendsknowsbest.Utils;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.empty;

public class LoremViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context ctxt=null;
    private int appWidgetId;
    private List<Game> wWidgetGamesList;

    public LoremViewsFactory(Context ctxt, Intent intent) {
        Log.e("Widget","LoremViewsFactory onConstructor");

        this.ctxt=ctxt;
        appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        wWidgetGamesList=new ArrayList<>();
        wWidgetGamesList.addAll(Utils.getInstance().getWidgetGameList());
    for(Game game:wWidgetGamesList){
    Log.e("LoremViewsFactory","game added in list has"+game.getName());
    }
    }

    public void refreshWidgetGameList(){
        wWidgetGamesList=new ArrayList<>();

        wWidgetGamesList.addAll(Utils.getInstance().getWidgetGameList());
        for(Game game:wWidgetGamesList){
            Log.e("LoremViewsFactory","refreshWidgetGameList game added in list has"+game.getName());
        }

    }

    @Override
    public void onCreate() {
        Log.e("Widget","LoremViewsFactory onCreate");

        // no-op
//        wWidgetGamesList.addAll(Utils.getInstance().getWidgetGameList());

    }

    @Override
    public void onDestroy() {
        Log.e("Widget","LoremViewsFactory onDestroy");

        // no-op
    }

    @Override
    public int getCount() {
        Log.e("WidgetFactory","get count is in getCount():"+(Utils.getInstance().getWidgetGameListCount()));

//        if empty, then clear listvie
        if((Utils.getInstance().getWidgetGameListCount())==0)
        wWidgetGamesList.clear();

        return(Utils.getInstance().getWidgetGameListCount());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.e("Widget","LoremViewsFactory getViewAt:"+position);

        wWidgetGamesList.clear();
        wWidgetGamesList.addAll(Utils.getInstance().getWidgetGameList());

        RemoteViews row=new RemoteViews(ctxt.getPackageName(),
                R.layout.row);

//        row.setTextViewText(android.R.id.text1, wWidgetGamesList.get(position).getName());
        row.setTextViewText(android.R.id.text1, wWidgetGamesList.get(position).getName());

        Intent i=new Intent();
        Bundle extras=new Bundle();

        extras.putParcelable(WidgetProvider.EXTRA_GAME, wWidgetGamesList.get(position));//in order to do this, I implemented parcelable in Game object
//        extras.putString(WidgetProvider.EXTRA_GAME, wWidgetGamesList.get(position).getUid());
        i.putExtras(extras);
        row.setOnClickFillInIntent(android.R.id.text1, i);





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
        Log.e("Widget","LoremViewsFactory onDataSetChanged");

        // no-op
    }
}
