package com.example.tasktimer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * basic database class for application.
 * The only class that should use this is AppProvider.
 */
class AppDatabase extends SQLiteOpenHelper {
    private static final String TAG = "AppDatabase";
    public static final String DATABASE_NAME = "TaskTimer.db";
    public static final int DATABASE_VERSION = 3;

    //Implement AppDatabase as Singleton
    private static AppDatabase instance = null;

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "AppDatabase constructor");
        //reason for private constructor is it prevents from making instance of the class
        //by this way we have only one user or one way to access to database (all members are static here)
        //we want only one instance of the class (such class called singleton)
        //but by making private constructor we can not create instance
        // then how we do it. Ans. by using a static function that return instance of this class
    }

    /**
     * Get an instance of app's singleton database helper object (instance of AppDatabase class)
     *
     * @param context the content provider context
     * @return a SQLite Database helper
     */
    static AppDatabase getInstance(Context context) {
        if (instance == null) {
            Log.d(TAG, "getInstance : Creating new instance");
            instance = new AppDatabase(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate : Starts");
        String sSQL;
//        sSQL="CREATE TABLE Tasks ( _id INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL,Description TEXT,SortOrder INTEGER);";
        sSQL = "CREATE TABLE " + TasksContract.TABLE_NAME + " ("
                + TasksContract.columns._ID + " INTEGER PRIMARY KEY NOT NULL, "
                + TasksContract.columns.TASKS_NAME + " TEXT NOT NULL, "
                + TasksContract.columns.TASKS_DESCRIPTION + " TEXT, "
                + TasksContract.columns.TASKS_SORTORDER + " INTEGER);";
        Log.d(TAG, sSQL);
        db.execSQL(sSQL);

        addTimingsTable(db);
        addDurationsView(db);

        Log.d(TAG, "onCreate : ends");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade : starts");
        switch (oldVersion) {
            case 1:
                //upgrade logic from version 1
                addTimingsTable(db);
                // fall through, to include version 2 upgrade logic as well  // therefore break after this case is removed
            case 2:
                // upgrade logic from version 2
                addDurationsView(db);
                break;
            default:
                throw new IllegalStateException("onUpgrade with unknown newVersion " + newVersion);
        }
        Log.d(TAG, "onUpgrade : ends");
    }

    private void addTimingsTable(SQLiteDatabase db){
        String sSQL = "CREATE TABLE " + TimingsContract.TABLE_NAME + " ("
                +TimingsContract.columns._ID + " INTEGER PRIMARY KEY NOT NULL, "
                +TimingsContract.columns.TIMINGS_TASK_ID + " INTEGER NOT NULL, "
                +TimingsContract.columns.TIMINGS_START_TIME + " INTEGER, "
                +TimingsContract.columns.TIMINGS_DURATION + " INTEGER);";

        Log.d(TAG, sSQL);
        db.execSQL(sSQL);

        sSQL = "CREATE TRIGGER Remove_Task"
                + " AFTER DELETE ON "+TasksContract.TABLE_NAME
                + " FOR EACH ROW"
                + " BEGIN"
                + " DELETE FROM "+ TimingsContract.TABLE_NAME
                + " WHERE "+TimingsContract.columns.TIMINGS_TASK_ID + " = OLD." + TasksContract.columns._ID + ";"
                + " END;";

        Log.d(TAG, sSQL);
        db.execSQL(sSQL);
    }

    private void addDurationsView(SQLiteDatabase db){
        /*
         CREATE VIEW vwTaskDurations AS
         SELECT Timings._id,
         Tasks.Name,
         Tasks.Description,
         Timings.StartTime,
         DATE(Timings.StartTime, 'unixepoch') AS StartDate,
         SUM(Timings.Duration) AS Duration
         FROM Tasks INNER JOIN Timings
         ON Tasks._id = Timings.TaskId
         GROUP BY Tasks._id, StartDate;
         there is a mistake in last line
         */
        String sSQL = "CREATE VIEW " + DurationsContract.TABLE_NAME
                + " AS SELECT " + TimingsContract.TABLE_NAME + "." + TimingsContract.columns._ID + ", "
                + TasksContract.TABLE_NAME + "." + TasksContract.columns.TASKS_NAME + ", "
                + TasksContract.TABLE_NAME + "." + TasksContract.columns.TASKS_DESCRIPTION + ", "
                + TimingsContract.TABLE_NAME + "." + TimingsContract.columns.TIMINGS_START_TIME + ","
                + " DATE(" + TimingsContract.TABLE_NAME + "." + TimingsContract.columns.TIMINGS_START_TIME + ", 'unixepoch')"
                + " AS " + DurationsContract.columns.DURATIONS_START_DATE + ","
                + " SUM(" + TimingsContract.TABLE_NAME + "." + TimingsContract.columns.TIMINGS_DURATION + ")"
                + " AS " + DurationsContract.columns.DURATIONS_DURATION
                + " FROM " + TasksContract.TABLE_NAME + " JOIN " + TimingsContract.TABLE_NAME
                + " ON " + TasksContract.TABLE_NAME + "." + TasksContract.columns._ID + " = "
                + TimingsContract.TABLE_NAME + "." + TimingsContract.columns.TIMINGS_TASK_ID
                + " GROUP BY " + DurationsContract.columns.DURATIONS_START_DATE + ", " + DurationsContract.columns.DURATIONS_NAME
                + ";";

        Log.d(TAG, sSQL);
        db.execSQL(sSQL);
    }
}

