package com.runova.helpers;

import android.app.Activity;
import android.app.AlertDialog;

import com.runova.controllers.LevelCycleController;
import com.runova.database.DBHelper;

public class LevelUpDialog {

    public static void show(Activity activity, Runnable onChoice) {
        LevelCycleController controller = new LevelCycleController(new DBHelper(activity));
        String current = controller.getCurrentLevel();
        double passRate = controller.getPassRate();
        boolean passed = passRate >= 0.85;
        boolean isPro = current.equals("Pro");
        String next = nextLevel(current);
        int finished = (int) (passRate * 28);
        final boolean[] choiceApplied = {false};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (passed && isPro) {
            builder.setTitle("Congratulations!");
            builder.setMessage("Congratulations! You finished " + finished
                    + " of 28 days and mastered the Pro level, the highest level in RUNOVA."
                    + " There is no next level, but you're warmly welcome to keep your streak"
                    + " going and keep using the application for as long as you'd like."
                    + " Thank you for training with us!");
            builder.setPositiveButton("Continue Current Level", (dialog, which) -> {
                choiceApplied[0] = true;
                controller.applyLevelChoice(current);
                if (onChoice != null) onChoice.run();
            });
        } else if (passed) {
            builder.setTitle("Congratulations!");
            builder.setMessage("Congratulations! You finished " + finished
                    + " of 28 days this cycle - you're ready for the next level."
                    + " Move up to " + next + " now, or if you'd prefer more time,"
                    + " continue with " + current + ".");
            builder.setPositiveButton("Move to Next Level", (dialog, which) -> {
                choiceApplied[0] = true;
                controller.applyLevelChoice(next);
                if (onChoice != null) onChoice.run();
            });
            builder.setNegativeButton("Continue Current Level", (dialog, which) -> {
                choiceApplied[0] = true;
                controller.applyLevelChoice(current);
                if (onChoice != null) onChoice.run();
            });
        } else {
            builder.setTitle("Level Cycle Complete");
            builder.setMessage("You finished " + finished + " of 28 days this cycle."
                    + " You need 24 days to move up."
                    + " Continue with " + current + " to start a new cycle?");
            builder.setPositiveButton("Continue Current Level", (dialog, which) -> {
                choiceApplied[0] = true;
                controller.applyLevelChoice(current);
                if (onChoice != null) onChoice.run();
            });
        }

        builder.setOnDismissListener(dialog -> {
            if (!choiceApplied[0]) {
                String label;
                if (!passed) {
                    label = "Level cycle complete";
                } else if (isPro) {
                    label = "Congratulations! Pro level complete";
                } else {
                    label = "Congratulations! level up available";
                }
                controller.createLevelUpOffer(label);
            }
        });

        builder.show();
    }

    private static String nextLevel(String current) {
        if (current.equals("Beginner")) return "Intermediate";
        if (current.equals("Intermediate")) return "Pro";
        return "Pro";
    }
}
