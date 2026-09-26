package com.runova.models;

public class TaskTemplate {
    public String name;
    public String type;
    public int minutes;

    public TaskTemplate(String name, String type, int minutes) {
        this.name = name;
        this.type = type;
        this.minutes = minutes;
    }
}
