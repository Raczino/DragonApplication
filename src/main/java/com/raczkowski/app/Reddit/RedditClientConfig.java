package com.raczkowski.app.Reddit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reddit")
@Getter
@Setter
public class RedditClientConfig {

    private Client client = new Client();
    private User user = new User();

    @Getter
    @Setter
    public static class Client {
        private String id;
        private String secret;
    }

    @Getter
    @Setter
    public static class User {
        private String agent;
        private String subreddit;
    }
}
