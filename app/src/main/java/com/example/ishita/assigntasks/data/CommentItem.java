package com.example.ishita.assigntasks.data;

/**
 * Created by ishita on 11/3/16.
 */
public class CommentItem {
    String contact_from;
    String msg;
    String timestamp;

    public CommentItem() {
    }

    public CommentItem(String contact_from, String msg, String timestamp) {
        this.contact_from = contact_from;
        this.msg = msg;
        this.timestamp = timestamp;
    }

    public String getContact_from() {
        return contact_from;
    }

    public String getMsg() {
        return msg;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
