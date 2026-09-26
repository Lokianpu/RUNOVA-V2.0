package com.runova.models;

public class Profile {
    public int id;
    public String firstName;
    public String lastName;
    public String dateOfBirth;
    public String gender;
    public double height;
    public double weight;
    public String event;
    public String level;
    public String goal;
    public String levelStartDate;
    public boolean seenIntro;
    public Integer activeTaskId;
    public Long activeTaskEndTime;

    public Profile() {
        this.id = 1; // Always 1 - single row
    }
}
