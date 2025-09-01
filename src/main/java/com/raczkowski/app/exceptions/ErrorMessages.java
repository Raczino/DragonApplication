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
    public static final String PLAN_NOT_FOUND = "Plan not found";

    /**
     * Permissions validator
     */
    public static final String WRONG_PERMISSION = "You don't have permissions to do this action";
    public static final String NO_PERMISSION = "No permission";

    /**
     * User
     */
    public static final String INVALID_EMAIL = "Invalid Email";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials";
    public static final String EMAIL_NOT_EXISTS = "User with this email doesn't exists";
    public static final String INVALID_PASSWORD = "Invalid Password";
    public static final String EMAIL_AND_PASSWORD_CANNOT_BE_NULL = "Email and password can't be null";

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
     * Subscription
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
    public static final String ARTICLE_CANNOT_BE_NULL = "Article cannot be null";
    public static final String ARTICLE_NOT_EXISTS = "Article not exists.";
    public static final String TITLE_AND_CONTENT_CANNOT_BE_EMPTY = "Title or content can't be empty.";
    public static final String CONTENT_IS_TOO_SHORT = "Content is shorter than minimum length";
    public static final String SCHEDULED_FOR_MUST_BE_IN_FUTURE = "Scheduled for cannot be before now!";

    /**
     * Hashtags
     */
    public static final String HASHTAG_LENGTH_IS_TOO_LONG = "Hashtags is longer than maximum length ";

    /**
     * Comments
     */
    public static final String COMMENT_NOT_EXISTS = "Comment doesn't exists";
    public static final String COMMENT_CANNOT_BE_NULL = "Comment cannot be null";
    public static final String COMMENT_CANT_BE_EMPTY = "Comment can't be empty";
    public static final String COMMENT_ID_NOT_FOUND = "There is no comment with provided id:";
    public static final String COMMENT_TOO_LONG = "Comment content length is longer than maximum length";
    public static final String COMMENT_CONTAINS_BANNED_WORDS = "Comment contains banned words.";

    /**
     * Surveys
     */
    public static final String SURVEY_NOT_FOUND = "Survey not found";
    public static final String TITLE_AND_DESCRIPTION_TOO_SHORT = "Title or description is lower than min length: ";
    public static final String TITLE_AND_DESCRIPTION_TOO_LONG = "Title or description is longer than maximum length: ";
    public static final String END_DATE_MUST_BE_IN_THE_FUTURE = "End time must be in the future.";
    public static final String SURVEY_MUST_HAS_QUESTION = "Survey must have at least one questiaon.";
    public static final String TO_MANY_QUESTIONS = "To many questions of max: ";
    public static final String TO_MANY_ANSWERS = "To many answers of max: ";
    public static final String SURVEY_OR_USER_NOT_FOUND = "Survey or user not found";
    public static final String USER_ALREADY_ANSWERED = "User has already answered this question.";
    public static final String SURVEY_ENDED = "Survey already ended.";

    /**
     * Surveys questions
     */
    public static final String QUESTION_VALUE_IS_NULL = "Question value cannot be null";
    public static final String QUESTION_TYPE_IS_REQUIRED = "Question type is required.";
    public static final String QUESTION_VALUE_TOO_SHORT = "Question value must be at least: ";
    public static final String QUESTION_VALUE_TOO_LONG = "Question is longer than maximum length: ";

    /**
     * Surveys question answers
     */
    public static final String MIN_SELECTED_TOO_LOW = "Min selected cannot be lower than 1.";
    public static final String QUESTION_ANSWER_REQUIRED = "Question must have at least one answer.";
    public static final String ANSWER_MUST_BE_UNIQUE = "Response must be unique. Duplicated found";
    public static final String ANSWER_VALUE_IS_REQUIRED = "Answer value is required";
    public static final String ANSWER_VALUE_TOO_LONG = "Answer value is bigger than maximum length: ";
    public static final String ANSWER_IS_REQUIRED = "Answer is required for this question.";
    public static final String ANSWER_MUST_BE_BETWEEN = "Number of selected answers must be between: ";
    public static final String INVALID_ANSWER_VALUE = "Invalid answer value: ";

    /**
     * Common
     */
    public static final String REQUIRED_TITLE_AND_DESCRIPTION = "Title and description is required";

    /**
     * Limits
     */
    public static final String LIMIT_REACHED = "You have reached the weekly limit. If you need more actions buy premium account.";
}
