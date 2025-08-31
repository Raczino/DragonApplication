package com.raczkowski.app.http;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class DefaultHttpConnectionFactory implements HttpConnectionFactory {
    @Override
    public HttpURLConnection open(String urlString) throws IOException {
        URL url = new URL(urlString);
        return (HttpURLConnection) url.openConnection();
    }
}

