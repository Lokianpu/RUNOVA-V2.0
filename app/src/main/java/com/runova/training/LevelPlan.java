package com.runova.training;

import com.runova.models.DayTemplate;

public class LevelPlan {
    
    // Get template for specific level and weekday
    public static DayTemplate getTemplate(String level, int weekday) {
        switch (level) {
            case "Beginner":
                return getBeginnerTemplate(weekday);
            case "Intermediate":
                return getIntermediateTemplate(weekday);
            case "Pro":
                return getProTemplate(weekday);
            default:
                return getBeginnerTemplate(weekday);
        }
    }

    // BEGINNER WEEKLY ROUTINE
    private static DayTemplate getBeginnerTemplate(int weekday) {
        DayTemplate template = new DayTemplate();
        
        switch (weekday) {
            case 1: // Monday - 3 tasks / 60 min
                template.addTask("Warm-up", "WARMUP", 10);
                template.addTask("Easy Run", "EASY_RUN", 40);
                template.addTask("Cool-down", "COOLDOWN", 10);
                break;
                
            case 2: // Tuesday - 4 tasks / 60 min
                template.addTask("Warm-up", "WARMUP", 10);
                template.addTask("Running Drills", "DRILLS", 15);
                template.addTask("Strength & Core", "STRENGTH_CORE", 25);
                template.addTask("Cool-down", "COOLDOWN", 10);
                break;
                
            case 3: // Wednesday - 3 tasks / 60 min
                template.addTask("Warm-up", "WARMUP", 10);
                template.addTask("Cross-Training", "CROSS_TRAINING", 40);
                template.addTask("Cool-down", "COOLDOWN", 10);
                break;
                
            case 4: // Thursday - 4 tasks / 60 min
                template.addTask("Warm-up", "WARMUP", 10);
                template.addTask("Interval Run", "INTERVAL_RUN", 30);
                template.addTask("Mobility & Stretch", "MOBILITY", 10);
                template.addTask("Cool-down", "COOLDOWN", 10);
                break;
                
            case 5: // Friday - 3 tasks / 60 min
                template.addTask("Warm-up", "WARMUP", 10);
                template.addTask("Strength & Core", "STRENGTH_CORE", 40);
                template.addTask("Cool-down", "COOLDOWN", 10);
                break;
                
            case 6: // Saturday - 4 tasks / 60 min
                template.addTask("Warm-up", "WARMUP", 10);
                template.addTask("Easy Run", "EASY_RUN", 30);
                template.addTask("Running Drills", "DRILLS", 10);
                template.addTask("Cool-down", "COOLDOWN", 10);
                break;
                
            case 7: // Sunday - 2 tasks / 30 min (Rest)
                template.addTask("Easy Walk", "EASY_WALK", 15);
                template.addTask("Stretch", "STRETCH", 15);
                break;
        }
        
        return template;
    }

    // INTERMEDIATE WEEKLY ROUTINE
    private static DayTemplate getIntermediateTemplate(int weekday) {
        DayTemplate template = new DayTemplate();
        
        switch (weekday) {
            case 1: // Monday - 3 tasks / 75 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Easy Run", "EASY_RUN", 45);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 2: // Tuesday - 4 tasks / 90 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Interval Run", "INTERVAL_RUN", 30);
                template.addTask("Strength & Core", "STRENGTH_CORE", 30);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 3: // Wednesday - 5 tasks / 90 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Easy Run", "EASY_RUN", 25);
                template.addTask("Tempo Run", "TEMPO_RUN", 20);
                template.addTask("Strength & Core", "STRENGTH_CORE", 15);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 4: // Thursday - 3 tasks / 75 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Easy Run", "EASY_RUN", 45);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 5: // Friday - 4 tasks / 75 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Running Drills", "DRILLS", 10);
                template.addTask("Interval Run", "INTERVAL_RUN", 35);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 6: // Saturday - 5 tasks / 90 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Easy/Long Run", "EASY_LONG_RUN", 40);
                template.addTask("Strides", "STRIDES", 10);
                template.addTask("Mobility", "MOBILITY", 10);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 7: // Sunday - 2 tasks / 30 min (Rest)
                template.addTask("Easy Walk", "EASY_WALK", 15);
                template.addTask("Stretch", "STRETCH", 15);
                break;
        }
        
        return template;
    }

    // PRO WEEKLY ROUTINE
    private static DayTemplate getProTemplate(int weekday) {
        DayTemplate template = new DayTemplate();
        
        switch (weekday) {
            case 1: // Monday - 4 tasks / 75 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Running Drills", "DRILLS", 15);
                template.addTask("Speed Intervals", "SPEED_INTERVALS", 30);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 2: // Tuesday - 5 tasks / 120 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Easy Run", "EASY_RUN", 45);
                template.addTask("Strength & Core", "STRENGTH_CORE", 30);
                template.addTask("Mobility", "MOBILITY", 15);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 3: // Wednesday - 4 tasks / 75 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Running Drills", "DRILLS", 10);
                template.addTask("Tempo Run", "TEMPO_RUN", 35);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 4: // Thursday - 5 tasks / 105 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Easy Run", "EASY_RUN", 45);
                template.addTask("Technique", "TECHNIQUE", 15);
                template.addTask("Mobility", "MOBILITY", 15);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 5: // Friday - 4 tasks / 75 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Running Drills", "DRILLS", 10);
                template.addTask("Interval Run", "INTERVAL_RUN", 35);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 6: // Saturday - 5 tasks / 115 min
                template.addTask("Warm-up", "WARMUP", 15);
                template.addTask("Easy/Long Run", "EASY_LONG_RUN", 60);
                template.addTask("Strides", "STRIDES", 10);
                template.addTask("Mobility", "MOBILITY", 15);
                template.addTask("Cool-down", "COOLDOWN", 15);
                break;
                
            case 7: // Sunday - 2 tasks / 40 min (Rest)
                template.addTask("Easy Walk", "EASY_WALK", 20);
                template.addTask("Mobility & Stretch", "MOBILITY", 20);
                break;
        }
        
        return template;
    }
}
