package com.ijzepeda.friendsknowsbest.widget2;


        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.appwidget.AppWidgetProvider;
        import android.content.Context;
        import android.content.Intent;
        import android.content.ComponentName;
        import android.net.Uri;
        import android.widget.RemoteViews;

        import com.ijzepeda.friendsknowsbest.GameActivity;
        import com.ijzepeda.friendsknowsbest.R;


public class WidgetProvider2 extends AppWidgetProvider {
    public static String EXTRA_WORD=
            "com.ijzepeda.fkb.widget.WORD";
//            "com.commonsware.android.appwidget.lorem.WORD";

    @Override
    public void onUpdate(Context ctxt, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int i=0; i<appWidgetIds.length; i++) {
            Intent svcIntent=new Intent(ctxt, WidgetService2.class);

            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget=new RemoteViews(ctxt.getPackageName(),
                    R.layout.widget);

            widget.setRemoteAdapter(appWidgetIds[i], R.id.words,
                    svcIntent);

//            Intent clickIntent=new Intent(ctxt, LoremActivity2.class);
            Intent clickIntent=new Intent(ctxt, GameActivity.class);


//            clickIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//closing next activity
//            clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
//            clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//
//            clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

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


//instaupdate
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, WidgetProvider2.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.words);

    }
}