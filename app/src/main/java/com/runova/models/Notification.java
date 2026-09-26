package com.runova.models;

public class Notification {
    public int id;
    public String type;
    public String message;
    public long created;
    public boolean resolved;

    public Notification() {
        this.resolved = false;
    }
}
