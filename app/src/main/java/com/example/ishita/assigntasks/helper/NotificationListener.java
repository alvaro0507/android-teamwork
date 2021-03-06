package com.example.ishita.assigntasks.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.ishita.assigntasks.AddTask;
import com.example.ishita.assigntasks.R;
import com.example.ishita.assigntasks.TeamkarmaApp;
import com.example.ishita.assigntasks.data.CommentItem;
import com.example.ishita.assigntasks.data.TaskItem;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * This service sends notifications about new tasks/comments for the user
 */
public class NotificationListener extends Service {

    String userMobile;
    static final String ADD = "add";
    static final String DELETE = "delete";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //When the service is started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Opening sharedpreferences
        PrefManager sharedPreferences = new PrefManager(this);
        userMobile = sharedPreferences.getMobileNumber();

        //Creating a firebase object
        final Firebase tasksRef = new Firebase(Config.LOGIN_REF).child(userMobile).child(Config.KEY_USER_TASKS);

        //Adding a child event listener to firebase
        //this will help us to  track the value changes on firebase
        tasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TaskItem taskItem = dataSnapshot.getValue(TaskItem.class);

                /*if the task was assigned to the user logged in and it is a new task, show a
                * notification for it and remove the flag that indicates that it's a new task */
                if (taskItem.getAssignee_id().equals(userMobile) && dataSnapshot.hasChild(Config.KEY_NOTIFY)) {
                    showNotification(taskItem.getDescription(), ADD);
                    dataSnapshot.getRef().child(Config.KEY_NOTIFY).removeValue();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                /*if a child of this task ref was changed, then it was probably because a new
                * comment was added to it.*/
                onCommentAdded(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                TaskItem taskItem = dataSnapshot.getValue(TaskItem.class);
                //Send a deletion notification to anyone who could access the task
                if (taskItem.getAssignee_id().equals(userMobile) || taskItem.getCreator_id().equals(userMobile)) {
                    showNotification(taskItem.getDescription(), DELETE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });

        return START_STICKY;
    }

    /**
     * To send notification on a new comment
     *
     * @param dataSnapshot the node on which the comment was added
     */
    private void onCommentAdded(DataSnapshot dataSnapshot) {

        final TaskItem taskItem = dataSnapshot.getValue(TaskItem.class);
        //Attach a child event listener to the snapshot so that we would know how its child changed.
        dataSnapshot.getRef().child(Config.KEY_COMMENTS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /*if added child has "notify"
                *   if sender_id != userMobile
                *       delete "notify"
                *       if CommentsActivity is not in the foreground
                *           send notification about added child*/
                CommentItem commentItem = dataSnapshot.getValue(CommentItem.class);
                if (dataSnapshot.hasChild(Config.KEY_NOTIFY)) {
                    if (!TeamkarmaApp.isCommentsActivityVisible() && !commentItem.getContact_from().equals(userMobile)) {
                        showNotification(taskItem.getDescription(), commentItem.getMsg());
                    }
                    dataSnapshot.getRef().child(Config.KEY_NOTIFY).removeValue();
                }
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
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    /**
     * To display a notification to the user
     * TODO: use inboxStyle notifications
     * @param taskName The task which was added/deleted or to which a comment was added
     * @param category whether a task was added/deleted or there was a comment
     */
    private void showNotification(String taskName, String category) {
        //Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        Intent intent = new Intent(this, AddTask.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        if (category.equals(ADD) || category.equals(DELETE))
            builder.setContentTitle("TeamKarma");
        else builder.setContentTitle(taskName);

        if (category.equals(ADD))
            builder.setContentText("You have a new task: " + taskName);
        else if (category.equals(DELETE)) {
            builder.setContentText("Your task \"" + taskName + "\" was removed.");
        } else builder.setContentText("New Comment: " + category);

        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_ALL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}