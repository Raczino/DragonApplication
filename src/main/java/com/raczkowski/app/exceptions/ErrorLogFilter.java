package com.raczkowski.app.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ErrorLogFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        chain.doFilter(wrappedRequest, response);

        if (httpResponse.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            logger.error("Error occurred while processing request: {} {}", wrappedRequest.getMethod(), wrappedRequest.getRequestURI());
            logger.error("Request Headers: {}", getHeaders(wrappedRequest));
            logger.error("Request Body: {}", new String(wrappedRequest.getContentAsByteArray()));
        }
    }

    private String getHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                headers.append(headerName).append(": ").append(request.getHeader(headerName)).append(", "));
        return headers.toString();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
