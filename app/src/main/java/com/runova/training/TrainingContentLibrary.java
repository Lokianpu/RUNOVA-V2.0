package com.runova.training;

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
}
