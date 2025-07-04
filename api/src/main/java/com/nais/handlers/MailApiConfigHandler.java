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
 * Mail API Config Master API Handler
 * メール宛先マスター管理システム (NAIS) - メールAPI連携設定マスタAPI
 */
public class MailApiConfigHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            context.getLogger().log("Processing Mail API Config request: " + input.getHttpMethod() + " " + input.getPath());

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
            if (path.equals("/mail-api-config") && "GET".equals(method)) {
                return handleGetAllMailApiConfig(context);
            } else if (path.startsWith("/mail-api-config/") && "GET".equals(method)) {
                String jobId = path.substring("/mail-api-config/".length());
                return handleGetMailApiConfigByJobId(jobId, context);
            } else {
                return createCorsResponse(404, "{\"error\":\"Not Found\",\"message\":\"Endpoint not found\"}");
            }

        } catch (Exception e) {
            context.getLogger().log("Error in MailApiConfigHandler: " + e.getMessage());
            e.printStackTrace();
            return createCorsResponse(500, "{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Validate authentication token (same as CustomerHandler)
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
                context.getLogger().log("No valid Authorization header or X-Auth-Token header");
                return false;
            }
            
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
     * Get all mail API config records
     */
    private APIGatewayProxyResponseEvent handleGetAllMailApiConfig(Context context) {
        try {
            context.getLogger().log("Fetching all mail API config records");
            
            List<Map<String, Object>> records = new ArrayList<>();
            
            try (Connection conn = getDatabaseConnection()) {
                String sql = "SELECT job_id, job_name, send_mode, search_directory, send_directory, " +
                           "subject, body_file_path, update_sys_div, created_by, created_at, updated_by, updated_at " +
                           "FROM mail_api_config_mst ORDER BY job_id";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    while (rs.next()) {
                        Map<String, Object> record = new HashMap<>();
                        record.put("job_id", rs.getString("job_id"));
                        record.put("job_name", rs.getString("job_name"));
                        record.put("send_mode", rs.getString("send_mode"));
                        record.put("search_directory", rs.getString("search_directory"));
                        record.put("send_directory", rs.getString("send_directory"));
                        record.put("subject", rs.getString("subject"));
                        record.put("body_file_path", rs.getString("body_file_path"));
                        record.put("update_sys_div", rs.getString("update_sys_div"));
                        record.put("created_by", rs.getString("created_by"));
                        record.put("created_at", rs.getTimestamp("created_at"));
                        record.put("updated_by", rs.getString("updated_by"));
                        record.put("updated_at", rs.getTimestamp("updated_at"));
                        records.add(record);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", records);
            response.put("count", records.size());

            return createCorsResponse(200, buildJsonResponse(response));

        } catch (Exception e) {
            context.getLogger().log("Error fetching mail API config records: " + e.getMessage());
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
     * Get mail API config record by job ID
     */
    private APIGatewayProxyResponseEvent handleGetMailApiConfigByJobId(String jobId, Context context) {
        try {
            context.getLogger().log("Fetching mail API config for job ID: " + jobId);
            
            if (jobId == null || jobId.trim().isEmpty()) {
                return createCorsResponse(400, "{\"error\":\"Bad Request\",\"message\":\"Job ID is required\"}");
            }

            Map<String, Object> record = null;
            
            try (Connection conn = getDatabaseConnection()) {
                String sql = "SELECT job_id, job_name, send_mode, search_directory, send_directory, " +
                           "subject, body_file_path, update_sys_div, created_by, created_at, updated_by, updated_at " +
                           "FROM mail_api_config_mst WHERE job_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, jobId.toUpperCase());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            record = new HashMap<>();
                            record.put("job_id", rs.getString("job_id"));
                            record.put("job_name", rs.getString("job_name"));
                            record.put("send_mode", rs.getString("send_mode"));
                            record.put("search_directory", rs.getString("search_directory"));
                            record.put("send_directory", rs.getString("send_directory"));
                            record.put("subject", rs.getString("subject"));
                            record.put("body_file_path", rs.getString("body_file_path"));
                            record.put("update_sys_div", rs.getString("update_sys_div"));
                            record.put("created_by", rs.getString("created_by"));
                            record.put("created_at", rs.getTimestamp("created_at"));
                            record.put("updated_by", rs.getString("updated_by"));
                            record.put("updated_at", rs.getTimestamp("updated_at"));
                        }
                    }
                }
            }

            if (record == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Not Found");
                errorResponse.put("message", "Mail API config not found with job ID: " + jobId);
                return createCorsResponse(404, buildJsonResponse(errorResponse));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", record);

            return createCorsResponse(200, buildJsonResponse(response));

        } catch (Exception e) {
            context.getLogger().log("Error fetching mail API config: " + e.getMessage());
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
     * Get database connection (same as CustomerHandler)
     */
    private Connection getDatabaseConnection() throws Exception {
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        // Log environment variables for debugging (mask password)
        System.out.println("DB_URL from env: " + dbUrl);
        System.out.println("DB_USER from env: " + dbUser);
        System.out.println("DB_PASSWORD from env: " + (dbPassword != null ? "***set***" : "null"));
        
        // Default values for local development
        if (dbUrl == null) {
            dbUrl = "jdbc:postgresql://localhost:5432/nais";
            System.out.println("Using default DB_URL: " + dbUrl);
        }
        if (dbUser == null) {
            dbUser = "postgres";
            System.out.println("Using default DB_USER: " + dbUser);
        }
        if (dbPassword == null) {
            dbPassword = "password";
            System.out.println("Using default DB_PASSWORD");
        }

        System.out.println("Attempting to connect to: " + dbUrl + " with user: " + dbUser);
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Build JSON response string from Map (same as CustomerHandler)
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
     * Build JSON array string from List (same as CustomerHandler)
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
     * Create CORS-enabled response (same as CustomerHandler)
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