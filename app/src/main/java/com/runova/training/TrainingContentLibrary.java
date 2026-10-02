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
    // Durations follow the purpose of each movement: warm-up ramps up gradually,
    // cooldown stays relaxed, drills stay short and focused, strength work
    // matches a realistic set duration for a beginner.
    public static List<Exercise> getExercises(String taskType) {
        List<Exercise> list = new ArrayList<>();
        switch (taskType) {
            case "WARMUP":
                // 2.5 min to raise muscle temperature before mobility work
                list.add(new Exercise("Easy Walk/Jog", "Easy walk or light jog to raise body temperature", 150));
                // both legs, front-to-back and side-to-side
                list.add(new Exercise("Leg Swings", "Front-to-back and side-to-side swings, each leg", 45));
                // light movement, quick to do
                list.add(new Exercise("Arm Circles", "Small to large circles, both directions", 30));
                // rhythm and posture before running
                list.add(new Exercise("Marching in Place", "High knee march with upright posture", 60));
                // hips and legs need time to open up
                list.add(new Exercise("Dynamic Mobility", "Controlled mobility moves for hips and legs", 60));
                break;
            case "COOLDOWN":
                // gradual heart-rate drop
                list.add(new Exercise("Easy Walk/Jog", "Easy walk or light jog to bring heart rate down", 150));
                // 30-45 s hold per major muscle group
                list.add(new Exercise("Static Stretching", "Hold each major muscle group stretch 30-45 seconds, never to the point of pain", 240));
                // slow breathing to finish recovery
                list.add(new Exercise("Breathing Exercises", "Slow deep breaths to recover", 90));
                break;
            case "DRILLS":
                // short quality repetitions, not endurance work
                list.add(new Exercise("Marching", "2-3 repetitions of 20 m, tall posture, brief walk back", 45));
                // high intensity, keep it short
                list.add(new Exercise("High Knees", "2-3 repetitions of 20 m, fast knee lift and quick ground contact", 30));
                // high intensity, keep it short
                list.add(new Exercise("Butt Kicks", "2-3 repetitions of 20 m, heels to glutes, quick rhythm", 30));
                // coordination needs rhythm time
                list.add(new Exercise("A-Skips", "2-3 repetitions of 20 m, skipping with high knee drive", 45));
                // form focus with low fatigue
                list.add(new Exercise("Controlled Strides", "2-3 relaxed strides of 30-50 m, smooth form", 45));
                break;
            case "STRENGTH_CORE":
                // timer covers one realistic set; reps live in the description
                list.add(new Exercise("Squats", "12-15 controlled repetitions, knees tracking over toes", 60));
                list.add(new Exercise("Reverse Lunges", "10 repetitions each leg, upright torso", 60));
                list.add(new Exercise("Glute Bridges", "12-15 repetitions, squeeze at the top for 2 seconds", 50));
                list.add(new Exercise("Calf Raises", "15-20 repetitions, slow and controlled", 45));
                // beginner-safe hold: 30-60 s inside a 60 s timer
                list.add(new Exercise("Plank", "Hold 30-60 seconds, tight core, steady breathing", 60));
                break;
            case "EASY_RUN":
                // 0 = whole task; description guides the effort
                list.add(new Exercise("Easy Run", "Run continuously at a conversational pace the whole time - you should be able to talk comfortably", 0));
                break;
            case "EASY_LONG_RUN":
                list.add(new Exercise("Long Run", "Steady easy running for aerobic endurance - keep effort relaxed and consistent from start to finish", 0));
                break;
            case "TEMPO_RUN":
                list.add(new Exercise("Tempo Run", "Sustained comfortably-hard effort - a steady strong pace you could hold for the whole session", 0));
                break;
            case "INTERVAL_RUN":
                // work/recovery structure for the whole session
                list.add(new Exercise("Interval Repetitions", "Run faster than easy pace for 30-60 seconds, then recover with easy jogging for 60-120 seconds. Repeat across the session, keeping form strong", 0));
                break;
            case "SPEED_INTERVALS":
                list.add(new Exercise("Speed Repetitions", "Short fast efforts of 15-30 seconds near maximum speed, with 60-90 seconds easy walk or jog recovery between each. Quality over speed", 0));
                break;
            case "CROSS_TRAINING":
                list.add(new Exercise("Cross-Training", "Choose low-impact cardio - brisk walking, cycling, or swimming - at steady moderate effort for the full duration", 0));
                break;
            case "MOBILITY":
                list.add(new Exercise("Lower-Body Mobility", "Move through hip, ankle, and leg mobility drills slowly and controlled for the full duration - quality over speed", 0));
                break;
            case "TECHNIQUE":
                list.add(new Exercise("Running Form Drills", "Practice running form drills - A-skips, high knees, posture checks - then controlled strides, focusing on form the whole session", 0));
                break;
            case "STRIDES":
                list.add(new Exercise("Strides", "Run 4-6 controlled fast strides of 20-30 seconds with full walk-back recovery between each", 0));
                break;
            case "EASY_WALK":
                list.add(new Exercise("Easy Walk", "Walk at a comfortable recovery pace for the full duration - relaxed breathing and posture", 0));
                break;
            case "STRETCH":
                list.add(new Exercise("Recovery Stretching", "Hold each major muscle group stretch for 30-45 seconds - gentle tension, never pain - for the full duration", 0));
                break;
            default:
                list.add(new Exercise("Follow Task Instructions", getInstruction(taskType, "General Running"), 0));
                break;
        }
        return list;
    }
}
