package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer Master API Handler
 * メール宛先マスター管理システム (NAIS) - 得意先マスタAPI
 */
public class CustomerHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper;

    public CustomerHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Logging
    @Tracing
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            logInfo("Processing Customer API request: " + input.getHttpMethod() + " " + input.getPath());

            // Handle CORS preflight
            if ("OPTIONS".equals(input.getHttpMethod())) {
                return createCorsResponse(200, "");
            }

            // Validate authentication
            if (!isAuthenticated(input)) {
                logInfo("Authentication failed for request");
                return createCorsResponse(401, "{\"error\":\"Unauthorized\",\"message\":\"Valid authentication token required\"}");
            }

            String path = input.getPath();
            String method = input.getHttpMethod();

            // Route requests
            if (path.equals("/customer") && "GET".equals(method)) {
                return handleGetAllCustomers();
            } else if (path.startsWith("/customer/") && "GET".equals(method)) {
                String customerCode = path.substring("/customer/".length());
                return handleGetCustomerByCode(customerCode);
            } else {
                logInfo("Endpoint not found: " + method + " " + path);
                return createCorsResponse(404, "{\"error\":\"Not Found\",\"message\":\"Endpoint not found\"}");
            }

        } catch (Exception e) {
            logError("Error in CustomerHandler", e);
            return createCorsResponse(500, "{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Validate authentication token
     */
    private boolean isAuthenticated(APIGatewayProxyRequestEvent input) {
        try {
            Map<String, String> headers = input.getHeaders();
            if (headers == null) {
                logInfo("No headers present");
                return false;
            }

            String authHeader = headers.get("Authorization");
            if (authHeader == null) {
                authHeader = headers.get("authorization");
            }
            
            String xAuthToken = headers.get("X-Auth-Token");
            if (xAuthToken == null) {
                xAuthToken = headers.get("x-auth-token");
            }

            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring("Bearer ".length());
            } else if (xAuthToken != null) {
                token = xAuthToken;
            }
            
            if (token == null) {
                logInfo("No valid Authorization header or X-Auth-Token header");
                return false;
            }
            
            // For local development, accept mock tokens
            String environment = System.getenv("AUTH_MODE");
            if ("MOCK".equalsIgnoreCase(environment)) {
                logInfo("Mock mode: accepting token for local development");
                return token.startsWith("eyJ") || token.contains("mock") || token.contains("dummy");
            }

            // For production, validate real Cognito tokens
            return validateCognitoToken(token);

        } catch (Exception e) {
            logError("Authentication error", e);
            return false;
        }
    }

    /**
     * Validate Cognito JWT token (production)
     */
    private boolean validateCognitoToken(String token) {
        try {
            // Simple JWT structure validation
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                logInfo("Invalid JWT format");
                return false;
            }

            // In production, you would:
            // 1. Verify the token signature against Cognito public keys
            // 2. Check token expiration
            // 3. Validate token claims (audience, issuer, etc.)
            
            // For now, basic validation that it's a proper JWT
            logInfo("Token validation passed (simplified)");
            return true;

        } catch (Exception e) {
            logError("Token validation failed", e);
            return false;
        }
    }

    /**
     * Get all customers
     */
    private APIGatewayProxyResponseEvent handleGetAllCustomers() {
        try {
            logInfo("Fetching all customers");
            
            List<Map<String, Object>> customers = new ArrayList<>();
            
            try (Connection conn = getDatabaseConnection()) {
                String sql = "SELECT office_cd, customer_cd, normal_name_kanji, chain_store_cd, chain_store_subcd, " +
                            "created_by, created_at, updated_by, updated_at FROM customer_mst ORDER BY office_cd, customer_cd";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    while (rs.next()) {
                        Map<String, Object> customer = new HashMap<>();
                        customer.put("office_cd", rs.getString("office_cd"));
                        customer.put("customer_cd", rs.getString("customer_cd"));
                        customer.put("normal_name_kanji", rs.getString("normal_name_kanji"));
                        customer.put("chain_store_cd", rs.getString("chain_store_cd"));
                        customer.put("chain_store_subcd", rs.getString("chain_store_subcd"));
                        customer.put("created_by", rs.getString("created_by"));
                        customer.put("created_at", rs.getTimestamp("created_at"));
                        customer.put("updated_by", rs.getString("updated_by"));
                        customer.put("updated_at", rs.getTimestamp("updated_at"));
                        customers.add(customer);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", customers);
            response.put("count", customers.size());

            logInfo("Successfully fetched " + customers.size() + " customers");
            return createCorsResponse(200, buildJsonResponse(response));

        } catch (Exception e) {
            logError("Error fetching customers", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Database Error");
            errorResponse.put("message", e.getMessage());
            
            try {
                return createCorsResponse(500, buildJsonResponse(errorResponse));
            } catch (Exception jsonError) {
                return createCorsResponse(500, "{\"error\":\"Database Error\"}");
            }
        }
    }

    /**
     * Get customer by code (expects office_cd-customer_cd format like "0001-0001")
     */
    private APIGatewayProxyResponseEvent handleGetCustomerByCode(String customerCode) {
        try {
            logInfo("Fetching customer with code: " + customerCode);
            
            if (customerCode == null || customerCode.trim().isEmpty()) {
                return createCorsResponse(400, "{\"error\":\"Bad Request\",\"message\":\"Customer code is required\"}");
            }

            // Parse office_cd and customer_cd from the code (format: office_cd-customer_cd)
            String[] parts = customerCode.split("-");
            String officeCd, custCd;
            
            if (parts.length == 2) {
                // Format: "0001-0002"
                officeCd = parts[0];
                custCd = parts[1];
            } else {
                // Fallback: treat as customer_cd only, search all offices
                officeCd = null;
                custCd = customerCode;
            }

            Map<String, Object> customer = null;
            
            try (Connection conn = getDatabaseConnection()) {
                String sql;
                if (officeCd != null) {
                    // Search by specific office_cd and customer_cd
                    sql = "SELECT office_cd, customer_cd, normal_name_kanji, chain_store_cd, chain_store_subcd, " +
                          "created_by, created_at, updated_by, updated_at FROM customer_mst " +
                          "WHERE office_cd = ? AND customer_cd = ?";
                } else {
                    // Search by customer_cd only (first match)
                    sql = "SELECT office_cd, customer_cd, normal_name_kanji, chain_store_cd, chain_store_subcd, " +
                          "created_by, created_at, updated_by, updated_at FROM customer_mst " +
                          "WHERE customer_cd = ? LIMIT 1";
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (officeCd != null) {
                        stmt.setString(1, officeCd.toUpperCase());
                        stmt.setString(2, custCd.toUpperCase());
                    } else {
                        stmt.setString(1, custCd.toUpperCase());
                    }
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            customer = new HashMap<>();
                            customer.put("office_cd", rs.getString("office_cd"));
                            customer.put("customer_cd", rs.getString("customer_cd"));
                            customer.put("normal_name_kanji", rs.getString("normal_name_kanji"));
                            customer.put("chain_store_cd", rs.getString("chain_store_cd"));
                            customer.put("chain_store_subcd", rs.getString("chain_store_subcd"));
                            customer.put("created_by", rs.getString("created_by"));
                            customer.put("created_at", rs.getTimestamp("created_at"));
                            customer.put("updated_by", rs.getString("updated_by"));
                            customer.put("updated_at", rs.getTimestamp("updated_at"));
                        }
                    }
                }
            }

            if (customer == null) {
                logInfo("Customer not found with code: " + customerCode);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Not Found");
                errorResponse.put("message", "Customer not found with code: " + customerCode);
                return createCorsResponse(404, buildJsonResponse(errorResponse));
            }

            logInfo("Successfully fetched customer with code: " + customerCode);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", customer);

            return createCorsResponse(200, buildJsonResponse(response));

        } catch (Exception e) {
            logError("Error fetching customer with code: " + customerCode, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Database Error");
            errorResponse.put("message", e.getMessage());
            
            try {
                return createCorsResponse(500, buildJsonResponse(errorResponse));
            } catch (Exception jsonError) {
                return createCorsResponse(500, "{\"error\":\"Database Error\"}");
            }
        }
    }

    /**
     * Get database connection
     */
    private Connection getDatabaseConnection() throws Exception {
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        // Log environment variables for debugging (mask password)
        logInfo("DB_URL from env: " + dbUrl);
        logInfo("DB_USER from env: " + dbUser);
        logInfo("DB_PASSWORD from env: " + (dbPassword != null ? "***set***" : "null"));
        
        // Default values for local development
        if (dbUrl == null) {
            dbUrl = "jdbc:postgresql://localhost:5432/nais";
            logInfo("Using default DB_URL: " + dbUrl);
        }
        if (dbUser == null) {
            dbUser = "postgres";
            logInfo("Using default DB_USER: " + dbUser);
        }
        if (dbPassword == null) {
            dbPassword = "password";
            logInfo("Using default DB_PASSWORD");
        }

        logInfo("Attempting to connect to database: " + dbUrl + " with user: " + dbUser);
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        logInfo("Successfully connected to database");
        return conn;
    }

    /**
     * Build JSON response string from Map
     */
    private String buildJsonResponse(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(((String) value).replace("\"", "\\\"")).append("\"");
            } else if (value instanceof Number) {
                json.append(value.toString());
            } else if (value instanceof List) {
                json.append(buildJsonArray((List<?>) value));
            } else if (value instanceof Map) {
                json.append(buildJsonResponse((Map<String, Object>) value));
            } else {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            }
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Build JSON array string from List
     */
    private String buildJsonArray(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : list) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            if (item == null) {
                json.append("null");
            } else if (item instanceof String) {
                json.append("\"").append(((String) item).replace("\"", "\\\"")).append("\"");
            } else if (item instanceof Number) {
                json.append(item.toString());
            } else if (item instanceof Map) {
                json.append(buildJsonResponse((Map<String, Object>) item));
            } else {
                json.append("\"").append(item.toString().replace("\"", "\\\"")).append("\"");
            }
        }
        
        json.append("]");
        return json.toString();
    }

    /**
     * Create CORS-enabled response
     */
    private APIGatewayProxyResponseEvent createCorsResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        
        response.setBody(body);
        return response;
    }

    /**
     * Structured logging methods using AWS Lambda Powertools format
     */
    private void logInfo(String message) {
        System.out.println(createLogJson("INFO", message, null));
    }

    private void logError(String message, Exception e) {
        System.out.println(createLogJson("ERROR", message, e));
    }

    private String createLogJson(String level, String message, Exception e) {
        try {
            Map<String, Object> logData = Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "level", level,
                    "message", message,
                    "service", "customer-api",
                    "error", e != null ? e.getMessage() : null
            );
            return objectMapper.writeValueAsString(logData);
        } catch (Exception ex) {
            return "{\"level\":\"" + level + "\",\"message\":\"" + message + "\",\"service\":\"customer-api\",\"error\":\"" + (e != null ? e.getMessage() : "") + "\"}";
        }
    }
}