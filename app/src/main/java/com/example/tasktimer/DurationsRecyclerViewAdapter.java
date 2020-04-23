package com.example.tasktimer;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class DurationsRecyclerViewAdapter extends RecyclerView.Adapter<DurationsRecyclerViewAdapter.ViewHolder> {

    private  Cursor mCursor;
    private final java.text.DateFormat mDateFormat; // module level so we don't keep instantiating in bindView.

    public DurationsRecyclerViewAdapter(Context context, Cursor mCursor) {
        this.mCursor = mCursor;
        mDateFormat = DateFormat.getDateFormat(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_duration_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if ((mCursor!=null) && (mCursor.getCount()!=0)){
            if (!mCursor.moveToPosition(position)){
                throw new IllegalStateException("Couldn't move cursor to position "+position);
            }

            String name  = mCursor.getString(mCursor.getColumnIndex(DurationsContract.columns.DURATIONS_NAME));
            String description  = mCursor.getString(mCursor.getColumnIndex(DurationsContract.columns.DURATIONS_DESCRIPTION));
            long startTime = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.columns.DURATIONS_START_TIME));
            long totalDuration = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.columns.DURATIONS_DURATION));

            holder.name.setText(name);
            if (holder.description != null){       // description is not present in portrait mode
                holder.description.setText(description);
            }
            String userDate = mDateFormat.format(startTime*1000);  // the database stores seconds, we need milliseconds
            String totalTime = formatDuration(totalDuration);

            holder.startDate.setText(userDate);
            holder.duration.setText(totalTime);
        }

    }

    @Override
    public int getItemCount() {
        return (mCursor != null)? mCursor.getCount() : 0;
    }

    private String formatDuration(long duration){
        // duration is in seconds, convert to hours:minutes:seconds
        // (allowing for >24 hours  -  so we can't use a time date type )
        long hours = duration/3600;
        long remainder = duration - (hours*3600);
        long minutes = remainder/60;
        long seconds = remainder - (minutes*60);

        return String.format(Locale.US,"%02d:%02d:%02d",hours,minutes,seconds);
    }

    /**
     * Swap in a new cursor, returning the old cursor
     * The returned old cursor is <em>not</em> closed.
     *
     * @param newCursor The new cursor to be used
     * @return Return the previously set cursor, or null if there wasn't one.
     * If the new cursor is same as the previously set cursor ,then return null
     */
    Cursor swapCursor(Cursor newCursor){
        if (newCursor==mCursor){
            return null;
        }

        final Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor!=null){
            //notify the observers about the new cursor
            notifyDataSetChanged();
        }else {
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0,getItemCount());
        }
        return oldCursor;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView description;
        TextView startDate;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.td_name);
            this.description = itemView.findViewById(R.id.td_description);
            this.startDate = itemView.findViewById(R.id.td_start);
            this.duration = itemView.findViewById(R.id.td_duration);
        }
    }
}
