package com.runova.models;

public class Task {
    public int id;
    public String date;
    public String name;
    public String type;
    public int minutes;
    public String status;
    public int sortOrder;

    public Task() {
        this.status = "Pending";
    }

    public Task(String date, String name, String type, int minutes, int sortOrder) {
        this.date = date;
        this.name = name;
        this.type = type;
        this.minutes = minutes;
        this.sortOrder = sortOrder;
        this.status = "Pending";
    }
}
