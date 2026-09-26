package com.runova.helpers;

public class ValidationHelper {
    
    // Sign Up validation
    public static String validateFirstName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "First name is required";
        }
        return null;
    }

    public static String validateLastName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Last name is required";
        }
        return null;
    }

    public static String validateDateOfBirth(String dob) {
        if (dob == null || dob.isEmpty()) {
            return "Date of birth is required";
        }
        if (!DateUtils.isValidDate(dob)) {
            return "Invalid date format";
        }
        if (DateUtils.isFuture(dob)) {
            return "Date cannot be in the future";
        }
        int age = DateUtils.ageFrom(dob);
        if (age < 13 || age > 100) {
            return "Age must be between 13 and 100";
        }
        return null;
    }

    public static String validateGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            return "Gender must be selected";
        }
        return null;
    }

    // Questionnaire validation
    public static String validateHeight(String heightStr) {
        try {
            double height = Double.parseDouble(heightStr);
            if (height < 100 || height > 250) {
                return "Height must be between 100-250 cm";
            }
            return null;
        } catch (NumberFormatException e) {
            return "Invalid height value";
        }
    }

    public static String validateWeight(String weightStr) {
        try {
            double weight = Double.parseDouble(weightStr);
            if (weight < 25 || weight > 250) {
                return "Weight must be between 25-250 kg";
            }
            return null;
        } catch (NumberFormatException e) {
            return "Invalid weight value";
        }
    }

    public static String validateEvent(String event) {
        if (event == null || event.isEmpty()) {
            return "Event must be selected";
        }
        return null;
    }

    public static String validateLevel(String level) {
        if (level == null || level.isEmpty()) {
            return "Level must be selected";
        }
        return null;
    }

    public static String validateGoal(String goal) {
        if (goal == null || goal.isEmpty()) {
            return "Goal must be selected";
        }
        return null;
    }
}
