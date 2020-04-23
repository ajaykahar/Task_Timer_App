package com.example.tasktimer;

import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.TaskViewHolder> {
    private static final String TAG = "CursorRecyclerViewAdapt";

    private OnTaskClickListener mListener;

    interface OnTaskClickListener{
        void onEditClick(@NonNull Task task);
        void onDeleteClick(@NonNull Task task);
        void onTaskLongClick(@NonNull Task task);
    }
    Cursor mCursor;
    public CursorRecyclerViewAdapter(Cursor cursor, OnTaskClickListener listener) {
        Log.d(TAG, "CursorRecyclerViewAdapter: constructor called");
        mCursor =cursor;
        mListener = listener;
    }

//    public void setmListener(OnTaskClickListener mListener) {
//        this.mListener = mListener;
//    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: new view requested");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_items,parent,false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: starts");
        if ((mCursor==null) || (mCursor.getCount()==0)){
            Log.d(TAG, "onBindViewHolder: providing instructions");
            holder.name.setText(R.string.instructions_heading);
            holder.description.setText(R.string.instructions);
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);

        }else{
            if (!mCursor.moveToPosition(position)){
                throw new IllegalStateException("Couldn't move cursor to position "+position);
            }
            final Task task = new Task(mCursor.getLong(mCursor.getColumnIndex(TasksContract.columns._ID)),
                    mCursor.getString(mCursor.getColumnIndex(TasksContract.columns.TASKS_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(TasksContract.columns.TASKS_DESCRIPTION)),
                    mCursor.getInt(mCursor.getColumnIndex(TasksContract.columns.TASKS_SORTORDER)));

            holder.name.setText(task.getmName());
            holder.description.setText(task.getmDescription());
            holder.editButton.setVisibility(View.VISIBLE);    //TODO add onClick listener
            holder.deleteButton.setVisibility(View.VISIBLE);  //TODO add onClick Listener

            View.OnClickListener buttonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: starts");
                    switch (v.getId()){
                        case R.id.tli_edit_button:
                            if (mListener!=null){
                            mListener.onEditClick(task);}
                            break;
                        case R.id.tli_delete_button:
                            if (mListener!=null){
                            mListener.onDeleteClick(task);}
                            break;
                        default:
                            Log.d(TAG, "onClick: found unexpected button id");
                    }
                    Log.d(TAG, "onClick: button with id "+v.getId()+" clicked.");
                    Log.d(TAG, "onClick: task name is : "+ task.getmName());
                }
            };

            View.OnLongClickListener buttonLongListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "onLongClick: starts");
                    if (mListener!=null){
                        mListener.onTaskLongClick(task);
                        return true;
                    }
                    return false;
                }
            };

            holder.editButton.setOnClickListener(buttonListener);
            holder.deleteButton.setOnClickListener(buttonListener);
            holder.itemView.setOnLongClickListener(buttonLongListener);

        }

    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: starts");
        if(mCursor==null || mCursor.getCount()==0){
            return 1;  // return 1 because when no records to display we show only one view holder showing instructions
        }else {
            return mCursor.getCount();
        }
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

    static class TaskViewHolder extends RecyclerView.ViewHolder{
        private static final String TAG = "TaskViewHolder";

        TextView name;
        TextView description;
        ImageButton editButton;
        ImageButton deleteButton;
        View itemView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "TaskViewHolder: starts");
            this.name = itemView.findViewById(R.id.tli_name);
            this.description = itemView.findViewById(R.id.tli_description);
            this.editButton = itemView.findViewById(R.id.tli_edit_button);
            this.deleteButton = itemView.findViewById(R.id.tli_delete_button);
            this.itemView = itemView;

        }
    }
}
