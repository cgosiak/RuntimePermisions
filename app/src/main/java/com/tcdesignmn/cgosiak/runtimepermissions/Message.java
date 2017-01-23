package com.tcdesignmn.cgosiak.runtimepermissions;

import java.util.Date;

/**
 * Created by G554146 on 1/23/2017.
 */

public class Message {
    private Contact contact;
    private Date date;
    private String body;

    public Message(Contact contact, Date date, String body) {
        this.contact = contact;
        this.date = date;
        this.body = body;
    }
}
