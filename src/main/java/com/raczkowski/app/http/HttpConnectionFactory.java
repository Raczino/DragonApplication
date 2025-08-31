package com.raczkowski.app.http;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface HttpConnectionFactory {
    HttpURLConnection open(String urlString) throws IOException;
}