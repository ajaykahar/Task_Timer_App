package com.example.tasktimer.debug;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.example.tasktimer.TasksContract;
import com.example.tasktimer.TimingsContract;

import java.util.GregorianCalendar;

public class TestData {

    public static void generateTestData(ContentResolver contentResolver){
        final int SEC_IN_A_DAY = 86400;
        final int LOWER_BOUND = 100;
        final int UPPER_BOUND = 500;
        final int MAX_DURATION = SEC_IN_A_DAY/6;

        //get a list of task ID's from the database
        String[] projection = {TasksContract.columns._ID};
        Uri uri = TasksContract.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,projection,null,null,null);

        if (cursor!=null && cursor.moveToFirst()){
            do{
                long taskId = cursor.getLong(cursor.getColumnIndex(TasksContract.columns._ID));

                // generate between 100 and 500 random timings for this task
                int loopCount = LOWER_BOUND + getRandomInt(UPPER_BOUND-LOWER_BOUND);

                for (int i=0;i<loopCount;i++) {
                    long randomDate = randomDateTime();

                    // generate a random duration between 0 and 4 hours
                    long duration = (long) getRandomInt(MAX_DURATION);

                    // create new TestTiming object
                    TestTiming testTiming = new TestTiming(taskId, randomDate, duration);

                    //add it to the database
                    saveCurrentTiming(contentResolver, testTiming);
                }

            }while (cursor.moveToNext());
            cursor.close();
        }
    }
    private static int getRandomInt(int max){
        return (int) Math.round(Math.random()*max);
    }

    //method to generate a date and time
    private static long randomDateTime(){
        //set the range of years - change if necessary
        final int startYear = 2019;
        final int endYear = 2020;

        int sec = getRandomInt(59);
        int min = getRandomInt(59);
        int hour = getRandomInt(23);
        int month = getRandomInt(11);     // days depend on month (each month has different number of days therefore calculated below
        int year = startYear + getRandomInt(endYear-startYear);

        GregorianCalendar gregorianCalendar = new GregorianCalendar(year,month,1);
        int day = 1 + getRandomInt(gregorianCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)-1);

        gregorianCalendar.set(year,month,day,hour,min,sec);
        return gregorianCalendar.getTimeInMillis();
    }

    private static void saveCurrentTiming(ContentResolver contentResolver,TestTiming currentTiming){

        //save the timing record;
        ContentValues values = new ContentValues();
        values.put(TimingsContract.columns.TIMINGS_TASK_ID,currentTiming.taskId);
        values.put(TimingsContract.columns.TIMINGS_START_TIME,currentTiming.starTime);
        values.put(TimingsContract.columns.TIMINGS_DURATION,currentTiming.duration);

        // update the database
        contentResolver.insert(TimingsContract.CONTENT_URI,values);
    }

}
