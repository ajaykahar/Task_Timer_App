package com.example.tasktimer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tasktimer.debug.TestData;

public class MainActivity extends AppCompatActivity implements CursorRecyclerViewAdapter.OnTaskClickListener,
                                                               AddEditActivityFragment.OnSaveClicked,
                                                               AppDialog.DialogEvents
{
    private static final String TAG = "MainActivity";

    //whether or not the activity is in 2-pane mode
    //i.e. running in landscape in tablet
    private boolean mTwoPane = false;
    
    public static final int DIALOG_ID_DELETE =1;
    public static final int DIALOG_ID_CANCEL_EDIT =2;  // for back button
    private static final int DIALOG_ID_CANCEL_EDIT_UP = 3; // for up button

    private AlertDialog mDialog =null;   // module scope because we need to dismiss it in onStop
                                         //( e.g. when orientation changes) to avoid memory leaks


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        if (findViewById(R.id.task_details_container) != null){
//            //The detail container view will be present only in th large-screen layouts (res/values-land and res/values-sw600dp)
//            //If this view is present, then the activity should be in two-pane mode
//            mTwoPane = true;
//        }

        mTwoPane = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        Log.d(TAG, "onCreate: Two pane is "+ mTwoPane);

        FragmentManager fragmentManager = getSupportFragmentManager();
        // if AddEditActivity fragment exists, we are editing
        Boolean editing  = fragmentManager.findFragmentById(R.id.task_details_container) != null;
        Log.d(TAG, "onCreate: editing is "+editing);

        //we need references to the containers, so we can show or hide them as necessary
        // No need to cast them, as we are only calling a method that's available for all views.

        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFragment = findViewById(R.id.fragment);

        if (mTwoPane){
            Log.d(TAG, "onCreate: twoPane mode");
            mainFragment.setVisibility(View.VISIBLE);
            addEditLayout.setVisibility(View.VISIBLE);
        }else if (editing){
            mainFragment.setVisibility(View.GONE);
        }else {
            mainFragment.setVisibility(View.VISIBLE);
            addEditLayout.setVisibility(View.GONE);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (BuildConfig.DEBUG){
            MenuItem generate = menu.findItem(R.id.menumain_generateData);
            generate.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.menumain_addTask:
                taskEditRequest(null);
                break;
            case R.id.menumain_showDuration:
                startActivity(new Intent(this,DurationsReport.class));
                break;
            case R.id.menumain_settings:
                break;
            case R.id.menumain_showAbout:
                showAboutDialog();
                break;
            case R.id.menumain_generateData:
                TestData.generateTestData(getContentResolver());
                break;
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: home button pressed");
                FragmentManager fragmentManager = getSupportFragmentManager();
                AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.task_details_container);

                if (fragment.canClose()){
                    return super.onOptionsItemSelected(item);
                }else {
                    //show dialog to get confirmation to quit editing
                    showConfirmationDialog(DIALOG_ID_CANCEL_EDIT_UP);
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }
    public void showAboutDialog(){
        View messageView = getLayoutInflater().inflate(R.layout.about,null,false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(messageView);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDialog!=null && mDialog.isShowing()){
                    mDialog.dismiss();
                }
            }
        });

        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(true);

        TextView tv = messageView.findViewById(R.id.about_version);
        tv.setText("v"+BuildConfig.VERSION_NAME);

        mDialog.show();
    }

    @Override
    public void onEditClick(@NonNull Task task) {
        taskEditRequest(task);
    }

    @Override
    public void onDeleteClick(@NonNull Task task) {
        Log.d(TAG, "onDeleteClick: starts");

        AppDialog dialog = new AppDialog();
        Bundle arguments = new Bundle();
        arguments.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE);
        arguments.putString(AppDialog.DIALOG_MESSAGE,getString(R.string.deldiag_message,task.getId(),task.getmName()));
        arguments.putInt(AppDialog.DIALOG_POSITIVE_RID,R.string.deldial_positive_caption);

        arguments.putLong("TaskId",task.getId()); //for retrieving task to delete in OnPositiveDialogResult() method.

        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(),null);

//        getContentResolver().delete(TasksContract.buildTaskUri(task.getId()),null,null); //this  line is now moved to OnPositiveDialogResult() method.
    }

    @Override
    public void onSaveClicked() {
        Log.d(TAG, "onSaveClicked: starts");
        FragmentManager fragmentManager  = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.task_details_container);
        if (fragment!=null){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }

        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFragment = findViewById(R.id.fragment);

        if (!mTwoPane){
            addEditLayout.setVisibility(View.GONE);
            mainFragment.setVisibility(View.VISIBLE);
        }
    }

    private void taskEditRequest(Task task){
        Log.d(TAG,"tskEditRequest : starts");

            Log.d(TAG,"taskEditRequest : in two-pane mode (tablet)");
            AddEditActivityFragment fragment = new AddEditActivityFragment();

            Bundle arguments = new Bundle();
            arguments.putSerializable(Task.class.getSimpleName(),task);
            fragment.setArguments(arguments);

//            FragmentManager fragmentManager = getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.task_details_container,fragment);
//            fragmentTransaction.commit();
            //above four line of code in single line
            getSupportFragmentManager().beginTransaction().replace(R.id.task_details_container,fragment).commit();

         if (!mTwoPane){
             Log.d(TAG, "taskEditRequest: in single pane mode");

             View mainFragment = findViewById(R.id.fragment);
             View addEditLayout = findViewById(R.id.task_details_container);

             mainFragment.setVisibility(View.GONE);
             addEditLayout.setVisibility(View.VISIBLE);
             
         }
        Log.d(TAG, "taskEditRequest: Exiting");
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult: called");
        switch (dialogId){
            case DIALOG_ID_DELETE:
                long  taskId = args.getLong("TaskId");
                if (BuildConfig.DEBUG && taskId==0 ) throw new AssertionError("TaskId is Zero. ");
                getContentResolver().delete(TasksContract.buildTaskUri(taskId),null,null);
                break;
            case DIALOG_ID_CANCEL_EDIT:
            case DIALOG_ID_CANCEL_EDIT_UP:
                // no action required
                break;
        }

    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onNegativeDialogResult: called");
        switch (dialogId){
            case DIALOG_ID_DELETE:
                // no action required
                break;
            case DIALOG_ID_CANCEL_EDIT:
            case DIALOG_ID_CANCEL_EDIT_UP:
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentById(R.id.task_details_container);

                if (fragment!=null){
                    //we were editing
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    if (mTwoPane){
                        if (dialogId == DIALOG_ID_CANCEL_EDIT){
                               finish();}
                    }else {
                        // single-pane
                        //hide the edit container in single-pane mode
                        // make sure that left hand container is visible
                        View addEditLayout = findViewById(R.id.task_details_container);
                        View maimFragment = findViewById(R.id.fragment);

                        addEditLayout.setVisibility(View.GONE);
                        maimFragment.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    // no editing, quit regardless of orientation
                    finish();
                }
                break;
        }
    }

    @Override
    public void onDialogCancelled(int dialogId) {
        Log.d(TAG, "onDialogCancelled: called");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.task_details_container);

        if ((fragment==null) || fragment.canClose()){
            super.onBackPressed();
        }else {
            //show dialog to get confirmation to quit editing
            showConfirmationDialog(DIALOG_ID_CANCEL_EDIT);
//            AppDialog dialog = new AppDialog();
//            Bundle args = new Bundle();
//            args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
//            args.putString(AppDialog.DIALOG_MESSAGE,getString(R.string.cancelEditDiag_message));
//            args.putInt(AppDialog.DIALOG_POSITIVE_RID,R.string.cancelEditDiag_positive_caption);
//            args.putInt(AppDialog.DIALOG_NEGATIVE_RID,R.string.cancelEditDiag_negative_caption);
//
//            dialog.setArguments(args);
//            dialog.show(getSupportFragmentManager(),null);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog!=null && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    void showConfirmationDialog(int dailogId){
        //show dialog to get confirmation to quit editing
        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, dailogId);
        args.putString(AppDialog.DIALOG_MESSAGE,getString(R.string.cancelEditDiag_message));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID,R.string.cancelEditDiag_positive_caption);
        args.putInt(AppDialog.DIALOG_NEGATIVE_RID,R.string.cancelEditDiag_negative_caption);

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(),null);
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {
        // required to satisfy the interface
    }
}
