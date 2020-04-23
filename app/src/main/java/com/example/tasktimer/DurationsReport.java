package com.example.tasktimer;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DurationsReport extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
                                                                    DatePickerDialog.OnDateSetListener,
                                                                    AppDialog.DialogEvents,
                                                                    View.OnClickListener {

    private static final String TAG = "DurationsReport";

    private static final int LOADER_ID = 1;

    public static final int DIALOG_FILTER = 1;
    public static final int DIALOG_DELETE = 2;

    private static final String SELECTION_PARAM = "SELECTION";
    private static final String SELECTION_ARGS_PARAM  = "SELECTION_ARGS";
    private static final String SORT_ORDER_PARAM = "SORT_ORDER";

    public static final String CURRENT_DATE = "CURRENT_DATE";
    public static final String DISPLAY_WEEK = "DISPLAY_WEEK";

    public static final String DELETION_DATE  ="DELETION_DATE";

    //module level arguments - so when we change sort order, for example, the selection is retained (and vice-vera)
    private Bundle mArgs = new Bundle();
    private boolean mDisplayWeek = true;

    private  DurationsRecyclerViewAdapter mAdapter;

    private final GregorianCalendar mCalendar = new GregorianCalendar();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_durations_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            // show the up button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        if (savedInstanceState!=null){
            long timeInMillis = savedInstanceState.getLong(CURRENT_DATE,0);
            // it will be zero when the activity is first created, so don't set the value
            if (timeInMillis != 0){
                mCalendar.setTimeInMillis(timeInMillis);
                // make sure the time part is cleared, because we will filter the database by seconds
                mCalendar.clear(GregorianCalendar.HOUR_OF_DAY);
                mCalendar.clear(GregorianCalendar.MINUTE);
                mCalendar.clear(GregorianCalendar.SECOND);
            }
            mDisplayWeek = savedInstanceState.getBoolean(DISPLAY_WEEK,true);
        }

        applyFilter();

        // set the listener for the buttons, so we can sort the report
        TextView taskName = findViewById(R.id.td_name_heading);
        taskName.setOnClickListener(this);

        TextView taskDesc = findViewById(R.id.td_description_heading);
        if (taskDesc != null){
            taskDesc.setOnClickListener(this);
        }

        TextView taskDate = findViewById(R.id.td_start_heading);
        taskDate.setOnClickListener(this);

        TextView taskDuration = findViewById(R.id.td_duration_heading);
        taskDuration.setOnClickListener(this);


        RecyclerView recyclerView = findViewById(R.id.td_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //create an empty adapter we will use, to display the loaded data
        if (mAdapter == null){
            mAdapter = new DurationsRecyclerViewAdapter(this,null);
        }
        recyclerView.setAdapter(mAdapter);

        LoaderManager.getInstance(this).initLoader(LOADER_ID,mArgs,this);


    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: called");
        switch (view.getId()){
            case R.id.td_name_heading:
                mArgs.putString(SORT_ORDER_PARAM,DurationsContract.columns.DURATIONS_NAME);
                break;
            case R.id.td_description_heading:
                mArgs.putString(SORT_ORDER_PARAM,DurationsContract.columns.DURATIONS_DESCRIPTION);
                break;
            case R.id.td_start_heading:
                mArgs.putString(SORT_ORDER_PARAM,DurationsContract.columns.DURATIONS_START_DATE);
                break;
            case R.id.td_duration_heading:
                mArgs.putString(SORT_ORDER_PARAM,DurationsContract.columns.DURATIONS_DURATION);
                break;
        }
        LoaderManager.getInstance(this).restartLoader(LOADER_ID,mArgs,this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CURRENT_DATE,mCalendar.getTimeInMillis());
        outState.putBoolean(DISPLAY_WEEK,mDisplayWeek);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.rm_filter_period:
                mDisplayWeek = !mDisplayWeek;  // was showing a week, so now show a day or vice-versa
                applyFilter();
                invalidateOptionsMenu();
                LoaderManager.getInstance(this).restartLoader(LOADER_ID,mArgs,this);
                return true;
            case R.id.rm_filter_date:
                showDatePickerDialog(getString(R.string.date_title_filter),DIALOG_FILTER);  // The actual filtering id done in onDateSet();
                return true;
            case R.id.rm_delete:
                showDatePickerDialog(getString(R.string.date_title_delete),DIALOG_DELETE);  // The actual filtering id done in onDateSet();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item  = menu.findItem(R.id.rm_filter_period);
        if (mDisplayWeek){
            item.setIcon(R.drawable.ic_filter_1_black_24dp);
            item.setTitle(R.string.rm_title_filter_day);
        }else {
            item.setIcon(R.drawable.ic_filter_7_black_24dp);
            item.setTitle(R.string.rm_title_filter_week);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void showDatePickerDialog(String title,int dialogId){
        Log.d(TAG, "showDatePickerDialog: Entering");
        DialogFragment dialogFragment = new DatePickerFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(DatePickerFragment.DATE_PICKER_ID,dialogId);
        arguments.putString(DatePickerFragment.DATE_PICKER_TITLE,title);
        arguments.putSerializable(DatePickerFragment.DATE_PICKER_DATE,mCalendar.getTime());

        dialogFragment.setArguments(arguments);
        dialogFragment.show(getSupportFragmentManager(),"datePicker");
        Log.d(TAG, "showDatePickerDialog: Exiting");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // check the id, so we know what to do with the result
        int dialogId = (int)view.getTag();
        switch (dialogId){
            case DIALOG_FILTER:
                mCalendar.set(year,month,dayOfMonth,0,0,0);
                applyFilter();
                LoaderManager.getInstance(this).restartLoader(LOADER_ID,mArgs,this);
                break;
            case DIALOG_DELETE:
                mCalendar.set(year,month,dayOfMonth,0,0,0);
                String fromDate = DateFormat.getDateFormat(this).format(mCalendar.getTimeInMillis());
                AppDialog dialog = new AppDialog();
                Bundle args = new Bundle();
                args.putInt(AppDialog.DIALOG_ID,1);
                args.putString(AppDialog.DIALOG_MESSAGE,getString(R.string.delete_timing_message,fromDate));
                args.putLong(DELETION_DATE,mCalendar.getTimeInMillis());
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(),null);
                break;
        }
    }

    private void deleteRecords(long timeInMillis){
        Log.d(TAG, "deleteRecords: Entering ");

        long longDate = timeInMillis/1000;   // we want time in seconds, not in milliseconds
        String[] selectionArgs = new String[] {Long.toString(longDate)};
        String selection = TimingsContract.columns.TIMINGS_START_TIME + " < ?";
        Log.d(TAG, "deleteRecords: deleting Records prior to "+ longDate);

        ContentResolver contentResolver = getContentResolver();
        contentResolver.delete(TimingsContract.CONTENT_URI,selection,selectionArgs);
        applyFilter();
        LoaderManager.getInstance(this).restartLoader(LOADER_ID,mArgs,this);
        Log.d(TAG, "deleteRecords: Exiting");
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult:  called");
        long deleteDate = args.getLong(DELETION_DATE);
        //clear all the records from the timings table prior to the date selected.
        deleteRecords(deleteDate);
        // re-query, in case we've deleted records that currently being shown
        LoaderManager.getInstance(this).restartLoader(LOADER_ID,mArgs,this);
    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {

    }

    @Override
    public void onDialogCancelled(int dialogId) {

    }

    private void applyFilter(){
        Log.d(TAG, "applyFilter: Starts");

        if (mDisplayWeek){
            //show records for entire week

            Date currentCalenderDate = mCalendar.getTime(); // store the time so we can put it back

            // we have a date, so find out which day of it is.
            int dayOfWeek = mCalendar.get(GregorianCalendar.DAY_OF_WEEK);
            int weekStart = mCalendar.getFirstDayOfWeek();
            Log.d(TAG, "applyFilter: first day of calender week is "+weekStart);
            Log.d(TAG, "applyFilter: day of week is "+dayOfWeek);
            Log.d(TAG, "applyFilter: date is "+mCalendar.getTime());

            // calculate week start and end dates
            mCalendar.set(GregorianCalendar.DAY_OF_WEEK,weekStart);

            String startDate = String.format(Locale.US,"%04d-%02d-%02d",
                    mCalendar.get(GregorianCalendar.YEAR),
                    mCalendar.get(GregorianCalendar.MONTH)+1,
                    mCalendar.get(GregorianCalendar.DAY_OF_MONTH));

            mCalendar.add(GregorianCalendar.DATE,6); // move forward 6 days to get the last day of the week

            String endDate = String.format(Locale.US,"%04d-%02d-%02d",
                    mCalendar.get(GregorianCalendar.YEAR),
                    mCalendar.get(GregorianCalendar.MONTH)+1,
                    mCalendar.get(GregorianCalendar.DAY_OF_MONTH));

            String[] selectionArgs = new String[] {startDate,endDate};
            // put the calender back to where it was before we started jumping back and forth
            mCalendar.setTime(currentCalenderDate);
            Log.d(TAG, "In applyFilter(7): start date is "+ startDate +", end date is "+endDate);
            mArgs.putString(SELECTION_PARAM,"StartDate Between ? AND ?");
            mArgs.putStringArray(SELECTION_ARGS_PARAM,selectionArgs);

        }else {
            // re-query for for the current day.
            String startDate = String.format(Locale.US,"%04d-%02d-%02d",
                    mCalendar.get(GregorianCalendar.YEAR),
                    mCalendar.get(GregorianCalendar.MONTH)+1,
                    mCalendar.get(GregorianCalendar.DAY_OF_MONTH));
            String[] selectionArgs = new String[] {startDate};
            Log.d(TAG, "In applyFilter(1): start date is "+ startDate);
            mArgs.putString(SELECTION_PARAM,"StartDate = ?");
            mArgs.putStringArray(SELECTION_ARGS_PARAM,selectionArgs);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (id){
            case LOADER_ID:
                String[] projection ={BaseColumns._ID,
                        DurationsContract.columns.DURATIONS_NAME,
                        DurationsContract.columns.DURATIONS_DESCRIPTION,
                        DurationsContract.columns.DURATIONS_START_TIME,
                        DurationsContract.columns.DURATIONS_START_DATE,
                        DurationsContract.columns.DURATIONS_DURATION  };

                String selection = null;
                String[] selectionArgs = null;
                String sortOrder = null;

                if (args!=null){
                    selection = args.getString(SELECTION_PARAM);
                    selectionArgs = args.getStringArray(SELECTION_ARGS_PARAM);
                    sortOrder = args.getString(SORT_ORDER_PARAM);
                }
                return new CursorLoader(this,
                                    DurationsContract.CONTENT_URI,
                                    projection,
                                    selection,
                                    selectionArgs,
                                    sortOrder);
            default:
                throw new InvalidParameterException(TAG+".onCreateLoader called with invalid loader id "+id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: starts");
        mAdapter.swapCursor(data);
        int count=mAdapter.getItemCount();

        Log.d(TAG, "onLoadFinished: count is "+count);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: starts");
        mAdapter.swapCursor(null);
    }
}
