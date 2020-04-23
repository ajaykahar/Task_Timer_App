package com.example.tasktimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AppDialog extends DialogFragment {
    public static final String TAG = "AppDialog";

    public static final String DIALOG_ID="id";
    public static final String DIALOG_MESSAGE="message";
    public static final String DIALOG_POSITIVE_RID="positive_rid";
    public static final String DIALOG_NEGATIVE_RID="negative_rid";

    interface DialogEvents{
        void onPositiveDialogResult(int dialogId, Bundle args);
        void onNegativeDialogResult(int dialogId, Bundle args);
        void onDialogCancelled(int dialogId);
    }

    private DialogEvents mDialogEvents;

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: Entering onAttach, Activity is "+context.toString());
        super.onAttach(context);

        //Activities containing this fragment must implements its callbacks.
        if(!(context instanceof DialogEvents)){
            throw new ClassCastException(context.toString()+" must implement AppDailog.DailogEvents interface.");
        }

        mDialogEvents = (DialogEvents) context;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: Starts");
        super.onDetach();
        // Reset the active callbacks interface, because we don't have an activity any longer.
        mDialogEvents = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: starts");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Bundle arguments = getArguments();
        final int dailogId;
        String messageString;
        int positiveStringId;
        int negativeStringId;

        if (arguments!= null){
            dailogId = arguments.getInt(DIALOG_ID);
            messageString = arguments.getString(DIALOG_MESSAGE);

            if (dailogId ==0 || messageString == null){
                throw new IllegalArgumentException("DIALOG_ID and/or DIALOG_MESSAGE not present int the bundle");
            }

            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID);
            if (positiveStringId==0){
                positiveStringId = R.string.ok;
            }
            negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID);
            if (negativeStringId==0){
                negativeStringId = R.string.cancel;
            }

        }else {
            throw new IllegalArgumentException("Must pass DIALOG_ID and DIALOG_MESSAGE in the Bundle ");
        }

        builder.setMessage(messageString).setPositiveButton(positiveStringId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //callback positive result method
                if (mDialogEvents!=null){
                mDialogEvents.onPositiveDialogResult(dailogId,arguments);}
            }
        }).setNegativeButton(negativeStringId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //callback negative result method
                if (mDialogEvents!=null){
                mDialogEvents.onNegativeDialogResult(dailogId,arguments);}
            }
        });
        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        Log.d(TAG, "onCancel: called ");
        if (mDialogEvents!=null){
            int dialogId = getArguments().getInt(DIALOG_ID);
            mDialogEvents.onDialogCancelled(dialogId);
        }
    }

//    @Override
//    public void onDismiss(@NonNull DialogInterface dialog) {  // do not use this unless you want this function
//        Log.d(TAG, "onDismiss: called");
//        super.onDismiss(dialog); //this line is very important
//
//    }
}
