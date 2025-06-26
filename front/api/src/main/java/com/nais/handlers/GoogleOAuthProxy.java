package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.http.Header;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Google OAuth Proxy for private network access
 * Handles proxying OAuth requests through private VPC endpoints
 */
public class GoogleOAuthProxy {
    
    private final CloseableHttpClient httpClient;
    
    public GoogleOAuthProxy() {
        this.httpClient = HttpClients.createDefault();
    }

    public APIGatewayProxyResponseEvent handleProxy(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Handle OPTIONS requests for CORS preflight
            if ("OPTIONS".equals(input.getHttpMethod())) {
                return createOptionsResponse();
            }
            
            context.getLogger().log("Proxying request to Google OAuth");
            
            // Extract the actual path after /auth/google/proxy
            String fullPath = input.getPath();
            String proxyPath = fullPath.replace("/auth/google/proxy", "");
            if (proxyPath.isEmpty()) {
                proxyPath = "/";
            }
            
            // Build the target Google URL
            String googleUrl = "https://accounts.google.com" + proxyPath;
            
            // Add query parameters if present
            if (input.getQueryStringParameters() != null && !input.getQueryStringParameters().isEmpty()) {
                googleUrl += "?" + buildQueryString(input.getQueryStringParameters());
            }
            
            context.getLogger().log("Proxying to Google URL: " + googleUrl);
            
            // Create the appropriate HTTP request
            HttpRequestBase request = createHttpRequest(input, googleUrl);
            
            // Copy relevant headers
            copyHeaders(input, request);
            
            // Execute the request to Google
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return buildProxyResponse(response);
            }
            
        } catch (Exception e) {
            context.getLogger().log("Error in proxy handler: " + e.getMessage());
            e.printStackTrace();
            return ResponseHelper.createErrorResponse(500, "Proxy error: " + e.getMessage());
        }
    }



    private HttpRequestBase createHttpRequest(APIGatewayProxyRequestEvent input, String googleUrl) throws Exception {
        String method = input.getHttpMethod();
        
        switch (method) {
            case "GET":
                return new HttpGet(googleUrl);
            case "POST":
                HttpPost post = new HttpPost(googleUrl);
                if (input.getBody() != null) {
                    String contentType = input.getHeaders() != null ? 
                        input.getHeaders().getOrDefault("Content-Type", "application/x-www-form-urlencoded") : 
                        "application/x-www-form-urlencoded";
                    post.setEntity(new StringEntity(input.getBody(), ContentType.create(contentType)));
                }
                return post;
            default:
                throw new IllegalArgumentException("Method not allowed: " + method);
        }
    }

    private void copyHeaders(APIGatewayProxyRequestEvent input, HttpRequestBase request) {
        if (input.getHeaders() != null) {
            input.getHeaders().forEach((key, value) -> {
                String lowerKey = key.toLowerCase();
                if (!lowerKey.equals("host") && 
                    !lowerKey.equals("content-length") &&
                    !lowerKey.startsWith("x-amz") &&
                    !lowerKey.startsWith("x-forwarded") &&
                    !lowerKey.equals("cloudfront-forwarded-proto")) {
                    request.setHeader(key, value);
                }
            });
        }
    }

    private APIGatewayProxyResponseEvent buildProxyResponse(CloseableHttpResponse response) throws Exception {
        APIGatewayProxyResponseEvent proxyResponse = new APIGatewayProxyResponseEvent();
        proxyResponse.setStatusCode(response.getStatusLine().getStatusCode());
        
        // Copy response headers
        Map<String, String> responseHeaders = new HashMap<>();
        for (Header header : response.getAllHeaders()) {
            String headerName = header.getName();
            String headerValue = header.getValue();
            
            // Rewrite Location headers to use our proxy
            if (headerName.equalsIgnoreCase("Location")) {
                headerValue = rewriteGoogleUrlToProxy(headerValue);
            }
            // Rewrite Set-Cookie domain
            else if (headerName.equalsIgnoreCase("Set-Cookie")) {
                headerValue = headerValue.replace("Domain=.google.com", "Domain=.nais.internal.jp");
                headerValue = headerValue.replace("Domain=accounts.google.com", 
                    "Domain=d0kbc4lmzg-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com");
            }
            
            responseHeaders.put(headerName, headerValue);
        }
        
        // Add COMPLETE CORS headers - this was the issue!
        responseHeaders.put("Access-Control-Allow-Origin", "*");
        responseHeaders.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        responseHeaders.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        // Note: Can't use Access-Control-Allow-Credentials with wildcard origin
        
        proxyResponse.setHeaders(responseHeaders);
        
        // Handle response body
        if (response.getEntity() != null) {
            String responseBody = EntityUtils.toString(response.getEntity());
            
            // Rewrite Google URLs in HTML/JavaScript responses
            if (responseHeaders.getOrDefault("Content-Type", "").contains("text/html") ||
                responseHeaders.getOrDefault("Content-Type", "").contains("application/javascript")) {
                responseBody = rewriteGoogleUrlsInContent(responseBody);
            }
            
            proxyResponse.setBody(responseBody);
        }
        
        return proxyResponse;
    }
    

    private String rewriteGoogleUrlToProxy(String url) {
        if (url.startsWith("https://accounts.google.com")) {
            return url.replace("https://accounts.google.com", 
                "https://d0kbc4lmzg-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev/auth/google/proxy");
        }
        return url;
    }

    private String rewriteGoogleUrlsInContent(String content) {
        content = content.replace("https://accounts.google.com", 
            "https://d0kbc4lmzg-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev/auth/google/proxy");
        content = content.replace("accounts.google.com", 
            "d0kbc4lmzg-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev/auth/google/proxy");
        content = content.replace("//accounts.google.com", 
            "//d0kbc4lmzg-vpce-03e2fb9671d9d8aed.execute-api.ap-northeast-1.amazonaws.com/dev/auth/google/proxy");
        content = content.replace("\"/o/oauth2", "\"/dev/auth/google/proxy/o/oauth2");
        content = content.replace("'/o/oauth2", "'/dev/auth/google/proxy/o/oauth2");
        
        return content;
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
            .map(e -> {
                try {
                    return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                } catch (Exception ex) {
                    return e.getKey() + "=" + e.getValue();
                }
            })
            .collect(Collectors.joining("&"));
    }


    // Add this new method for handling OPTIONS in the proxy:
    private APIGatewayProxyResponseEvent createOptionsResponse() {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.put("Access-Control-Max-Age", "86400");
        
        response.setHeaders(headers);
        response.setBody("");
        
        return response;
    }
}