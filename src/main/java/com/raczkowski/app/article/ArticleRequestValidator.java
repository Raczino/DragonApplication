package com.raczkowski.app.article;

import com.raczkowski.app.exceptions.ResponseException;

public class ArticleRequestValidator {
    private static final int TITLE_MAX_LENGTH = 250;
    private static final int CONTENT_MAX_LENGTH = 2000;
    private static final int HASHTAGS_MAX_LENGTH = 100;

    public static void validateCreationRequest(ArticleRequest request) {
        //Validation of content and title, those can't be null or empty
        if (request.getTitle() == null
                || request.getContent() == null
                || request.getTitle().equals("")
                || request.getContent().equals("")) {
            throw new ResponseException("Title or content can't be empty");
        }

        //Validation maximum length of title and content
        if (request.getTitle().length() > TITLE_MAX_LENGTH) {
            throw new ResponseException("Title is longer than maximum length " + TITLE_MAX_LENGTH);
        } else if (request.getContent().length() > CONTENT_MAX_LENGTH) {
            throw new ResponseException("Content is longer than maximum length " + CONTENT_MAX_LENGTH);
        }

        //Validation maximum length of hashtags
        if(request.getHashtags() != null){
            if(request.getHashtags().length()>HASHTAGS_MAX_LENGTH){
                throw new ResponseException("Hashtags is longer than maximum length " + HASHTAGS_MAX_LENGTH);
            }
        }
    }

    public static void validateUpdateRequest(ArticleRequest request) {
        //Validation of update, during update title or content both of them cant be empty or null
        if ((request.getTitle() == null || request.getTitle().equals("")) &&
                (request.getContent() == null || request.getContent().equals(""))) {
            throw new ResponseException("Title or content can't be empty");
        }

    }
}
