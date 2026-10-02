package com.runova.training;

import com.runova.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class TrainingContentLibrary {
    
    // Get instruction for task type (with event-specific wording)
    public static String getInstruction(String taskType, String event) {
        switch (taskType) {
            case "WARMUP":
                return "Easy walk/jog, leg swings, arm circles, marching, dynamic mobility";

            case "COOLDOWN":
                return "Easy walk/jog, static stretching, breathing exercises";
            
            case "DRILLS":
                return "Marching, high knees, butt kicks, A-skips, controlled strides";
            
            case "EASY_RUN":
                return "Relaxed continuous running at comfortable effort";
            
            case "EASY_LONG_RUN":
                return "Longer easy continuous running for aerobic endurance";
            
            case "TEMPO_RUN":
                return "Controlled sustained running at stronger but manageable effort";
            
            case "INTERVAL_RUN":
                return getIntervalInstruction(event);
            
            case "SPEED_INTERVALS":
                return "Shorter faster repetitions with full recovery";
            
            case "STRENGTH_CORE":
                return "Squats, lunges, glute bridges, calf raises, plank, core work";
            
            case "CROSS_TRAINING":
                return "Brisk walking, cycling, or low-impact aerobic activity";
            
            case "MOBILITY":
                return "Light lower-body mobility and stretching";
            
            case "TECHNIQUE":
                return "Running form drills and controlled strides";
            
            case "STRIDES":
                return "Short controlled fast runs with full recovery";
            
            case "EASY_WALK":
                return "Comfortable walking for recovery";
            
            case "STRETCH":
                return "Simple recovery stretching";
            
            default:
                return "Follow task instructions";
        }
    }
    
    // Event-specific interval wording
    private static String getIntervalInstruction(String event) {
        switch (event) {
            case "Sprint":
                return "Short fast efforts with full recovery emphasis";
            
            case "Middle Distance":
                return "Speed endurance and controlled intervals";
            
            case "Long Distance":
                return "Longer controlled repetitions with endurance emphasis";
            
            case "Hurdles":
                return "Basic hurdle movement concepts plus controlled sprint work";
            
            case "Relay":
                return "Basic exchange/coordination practice concepts";
            
            case "General Running":
            default:
                return "Controlled faster/easier running repetitions";
        }
    }
    
    // Get detailed content for task expansion (for future use)
    public static String getDetailedContent(String taskType) {
        switch (taskType) {
            case "STRENGTH_CORE":
                return "• Squats (10-15 reps)\n" +
                       "• Reverse lunges (10 each leg)\n" +
                       "• Glute bridges (15 reps)\n" +
                       "• Calf raises (15 reps)\n" +
                       "• Plank (30-60 seconds)";
            
            case "DRILLS":
                return "• Marching (20m)\n" +
                       "• High knees (20m)\n" +
                       "• Butt kicks (20m)\n" +
                       "• A-skips (20m)\n" +
                       "• Controlled strides (50m)";
            
            default:
                return getInstruction(taskType, "General Running");
        }
    }

    // Exercise list per task type. seconds = 0 means the exercise spans the
    // whole task duration (caller substitutes task minutes).
    public static List<Exercise> getExercises(String taskType) {
        List<Exercise> list = new ArrayList<>();
        switch (taskType) {
            case "WARMUP":
                list.add(new Exercise("Easy Walk/Jog", "Easy walk or light jog to raise body temperature", 60));
                list.add(new Exercise("Leg Swings", "Front-to-back and side-to-side swings, each leg", 30));
                list.add(new Exercise("Arm Circles", "Small to large circles, both directions", 20));
                list.add(new Exercise("Marching in Place", "High knee march with upright posture", 30));
                list.add(new Exercise("Dynamic Mobility", "Controlled mobility moves for hips and legs", 30));
                break;
            case "COOLDOWN":
                list.add(new Exercise("Easy Walk/Jog", "Easy walk or light jog to bring heart rate down", 60));
                list.add(new Exercise("Static Stretching", "Hold each major muscle group stretch", 60));
                list.add(new Exercise("Breathing Exercises", "Slow deep breaths to recover", 30));
                break;
            case "DRILLS":
                list.add(new Exercise("Marching", "Straight-leg marching with tall posture", 30));
                list.add(new Exercise("High Knees", "Fast knee lift, quick ground contact", 30));
                list.add(new Exercise("Butt Kicks", "Heels to glutes, quick rhythm", 30));
                list.add(new Exercise("A-Skips", "Skipping with high knee drive", 30));
                list.add(new Exercise("Controlled Strides", "Relaxed form-focused strides", 40));
                break;
            case "STRENGTH_CORE":
                list.add(new Exercise("Squats", "10-15 reps, controlled form", 40));
                list.add(new Exercise("Reverse Lunges", "10 each leg", 40));
                list.add(new Exercise("Glute Bridges", "10 each leg, squeeze at top", 40));
                list.add(new Exercise("Calf Raises", "15 reps, slow and controlled", 30));
                list.add(new Exercise("Plank", "30-60 seconds hold, tight core", 30));
                break;
            case "EASY_RUN":
                list.add(new Exercise("Easy Run", "Relaxed continuous running at comfortable effort", 0));
                break;
            case "EASY_LONG_RUN":
                list.add(new Exercise("Long Run", "Longer easy continuous running for aerobic endurance", 0));
                break;
            case "TEMPO_RUN":
                list.add(new Exercise("Tempo Run", "Controlled sustained running at stronger but manageable effort", 0));
                break;
            case "INTERVAL_RUN":
                list.add(new Exercise("Interval Repetitions", "Controlled faster/easier running repetitions", 0));
                break;
            case "SPEED_INTERVALS":
                list.add(new Exercise("Speed Repetitions", "Shorter faster repetitions with full recovery", 0));
                break;
            case "CROSS_TRAINING":
                list.add(new Exercise("Cross-Training", "Brisk walking, cycling, or low-impact aerobic activity", 0));
                break;
            case "MOBILITY":
                list.add(new Exercise("Lower-Body Mobility", "Light lower-body mobility and stretching", 0));
                break;
            case "TECHNIQUE":
                list.add(new Exercise("Running Form Drills", "Running form drills and controlled strides", 0));
                break;
            case "STRIDES":
                list.add(new Exercise("Strides", "Short controlled fast runs with full recovery", 0));
                break;
            case "EASY_WALK":
                list.add(new Exercise("Easy Walk", "Comfortable walking for recovery", 0));
                break;
            case "STRETCH":
                list.add(new Exercise("Recovery Stretching", "Simple recovery stretching", 0));
                break;
            default:
                list.add(new Exercise("Follow Task Instructions", getInstruction(taskType, "General Running"), 0));
                break;
        }
        return list;
    }
}
