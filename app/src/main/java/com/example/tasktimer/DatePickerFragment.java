package com.example.tasktimer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Date;
import java.util.GregorianCalendar;

//notes by me
// class created using new java class not by creating new fragment
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "DatePickerFragment";

    public static final String DATE_PICKER_ID = "ID";
    public static final String DATE_PICKER_TITLE = "TITLE";
    public static final String DATE_PICKER_DATE = "DATE";
    int mDialogId = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current date initially
        final GregorianCalendar calendar = new GregorianCalendar();
        String title = null;

        Bundle arguments = getArguments();
        if (arguments!=null){
            mDialogId = arguments.getInt(DATE_PICKER_ID);
            title = arguments.getString(DATE_PICKER_TITLE);

            // if we passed a date use it, otherwise set the calender to current date
            Date givenDate = (Date) arguments.getSerializable(DATE_PICKER_DATE);
            if (givenDate!=null){
                calendar.setTime(givenDate);
                Log.d(TAG, "onCreateDialog: retrieving date  = "+givenDate.toString());
            }
        }
        int year = calendar.get(GregorianCalendar.YEAR);
        int month = calendar.get(GregorianCalendar.MONTH);
        int day = calendar.get(GregorianCalendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),this,year,month,day);
        if (title!=null){
            datePickerDialog.setTitle(title);
        }
        return datePickerDialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Activities using this dialog must implements its callbacks
        if (!(context instanceof DatePickerDialog.OnDateSetListener)){
            throw new ClassCastException(context.toString()+ " must implement DatePickerDialog.OnDateSetListener interface");
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "onDateSet: Entering");
        DatePickerDialog.OnDateSetListener listener = (DatePickerDialog.OnDateSetListener) getActivity();
        if (listener != null){
            // Notify caller of the user-selected values
            view.setTag(mDialogId);
            listener.onDateSet(view,year,month,dayOfMonth);
        }
        Log.d(TAG, "onDateSet: Exiting");
    }
}
