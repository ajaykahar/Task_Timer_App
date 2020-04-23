package com.example.tasktimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AddEditActivityFragment extends Fragment {
    private static final String TAG = "AddEditActivityFragment";

    public  enum FragmentEditMode {EDIT,ADD}

    private FragmentEditMode mMode;

    private EditText mNameTextView;
    private EditText mDescriptionTextView;
    private EditText mSortOrderTextView;
    @SuppressWarnings("FieldCanBeLocal")
    private Button mSaveButton;

    private  OnSaveClicked mSaveListener;

    interface OnSaveClicked{
        void onSaveClicked();
    }


    public AddEditActivityFragment(){
        Log.d(TAG,"AddEditActivityFragment : constructor called" );
    }

    @SuppressWarnings("WeakerAccess")
    public boolean canClose(){
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: starts");
        super.onAttach(context);

        Activity activity = getActivity();
        if (!(activity instanceof OnSaveClicked)){
            //noinspection ConstantConditions
            throw new ClassCastException(activity.getClass().getSimpleName()+"must implement AddEditActivityFragment.OnSaveClicked interface");
        }

        mSaveListener = (OnSaveClicked) activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG,"onCreate : starts");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_edit,container,false);

        mNameTextView = view.findViewById(R.id.addedit_name);
        mDescriptionTextView = view.findViewById(R.id.addedit_description);
        mSortOrderTextView = view.findViewById(R.id.addedit_sortOrder);
        mSaveButton = view.findViewById(R.id.addedit_save_button);

//        Bundle arguments = getActivity().getIntent().getExtras(); //the line we will change later
        Bundle arguments = getArguments();

        final Task task;
        if(arguments!=null){
            Log.d(TAG,"onCreateView : retrieving task details");
            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            if (task!=null){
                Log.d(TAG,"onCreateView : task details found, editing ");
                mNameTextView.setText(task.getmName());
                mDescriptionTextView.setText(task.getmDescription());
                mSortOrderTextView.setText(Integer.toString(task.getmSortOrder()));
                mMode=FragmentEditMode.EDIT;
            }
            else {
                // no task found , so we must be adding new task, and not editing an existing task
                mMode=FragmentEditMode.ADD;
            }
        }else {
            task=null;
            Log.d(TAG,"onCreateView : No arguments, adding new record");
            mMode =FragmentEditMode.ADD;
        }
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update the database if at least one field has changed
                //- there is no need to hit database unless this has happened.
                int so; //sort order (text to int)
                if(mSortOrderTextView.length()>0){ so = Integer.parseInt(mSortOrderTextView.getText().toString());
                }else { so=0;}

                @SuppressWarnings("ConstantConditions")
                ContentResolver contentResolver = getActivity().getContentResolver();
                ContentValues values = new ContentValues();

                switch (mMode){
                    case EDIT:
                        //noinspection ConstantConditions
                        if (!mNameTextView.getText().toString().equals(task.getmName())){
                            values.put(TasksContract.columns.TASKS_NAME,mNameTextView.getText().toString());
                        }
                        if (!mDescriptionTextView.getText().toString().equals(task.getmDescription())){
                            values.put(TasksContract.columns.TASKS_DESCRIPTION,mDescriptionTextView.getText().toString());
                        }
                        if(so!= task.getmSortOrder()){
                            values.put(TasksContract.columns.TASKS_SORTORDER,so);
                        }
                        if (values.size()>0){
                            contentResolver.update(TasksContract.buildTaskUri(task.getId()),values,null,null);
                        }
                        break;
                    case ADD:
                        if(mNameTextView.length()>0){
                            values.put(TasksContract.columns.TASKS_NAME,mNameTextView.getText().toString());
                            values.put(TasksContract.columns.TASKS_DESCRIPTION,mDescriptionTextView.getText().toString());
                            values.put(TasksContract.columns.TASKS_SORTORDER,so);
                            contentResolver.insert(TasksContract.CONTENT_URI,values);
                        }
                        break;
                }

                Log.d(TAG,"onClick : Done editing");
                if (mSaveListener!=null){
                    mSaveListener.onSaveClicked();
                }

            }
        });

        Log.d(TAG,"onCreateView : Exiting....");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        @SuppressWarnings("ConstantConditions")
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        @SuppressWarnings("ConstantConditions")
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }
}
