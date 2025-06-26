package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced ResponseHelper with additional methods for workspace authentication
 */
public class ResponseHelper {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static APIGatewayProxyResponseEvent createSuccessResponse(Object data) throws Exception {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setHeaders(createCorsHeaders());
        response.setBody(objectMapper.writeValueAsString(data));
        return response;
    }
    
    public static APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setHeaders(createCorsHeaders());
        
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", message);
        errorBody.put("statusCode", statusCode);
        errorBody.put("timestamp", System.currentTimeMillis());
        
        try {
            response.setBody(objectMapper.writeValueAsString(errorBody));
        } catch (Exception e) {
            response.setBody("{\"error\":\"Failed to serialize error response\"}");
        }
        
        return response;
    }
    
    public static APIGatewayProxyResponseEvent createCustomResponse(int statusCode, Object data) throws Exception {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setHeaders(createCorsHeaders());
        
        if (data != null) {
            response.setBody(objectMapper.writeValueAsString(data));
        } else {
            response.setBody("");
        }
        
        return response;
    }
    
    public static APIGatewayProxyResponseEvent createTokenResponse(Object authenticationResult) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("authenticationResult", authenticationResult);
        response.put("message", "Authentication successful");
        response.put("timestamp", System.currentTimeMillis());
        
        return createSuccessResponse(response);
    }
    
    public static APIGatewayProxyResponseEvent createSuccessPageWithTokens(Object authenticationResult, String email) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setHeaders(createHtmlHeaders());
        
        String htmlContent = createSuccessHtmlPage(authenticationResult, email);
        response.setBody(htmlContent);
        
        return response;
    }
    
    /**
     * Create an access denied page for workspace authentication failures
     */
    public static APIGatewayProxyResponseEvent createAccessDeniedPage(String reason) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(403);
        response.setHeaders(createHtmlHeaders());
        
        String htmlContent = createAccessDeniedHtmlPage(reason);
        response.setBody(htmlContent);
        
        return response;
    }
    
    private static Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.put("Content-Type", "application/json");
        return headers;
    }
    
    private static Map<String, String> createHtmlHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Content-Type", "text/html");
        return headers;
    }
    
    private static String createSuccessHtmlPage(Object authenticationResult, String email) {
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "    <title>Authentication Successful</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }\n" +
               "        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
               "        .success { color: #28a745; }\n" +
               "        .info { background-color: #e9ecef; padding: 15px; border-radius: 4px; margin: 15px 0; }\n" +
               "        .email { font-weight: bold; color: #007bff; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <h1 class=\"success\">✅ Authentication Successful</h1>\n" +
               "        <p>You have successfully authenticated with your Google Workspace account.</p>\n" +
               "        <div class=\"info\">\n" +
               "            <p><strong>Email:</strong> <span class=\"email\">" + maskEmailForDisplay(email) + "</span></p>\n" +
               "            <p><strong>Timestamp:</strong> " + new java.util.Date() + "</p>\n" +
               "            <p><strong>Status:</strong> Workspace domain validated</p>\n" +
               "        </div>\n" +
               "        <p>You can now close this window and return to the application.</p>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    private static String createAccessDeniedHtmlPage(String reason) {
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "    <title>Access Denied</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }\n" +
               "        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
               "        .error { color: #dc3545; }\n" +
               "        .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 4px; margin: 15px 0; }\n" +
               "        .contact { background-color: #e9ecef; padding: 15px; border-radius: 4px; margin: 15px 0; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <h1 class=\"error\">🚫 Access Denied</h1>\n" +
               "        <div class=\"warning\">\n" +
               "            <p><strong>Reason:</strong> " + reason + "</p>\n" +
               "        </div>\n" +
               "        <p>You do not have permission to access this application. Only users from approved Google Workspace domains are allowed to authenticate.</p>\n" +
               "        <div class=\"contact\">\n" +
               "            <p><strong>Need access?</strong> Please contact your system administrator or IT department to request access.</p>\n" +
               "            <p><strong>Timestamp:</strong> " + new java.util.Date() + "</p>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * Mask email for display purposes (show only first 2 chars and domain)
     */
    private static String maskEmailForDisplay(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            return email.substring(0, 2) + "***";
        }
        
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (localPart.length() <= 2) {
            return localPart + "***" + domain;
        }
        
        return localPart.substring(0, 2) + "***" + domain;
    }
}