package com.example.ishita.assigntasks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.data.TasksContract;
import com.example.ishita.assigntasks.data.TasksDbHelper;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommentsFragment/*.OnFragmentInteractionListener
 *//*} interface
 * to handle interaction events.
 * Use the {@link CommentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentsFragment extends Fragment /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;

    private EditText msgEdit;
    private ImageButton sendBtn;
    String taskId, taskName;
    FirebaseListAdapter/*CommentsCursorAdapter*/ adapter;
    Firebase commentsRef;
    View rootView;
    PrefManager prefManager;

    /*TasksDbHelper dbHelper;
    SQLiteDatabase readableDatabase;*/

    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(String param1, String param2) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        final Firebase tasksRef = new Firebase("https://teamkarma.firebaseio.com/tasks");
        Query queryRef = tasksRef.orderByKey().limitToLast(1);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                commentsRef = new Firebase(tasksRef.child(snapshot.getKey()).child("/comments").toString());
                taskName = snapshot.child("description").getValue().toString();
                populateView();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_comments, container, false);

        return rootView;
    }

    private void populateView() {
        msgEdit = (EditText) rootView.findViewById(R.id.frag_msg_edit);
        sendBtn = (ImageButton) rootView.findViewById(R.id.frag_send_btn);

        /*dbHelper = new TasksDbHelper(getContext());
        readableDatabase = dbHelper.getReadableDatabase();

        Cursor tempCursor = readableDatabase.query(
                TasksContract.TaskEntry.TABLE_NAME,
                new String[]{TasksContract.TaskEntry._ID, TasksContract.TaskEntry.COL_DESCRIPTION},
                null,
                null,
                null,
                null,
                TasksContract.TaskEntry._ID + " DESC"
        );
        if (tempCursor.moveToFirst()) {
            taskId = tempCursor.getString(tempCursor.getColumnIndex(TasksContract.TaskEntry._ID));
            taskName = tempCursor.getString(tempCursor.getColumnIndex(TasksContract.TaskEntry.COL_DESCRIPTION));*/
//        if (taskName != null) {
            setTaskDetails();
        /*} else {
            TextView taskDetails = (TextView) rootView.findViewById(R.id.frag_task_details);
            taskDetails.setText(R.string.no_task_details);
            taskDetails.setVisibility(View.VISIBLE);
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), R.string.empty_task_comment_error, Toast.LENGTH_SHORT).show();
                }
            });
        }*/
//        tempCursor.close();
    }

    public String formatDate(String stringDate) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        long milliseconds = Long.parseLong(stringDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    private void setTaskDetails() {
        adapter = new FirebaseListAdapter<CommentItem>(getActivity(), CommentItem.class, R.layout.comments_list_item, commentsRef) {
            @Override
            protected void populateView(View view, CommentItem commentItem, int position) {
                LinearLayout box = (LinearLayout) view.findViewById(R.id.box);
                TextView message = (TextView) view.findViewById(R.id.text1);
                LinearLayout root = (LinearLayout) view;
                TextView timeStamp = (TextView) view.findViewById(R.id.text2);
                //TODO replace NULL in this check by the sender ID once login activity is done.
                if (prefManager.getMobileNumber().equals(commentItem.getContact_from())) {
                    GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
                    sd.setColor(Color.parseColor("#FBE9E7"));
                    sd.invalidateSelf();
                    root.setGravity(Gravity.END);
                    root.setPadding(50, 10, 10, 10);
                } else {
                    GradientDrawable sd = (GradientDrawable) box.getBackground().mutate();
                    sd.setColor(Color.parseColor("#fffeee"));
                    sd.invalidateSelf();
                    root.setGravity(Gravity.START);
                    root.setPadding(10, 10, 50, 10);
                }
                message.setText(commentItem.getMsg());
                timeStamp.setText(formatDate(commentItem.getTimestamp()));
            }
        };
        /*Cursor taskCursor = readableDatabase.rawQuery(
                "SELECT " + TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID + ", " +
                        TasksContract.ProfileEntry.COL_NAME + ", " +
                        TasksContract.TaskEntry.COL_DUE_DATE +
                        " FROM " + TasksContract.TaskEntry.TABLE_NAME + ", " + TasksContract.ProfileEntry.TABLE_NAME +
                        " WHERE " + TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID + "=" + taskId + " AND " +
                        TasksContract.ProfileEntry.TABLE_NAME + "." + TasksContract.ProfileEntry.COL_CONTACT + "=" +
                        TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry.COL_ASSIGNEE_KEY,
                null
        );
        if (taskCursor.moveToFirst()) {*/
            /*String assigneeName = taskCursor.getString(taskCursor.getColumnIndex(TasksContract.ProfileEntry.COL_NAME));
            String dueDate = taskCursor.getString(taskCursor.getColumnIndex(TasksContract.TaskEntry.COL_DUE_DATE));

            TextView taskDetails = (TextView) rootView.findViewById(R.id.frag_task_details);
            taskDetails.setText("Task Name: " + taskName + "\nAssignee: " + assigneeName + "\nDue Date: " + dueDate);
*/
//            adapter = new CommentsCursorAdapter(getActivity(), null, 0);
            ListView list = (ListView) rootView.findViewById(R.id.frag_comment_list);
//            getLoaderManager().initLoader(0, null, this);

            list.setAdapter(adapter);
        list.setSelection(list.getAdapter().getCount() - 1);

            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg = msgEdit.getText().toString();
                    if (!msg.equals("")) {
                        send(msg);
                        msgEdit.setText(null);
                    }
                }
            });
    }
//        taskCursor.close();
//    }

    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                            /*ContentValues values = new ContentValues(2);
                            values.put(TasksContract.MessageEntry.COL_MSG, txt);
                            values.put(TasksContract.MessageEntry.COL_TASK_KEY, taskId);
                            Uri rowUri = getActivity().getContentResolver().insert(TasksContract.MessageEntry.CONTENT_URI, values);
                            Log.v("inserted at:", rowUri.toString());
                            Log.v("values:", txt + " " + taskId);*/

                    //TODO also put commenter contact once login activity is done.
                    Map<String, String> comment = new HashMap<>();
                    comment.put(TasksContract.MessageEntry.COL_MSG, txt);
                    comment.put(TasksContract.MessageEntry.COL_FROM, prefManager.getMobileNumber());
                    comment.put("timestamp", "" + System.currentTimeMillis());
                    commentsRef.push().setValue(comment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return msg;
                    }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
//                getLoaderManager().restartLoader(0, null, this);
            }

            /*@Override
            public void onResume () {
                super.onResume();
                if (taskId != null)
                    getLoaderManager().restartLoader(0, null, this);
            }*/

            /*@Override
            public Loader<Cursor> onCreateLoader ( int id, Bundle args){
                return new CursorLoader(
                        getContext(),
                        TasksContract.MessageEntry.CONTENT_URI,
                        new String[]{TasksContract.MessageEntry._ID,
                                TasksContract.MessageEntry.COL_TASK_KEY,
                                TasksContract.MessageEntry.COL_MSG,
                                TasksContract.MessageEntry.COL_FROM,
                                TasksContract.MessageEntry.COL_AT},
                        TasksContract.MessageEntry.COL_TASK_KEY + "=?",
                        new String[]{taskId},
                        TasksContract.MessageEntry.COL_AT + " ASC"
                );
            }*/

            /*@Override
            public void onLoadFinished (Loader < Cursor > loader, Cursor data){
//        adapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset (Loader < Cursor > loader) {
//        adapter.swapCursor(null);
            }*/

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }*/
}
