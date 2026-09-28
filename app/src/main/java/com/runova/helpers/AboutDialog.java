package com.runova.helpers;

import android.app.AlertDialog;
import android.content.Context;

public class AboutDialog {

    public static void show(Context ctx) {
        new AlertDialog.Builder(ctx)
                .setTitle("Before you start 👟")
                .setMessage("Hello, runner! RUNOVA is an offline training guide made for a school project. "
                        + "It gives you a daily set of tasks, but it can't track your distance, speed or heart rate. "
                        + "Nothing here is automatic.\n\n"
                        + "That means you could tap \"Done\" without doing the task, and nobody will stop you. "
                        + "But real results only come from actually doing each task, so follow the plan and give it your best. "
                        + "Listen to your body: if something hurts or you feel unwell, stop and rest. "
                        + "If you have any health concerns, talk to a doctor before you begin.\n\n"
                        + "It's your choice: the shortcut or the real thing. Good luck on your journey!")
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show();
    }
}
