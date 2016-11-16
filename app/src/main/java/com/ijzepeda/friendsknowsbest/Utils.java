package com.ijzepeda.friendsknowsbest;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.ijzepeda.friendsknowsbest.models.Game;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ivan on 9/28/2016.
 */

public class Utils {
    private String userMail;
    private String uid;
    private String userName;
//    private LatLng currentLocation;
//    private static SharedPreferences sharedPrefs;
    private static Utils UtilSharedPrefs;
    public static final String PREFS_NAME = "AOP_PREFS";
    public static final String PREFS_KEY = "AOP_PREFS_String";

    private static List<Game> widgetGameList;

    public static final String CONTENT_URL="content://com.ijzepeda.friendsknowsbest.GameProvider";//content://com.example.MyApplication.StudentsProvider";

    //DEEPLINK
    static String DEEPLINK_LOGO= "android.resource://com.ijzepeda.friendsknowsbest/mipmap/logo";

    //Strings
     static String REF_USERS="Users";
     static String REF_GAME="Game"; //was game?
     static String REF_GAMES="Game"; //was game?
     static String REF_DECK="Deck";
     static String CHILD_GAMES="games";
     static String CHILD_NO_USERS="noUsers";
     static String CHILD_NAME="name";
     static String CHILD_USERS="users";
     static String CHILD_NO_CARDS="noCards";
     static String CHILD_CARD="card";
     static String CHILD_DECK_ID="currentCard";
     static String CHILD_VOTED="voted";
     static String CHILD_CURRENT_CARD="currentCard";
     static String CHILD_PHOTO_URL="photoUrl";
     static String CHILD_PHOTOS="photos";
     static String CHILD_ACCEPTED_RESULT="acceptResult";
     static String CHILD_NOMINEE_NAME="nomineeName";


    //SharedPReferences
    static String SHARED_USERNAME= "username";
    static String SHARED_NAME= "name";
    static String SHARED_UID= "uid";
    static String SHARED_EMAIL= "email";



    public static Utils getInstance()
    {
        if (UtilSharedPrefs == null)
        {
            UtilSharedPrefs = new Utils();
            widgetGameList=new ArrayList<>();
        }
        return UtilSharedPrefs;
    }

    public Utils() {
        super();
    }


    public void addGamesToWidgetList(List<Game> list) {
        widgetGameList.addAll(list);
    }
    public void addGameToWidgetList(Game game) {
        widgetGameList.add(game);

        //content provider
//        ContentValues contentValues=new ContentValues();
//        contentValues.put(GameProvider.NAME, ((EditText)findViewById(R.id.editText2)).getText().toString());
//        contentValues.put(GameProvider.GAME_ID,((EditText)findViewById(R.id.editText3)).getText().toString());
//        Uri uri=getContentResolver().insert(GameProvider.CONTENT_URI,contentValues);

    }
    public void removeGameFromWidgetList(Game game) {
        widgetGameList.remove(game);
    }
    public void clearWidgetGamesList(Context context) {

        widgetGameList.clear();

        //content provider clear
        Uri games=Uri.parse(Utils.CONTENT_URL+"/games");
        int count= context.getContentResolver().delete(games,null,null);

    }
    public List<Game> getWidgetGameList() {
        return widgetGameList;
    }
    public int getWidgetGameListCount() {
        return widgetGameList.size();
    }
    public Game getWidgetGameFromList(String gameId) {
   for(Game game:widgetGameList) {
    if(game.getUid().equals(gameId))
       return game;
   }
    return null;
    }

    public String getWidgetGameFromListContentProvider(String gameId,Context context) {
   /*for(Game game:widgetGameList) {
    if(game.getUid().equals(gameId))
       return game;
   }*/

        //content provider
        Uri games=Uri.parse(Utils.CONTENT_URL);
        Cursor cursor= context.getContentResolver().query(games,null,null,null,"name");//retrieve based on name //GameProvider._ID+"="+gameId

        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(cursor.getColumnIndex(GameProvider.NAME)).equals(gameId))
                    return cursor.getString(cursor.getColumnIndex(GameProvider.NAME));
//                        cursor.getString(cursor.getColumnIndex(GameProvider._ID))+
//                                ", "+cursor.getString(cursor.getColumnIndex(GameProvider.NAME))+
//                                ", "+cursor.getString(cursor.getColumnIndex(GameProvider.GAME_ID))+
//                                "."

            }while(cursor.moveToNext());
        }

    return null;
    }

    public void addGameToWidgetListContentProvider(Game game,Context context) {
        widgetGameList.add(game);

        //content provider
        ContentValues contentValues=new ContentValues();
        contentValues.put(GameProvider.NAME, game.getName());
        contentValues.put(GameProvider.GAME_ID,game.getUid());
        Uri uri=context.getContentResolver().insert(GameProvider.CONTENT_URI,contentValues);

    }

        public void save(Context context, String text , String Key) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putString(Key, text); //3

        editor.commit(); //4


    }

    public String getValue(Context context , String Key) {
        SharedPreferences settings;
        String text = "";
        //  settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(Key, "");
        return text;
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.clear();
        editor.commit();
    }

    public void removeValue(Context context , String value) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.remove(value);
        editor.commit();
    }




    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
