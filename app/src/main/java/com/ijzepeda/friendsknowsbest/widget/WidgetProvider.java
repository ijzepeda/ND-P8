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



        import android.app.Activity;
        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.appwidget.AppWidgetProvider;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ComponentName;
        import android.net.Uri;
        import android.util.Log;
        import android.widget.RemoteViews;

        import com.ijzepeda.friendsknowsbest.Game;
        import com.ijzepeda.friendsknowsbest.GameActivity;
        import com.ijzepeda.friendsknowsbest.GamesRecyclerAdapter;
        import com.ijzepeda.friendsknowsbest.LoadActivity;
        import com.ijzepeda.friendsknowsbest.R;

        import static android.R.style.Widget;

public class WidgetProvider extends AppWidgetProvider {
    public static String EXTRA_GAME=
            "com.ijzepeda.friendsknowsbes.GAME";
    private static String GAME_ID="game_id";
    private static String DECK_ID="deck_id";
    private static String CURRENT_CARD_ID="current_card_id";
    private static String TOTAL_CARDS_ID="total_card_id";

    private static final String REFRESH_ACTION = "com.ijzepeda.friendsknowsbest.appwidget.action.REFRESH";
    private static final String SYNC_CLICKED    = "automaticWidgetSyncButtonClick";

    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(REFRESH_ACTION);
        intent.setComponent(new ComponentName(context, DetailWidgetProvider.class));
        context.sendBroadcast(intent);
    }
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
    @Override
    public void onUpdate(Context ctxt, AppWidgetManager appWidgetManager,  int[] appWidgetIds) {
        Log.e("Widget","onUpdate appWidgetsId.length:"+appWidgetIds.length);

        RemoteViews remoteViews;
        ComponentName watchWidget;
        remoteViews = new RemoteViews(ctxt.getPackageName(), R.layout.widget_layout);
        watchWidget = new ComponentName(ctxt, WidgetProvider.class);
        remoteViews.setOnClickPendingIntent(R.id.syncButton, getPendingSelfIntent(ctxt, SYNC_CLICKED));
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        for (int i=0; i<appWidgetIds.length; i++) {
            Log.e("Widget","onUpdate appWidgetsId instance i:"+i);

            Intent svcIntent=new Intent(ctxt, WidgetService.class);

            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget=new RemoteViews(ctxt.getPackageName(),
                    R.layout.widget);

            widget.setRemoteAdapter(appWidgetIds[i], R.id.words,
                    svcIntent);


//            Intent clickIntent=new Intent(ctxt, LoadActivity.class);

            Intent clickIntent=new Intent(ctxt, GameActivity.class);
//            clickIntent.putExtra(GAME_ID, gameidTV.getText());
//            clickIntent.putExtra(DECK_ID,  deckIdTV.getText()+"");
//            clickIntent.putExtra(CURRENT_CARD_ID,  game.getCurrentCard());
//            clickIntent.putExtra(TOTAL_CARDS_ID,  game.getNoCards());
//

            PendingIntent clickPI=PendingIntent
                    .getActivity(ctxt, 0,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            widget.setPendingIntentTemplate(R.id.words, clickPI);

            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
        }

        super.onUpdate(ctxt, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e("Widget","onReceive");


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, WidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.words);


        if (GamesRecyclerAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Log.e("Widget","onReceive ACTION_DATA_UPDATED");

            AppWidgetManager appWidgetManager2 = AppWidgetManager.getInstance(context);
            int[] appWidgetIds2 = appWidgetManager2.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager2.notifyAppWidgetViewDataChanged(appWidgetIds2, R.id.widget_list);//widget_list
        }

        if (SYNC_CLICKED.equals(intent.getAction())) {
            Log.e("Widget","onReceive SYNC_CLICKED");

            AppWidgetManager appWidgetManager3 = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews;
            ComponentName watchWidget;
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            watchWidget = new ComponentName(context, WidgetProvider.class);
            remoteViews.setTextViewText(R.id.syncButton, "TESTING");

            appWidgetManager3.updateAppWidget(watchWidget, remoteViews);

        }


    }
}

