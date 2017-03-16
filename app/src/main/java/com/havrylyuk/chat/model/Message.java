package com.havrylyuk.chat.model;

import java.util.Date;

/**
 *
 * Created by Igor Havrylyuk on 15.03.2017.
 */
public class Message {

    private int id;
    private Date date;
    private String userName;
    private String message;

    public Message(String userName, String message) {
        this(0, userName, message);
    }

    public Message(int id, String userName, String message) {
        this.id = id;
        this.date = new Date();
        this.userName = userName;
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public String getUserName() {
        return userName;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
