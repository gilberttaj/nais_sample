package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer Master API Handler
 * メール宛先マスター管理システム (NAIS) - 得意先マスタAPI
 */
public class CustomerHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            context.getLogger().log("Processing Customer API request: " + input.getHttpMethod() + " " + input.getPath());

            // Handle CORS preflight
            if ("OPTIONS".equals(input.getHttpMethod())) {
                return createCorsResponse(200, "");
            }

            // Validate authentication
            if (!isAuthenticated(input, context)) {
                return createCorsResponse(401, "{\"error\":\"Unauthorized\",\"message\":\"Valid authentication token required\"}");
            }

            String path = input.getPath();
            String method = input.getHttpMethod();

            // Route requests
            if (path.equals("/customer") && "GET".equals(method)) {
                return handleGetAllCustomers(context);
            } else if (path.startsWith("/customer/") && "GET".equals(method)) {
                String customerCode = path.substring("/customer/".length());
                return handleGetCustomerByCode(customerCode, context);
            } else {
                return createCorsResponse(404, "{\"error\":\"Not Found\",\"message\":\"Endpoint not found\"}");
            }

        } catch (Exception e) {
            context.getLogger().log("Error in CustomerHandler: " + e.getMessage());
            e.printStackTrace();
            return createCorsResponse(500, "{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Validate authentication token
     */
    private boolean isAuthenticated(APIGatewayProxyRequestEvent input, Context context) {
        try {
            Map<String, String> headers = input.getHeaders();
            if (headers == null) {
                context.getLogger().log("No headers present");
                return false;
            }

            String authHeader = headers.get("Authorization");
            if (authHeader == null) {
                authHeader = headers.get("authorization"); // case-insensitive
            }
            if (authHeader == null) {
                authHeader = headers.get("X-Auth-Token"); // fallback for API Gateway issues
            }

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                context.getLogger().log("No valid Authorization header");
                return false;
            }

            String token = authHeader.substring("Bearer ".length());
            
            // For local development, accept mock tokens
            String environment = System.getenv("AUTH_MODE");
            if ("MOCK".equalsIgnoreCase(environment)) {
                context.getLogger().log("Mock mode: accepting token for local development");
                return token.startsWith("eyJ") || token.contains("mock") || token.contains("dummy");
            }

            // For production, validate real Cognito tokens
            return validateCognitoToken(token, context);

        } catch (Exception e) {
            context.getLogger().log("Authentication error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate Cognito JWT token (production)
     */
    private boolean validateCognitoToken(String token, Context context) {
        try {
            // Simple JWT structure validation
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                context.getLogger().log("Invalid JWT format");
                return false;
            }

            // In production, you would:
            // 1. Verify the token signature against Cognito public keys
            // 2. Check token expiration
            // 3. Validate token claims (audience, issuer, etc.)
            
            // For now, basic validation that it's a proper JWT
            context.getLogger().log("Token validation passed (simplified)");
            return true;

        } catch (Exception e) {
            context.getLogger().log("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all customers
     */
    private APIGatewayProxyResponseEvent handleGetAllCustomers(Context context) {
        try {
            context.getLogger().log("Fetching all customers");
            
            List<Map<String, Object>> customers = new ArrayList<>();
            
            try (Connection conn = getDatabaseConnection()) {
                String sql = "SELECT customer_cd, customer_name FROM customer_mst ORDER BY customer_cd";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    while (rs.next()) {
                        Map<String, Object> customer = new HashMap<>();
                        customer.put("customer_cd", rs.getString("customer_cd"));
                        customer.put("customer_name", rs.getString("customer_name"));
                        customers.add(customer);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", customers);
            response.put("count", customers.size());

            return createCorsResponse(200, buildJsonResponse(response));

        } catch (Exception e) {
            context.getLogger().log("Error fetching customers: " + e.getMessage());
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
     * Get customer by code
     */
    private APIGatewayProxyResponseEvent handleGetCustomerByCode(String customerCode, Context context) {
        try {
            context.getLogger().log("Fetching customer with code: " + customerCode);
            
            if (customerCode == null || customerCode.trim().isEmpty()) {
                return createCorsResponse(400, "{\"error\":\"Bad Request\",\"message\":\"Customer code is required\"}");
            }

            Map<String, Object> customer = null;
            
            try (Connection conn = getDatabaseConnection()) {
                String sql = "SELECT customer_cd, customer_name FROM customer_mst WHERE customer_cd = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, customerCode.toUpperCase());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            customer = new HashMap<>();
                            customer.put("customer_cd", rs.getString("customer_cd"));
                            customer.put("customer_name", rs.getString("customer_name"));
                        }
                    }
                }
            }

            if (customer == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Not Found");
                errorResponse.put("message", "Customer not found with code: " + customerCode);
                return createCorsResponse(404, buildJsonResponse(errorResponse));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", customer);

            return createCorsResponse(200, buildJsonResponse(response));

        } catch (Exception e) {
            context.getLogger().log("Error fetching customer: " + e.getMessage());
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
        
        // Default values for local development
        if (dbUrl == null) {
            dbUrl = "jdbc:postgresql://localhost:5432/nais";
        }
        if (dbUser == null) {
            dbUser = "postgres";
        }
        if (dbPassword == null) {
            dbPassword = "password";
        }

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
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
}