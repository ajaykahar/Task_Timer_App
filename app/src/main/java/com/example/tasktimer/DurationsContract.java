package com.example.tasktimer;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.example.tasktimer.AppProvider.CONTENT_AUTHORITY;
import static com.example.tasktimer.AppProvider.CONTENT_AUTHORITY_URI;

public class DurationsContract {
    public static final String TABLE_NAME = "vwTaskDurations";

    public class columns{
        public static final String _ID = BaseColumns._ID;
        public static final String DURATIONS_NAME = TasksContract.columns.TASKS_NAME;
        public static final String DURATIONS_DESCRIPTION = TasksContract.columns.TASKS_DESCRIPTION;
        public static final String DURATIONS_START_TIME = TimingsContract.columns.TIMINGS_START_TIME;
        public static final String DURATIONS_START_DATE = "StartDate";
        public static final String DURATIONS_DURATION = TimingsContract.columns.TIMINGS_DURATION;



        private columns() {
            //private constructor to prevent instantiation
        }
    }
    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME);

    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;


    public static long getDurationId(Uri uri) {
        return ContentUris.parseId(uri);
    }
}
