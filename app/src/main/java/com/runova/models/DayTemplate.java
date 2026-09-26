package com.runova.models;

import java.util.ArrayList;
import java.util.List;

public class DayTemplate {
    public List<TaskTemplate> tasks;

    public DayTemplate() {
        this.tasks = new ArrayList<>();
    }

    public void addTask(String name, String type, int minutes) {
        tasks.add(new TaskTemplate(name, type, minutes));
    }
}
