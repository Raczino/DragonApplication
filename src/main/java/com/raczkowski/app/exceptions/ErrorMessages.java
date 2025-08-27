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


    /**
     * User service
     */
    public static final String USER_NOT_EXITS = "User doesn't exists";
}
