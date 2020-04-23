package com.example.tasktimer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * provider for Task Timer app.
 * this is the only that knows about {@link AppDatabase}
 */

public class AppProvider extends ContentProvider {
    private static final String TAG = "AppProvider";

    AppDatabase mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final String CONTENT_AUTHORITY = "com.example.tasktimer.provider";
    public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final int TASK = 100;
    private static final int TASKS_ID = 101;

    private static final int TIMINGS = 200;
    private static final int TIMINGS_ID = 201;

    /*
    private static final int TASK_TIMINGS = 300;
    private static final int TASK_TIMINGS_ID = 301;
     */
    private static final int TASK_DURATIONS = 400;
    private static final int TASK_DURATIONS_ID = 401;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // eg.  content://com.example.tasktimer.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASK);
        // eg.  content://com.example.tasktimer.provider/Tasks/7
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASKS_ID);

        matcher.addURI(CONTENT_AUTHORITY,TimingsContract.TABLE_NAME,TIMINGS);
        matcher.addURI(CONTENT_AUTHORITY,TimingsContract.TABLE_NAME+"/#",TIMINGS_ID);

        matcher.addURI(CONTENT_AUTHORITY,DurationsContract.TABLE_NAME,TASK_DURATIONS);
        matcher.addURI(CONTENT_AUTHORITY,DurationsContract.TABLE_NAME+"/#",TASK_DURATIONS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.d(TAG, "query : called with uri " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "query : match is " + match);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match) {
            case TASK:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                break;
            case TASKS_ID:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                long taskId = TasksContract.getTaskId(uri);
                queryBuilder.appendWhere(TasksContract.columns._ID + "  = " + taskId);
                break;
            case TIMINGS:
                queryBuilder.setTables(TimingsContract.TABLE_NAME);
                break;
            case TIMINGS_ID:
                queryBuilder.setTables(TimingsContract.TABLE_NAME);
                long timingId = TimingsContract.getTimingId(uri);
                queryBuilder.appendWhere(TimingsContract.columns._ID+"  = "+timingId);
                break;
            case TASK_DURATIONS:
                queryBuilder.setTables(DurationsContract.TABLE_NAME);
                break;
            case TASK_DURATIONS_ID:
                queryBuilder.setTables(DurationsContract.TABLE_NAME);
                long durationId = DurationsContract.getDurationId(uri);
                queryBuilder.appendWhere(DurationsContract.columns._ID+"  = "+durationId);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        Log.d(TAG, "query: rows in returned corsor  = "+cursor.getCount());

        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case TASK:
                return TasksContract.CONTENT_TYPE;
            case TASKS_ID:
                return TasksContract.CONTENT_ITEM_TYPE;
            case TIMINGS:
                return TimingsContract.CONTENT_TYPE;
            case TIMINGS_ID:
              return TimingsContract.CONTENT_ITEM_TYPE;
            case TASK_DURATIONS:
               return DurationsContract.CONTENT_TYPE;
            case TASK_DURATIONS_ID:
                return DurationsContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(TAG,"Entering insert, called with uri "+uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG,"insert : match is :" + match);

        final SQLiteDatabase db;
        Uri returnUri;
        long recordId;
        switch (match){
            case TASK:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TasksContract.TABLE_NAME,null,values);
                if (recordId>=0){
                    returnUri = TasksContract.buildTaskUri(recordId);
                }else {
                    throw new android.database.SQLException("Failed to insert into "+ uri.toString());
                }
                break;
            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TimingsContract.TABLE_NAME,null,values);
                if (recordId>=0){
                    returnUri = TimingsContract.buildTimingUri(recordId);
                }else {
                    throw new android.database.SQLException("Failed to insert into "+ uri.toString());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown uri "+ uri);
        }

        //noinspection ConstantConditions
        if (recordId>=0){
            //something was inserted
            Log.d(TAG, "insert: setting notifyChange with "+uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri,null);
        }else {
            Log.d(TAG, "insert: nothing inserted");
        }
        Log.d(TAG,"Exiting insert : returning uri "+returnUri);

       return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG,"Delete called with uri "+uri);
        final int match = sUriMatcher.match(uri);
        int count;

        Log.d(TAG,"delete : match is "+match);
        SQLiteDatabase db;

        String selectionCriteria;

        switch (match){
            case TASK:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(TasksContract.TABLE_NAME,selection,selectionArgs);
                break;
            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.columns._ID+" = "+taskId;
                if ((selection!=null) && (selection.length()>0)){
                    selectionCriteria+=" AND ("+selection+")";
                }
                count = db.delete(TasksContract.TABLE_NAME,selectionCriteria,selectionArgs);
                break;

            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(TimingsContract.TABLE_NAME,selection,selectionArgs);
                break;
            case TIMINGS_ID:
                db = mOpenHelper.getWritableDatabase();
                long timingId = TimingsContract.getTimingId(uri);
                selectionCriteria = TimingsContract.columns._ID+" = "+timingId;
                if ((selection!=null) && (selection.length()>0)){
                    selectionCriteria+=" AND ("+selection+")";
                }
                count = db.delete(TimingsContract.TABLE_NAME,selectionCriteria,selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri "+uri);
        }
        if (count>0){ //something is deleted
            Log.d(TAG, "delete: setting notifyChange with "+uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri,null);
        }
        else {
            Log.d(TAG, "delete: nothing deleted");
        }
        Log.d(TAG,"Exiting delete , returning count  = "+count);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG,"Update called with uri "+uri);
        final int match = sUriMatcher.match(uri);
        int count;

        Log.d(TAG,"update : match is "+match);
        SQLiteDatabase db;

        String selectionCriteria;

        switch (match){
            case TASK:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(TasksContract.TABLE_NAME,values,selection,selectionArgs);
                break;
            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.columns._ID+" = "+taskId;
                if ((selection!=null) && (selection.length()>0)){
                    selectionCriteria+=" AND ("+selection+")";
                }
                count = db.update(TasksContract.TABLE_NAME,values,selectionCriteria,selectionArgs);
                break;

            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(TimingsContract.TABLE_NAME,values,selection,selectionArgs);
                break;
            case TIMINGS_ID:
                db = mOpenHelper.getWritableDatabase();
                long timingId = TimingsContract.getTimingId(uri);
                selectionCriteria = TimingsContract.columns._ID+" = "+timingId;
                if ((selection!=null) && (selection.length()>0)){
                    selectionCriteria+=" AND ("+selection+")";
                }
                count = db.update(TimingsContract.TABLE_NAME,values,selectionCriteria,selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri "+uri);
        }
        if (count>0){ //something is deleted
            Log.d(TAG, "update: setting notifyChange with "+uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri,null);
        }
        else {
            Log.d(TAG, "update: nothing updated");
        }
        Log.d(TAG,"Exiting update , returning count  = "+count);
        return count;
    }
}
