package com.raczkowski.app.exceptions;

public final class ErrorMessages {
    private ErrorMessages() {
    }

    /**
     * Plan price change validator
     */
    public static final String SUBSCRIPTION_PLAN_REQUIRED = "Subscription plan is required";
    public static final String AMOUNT_TOO_LOW = "Amount must be > 0";
    public static final String AMOUNT_TOO_HIGH = "Amount must be <= 100000";
    public static final String CURRENCY_REQUIRED = "Currency is required";
    public static final String UNSUPPORTED_CURRENCY = "Unsupported currency";


    /**
     * Permissions validator
     */
    public static final String WRONG_PERMISSION = "You don't have permissions to do this action";
    public static final String NO_PERMISSION = "No permission";

    /**
     * User
     */
    public static final String INVALID_EMAIL = "Invalid Email";
    public static final String INVALID_PASSWORD = "Invalid Password";

    /**
     * User service
     */
    public static final String USER_NOT_EXITS = "User doesn't exists";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXITS = "User already exists";

    /**
     * Follows
     */
    public static final String CANNOT_FOLLOW_YOURSELF = "You cannot follow yourself.";

    /**
     * Subsciptions
     */
    public static final String USER_HAS_SUBSCRIPTION = "User already has an active subscription";
    public static final String USER_HAS_NO_SUBSCRIPTION = "User has not a subscription";
    public static final String SUBSCRIPTION_NOT_FOUND = "Subscription plan not found";
    public static final String PLAN_ALREADY_ACTIVATED = "Plan already activated";

    /**
     * Admin
     */
    public static final String SETTING_NOT_FOUND = "Setting not found";

    /**
     * Articles
     */
    public static final String ARTICLE_ID_NOT_EXISTS = "Article with provided id doesn't exist";
    public static final String ARTICLE_NOT_EXISTS = "Article not exists";

    /**
     * Comments
     */
    public static final String COMMENT_NOT_EXISTS = "Comment doesn't exists";

    /**
     * Surveys
     */
    public static final String SURVEY_NOT_FOUND = "Survey not found";
    public static final String END_DATE_MUST_BE_IN_THE_FUTURE = "End time must be in the future.";
    public static final String SURVEY_MUST_HAS_QUESTION = "Survey must have at least one question.";
    public static final String TO_MANY_QUESTIONS = "To many questions of max: ";
    public static final String TO_MANY_ANSWERS = "To many answers of max: ";
    public static final String SURVEY_OR_USER_NOT_FOUND = "Survey or user not found";
    public static final String USER_ALREADY_ANSWERED = "User has already answered this question.";

    /**
     * Common
     */
    public static final String REQUIRED_TITLE_AND_DESCRIPTION = "Title and description is required";
}
