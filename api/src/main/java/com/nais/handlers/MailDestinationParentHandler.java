package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mail Destination Parent Master API Handler
 * メール宛先親マスタAPI - CRUD operations
 */
public class MailDestinationParentHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper;

    public MailDestinationParentHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Logging
    @Tracing
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            logInfo("Processing Mail Destination Parent API request: " + input.getHttpMethod() + " " + input.getPath());

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
            Map<String, String> pathParameters = input.getPathParameters();

            // Route requests
            if (path.equals("/mail-destination-parent") && "GET".equals(method)) {
                return handleGetAllParents(input);
            } else if (path.equals("/mail-destination-parent") && "POST".equals(method)) {
                return handleCreateParent(input);
            } else if (path.startsWith("/mail-destination-parent/") && "GET".equals(method)) {
                String compositeKey = extractCompositeKey(pathParameters);
                return handleGetParentByKey(compositeKey);
            } else if (path.startsWith("/mail-destination-parent/") && "PUT".equals(method)) {
                String compositeKey = extractCompositeKey(pathParameters);
                return handleUpdateParent(compositeKey, input);
            } else if (path.startsWith("/mail-destination-parent/") && "DELETE".equals(method)) {
                String compositeKey = extractCompositeKey(pathParameters);
                return handleDeleteParent(compositeKey);
            } else {
                logInfo("Endpoint not found: " + method + " " + path);
                return createCorsResponse(404, "{\"error\":\"Not Found\",\"message\":\"Endpoint not found\"}");
            }

        } catch (Exception e) {
            logError("Error in MailDestinationParentHandler", e);
            return createCorsResponse(500, "{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /mail-destination-parent - Get all parent records
     */
    private APIGatewayProxyResponseEvent handleGetAllParents(APIGatewayProxyRequestEvent input) {
        try {
            logInfo("Fetching all mail destination parent records");

            Map<String, String> queryParams = input.getQueryStringParameters();
            String jobId = queryParams != null ? queryParams.get("job_id") : null;
            String customerCd = queryParams != null ? queryParams.get("customer_cd") : null;
            String deleteFlag = queryParams != null ? queryParams.get("delete_flag") : "0"; // Default to active records

            List<Map<String, Object>> parents = new ArrayList<>();

            try (Connection conn = getDatabaseConnection()) {
                StringBuilder sql = new StringBuilder(
                    "SELECT job_id, office_cd, customer_cd, chain_store_cd, supplier_cd, order_branch_cd, " +
                    "extend_cd, destination_name, send_mode, search_file, search_directory, send_directory, " +
                    "subject, body_file_path, attachment_file_path, mailing_list_id, update_sys_div, " +
                    "importer_cd, delete_flag, created_by, created_at, updated_by, updated_at " +
                    "FROM mail_destination_parent_mst WHERE 1=1"
                );

                List<Object> parameters = new ArrayList<>();

                if (jobId != null && !jobId.isEmpty()) {
                    sql.append(" AND job_id = ?");
                    parameters.add(jobId);
                }
                if (customerCd != null && !customerCd.isEmpty()) {
                    sql.append(" AND customer_cd = ?");
                    parameters.add(customerCd);
                }
                if (deleteFlag != null) {
                    sql.append(" AND delete_flag = ?");
                    parameters.add(deleteFlag);
                }

                sql.append(" ORDER BY job_id, office_cd, customer_cd");

                try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                    for (int i = 0; i < parameters.size(); i++) {
                        stmt.setObject(i + 1, parameters.get(i));
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            parents.add(mapResultSetToParent(rs));
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", parents);
            response.put("count", parents.size());

            logInfo("Successfully fetched " + parents.size() + " parent records");
            return createCorsResponse(200, objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            logError("Error fetching parent records", e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * POST /mail-destination-parent - Create new parent record
     */
    private APIGatewayProxyResponseEvent handleCreateParent(APIGatewayProxyRequestEvent input) {
        try {
            logInfo("Creating new mail destination parent record");

            if (input.getBody() == null || input.getBody().trim().isEmpty()) {
                return createErrorResponse(400, "Bad Request", "Request body is required");
            }

            Map<String, Object> requestData = objectMapper.readValue(input.getBody(), Map.class);
            
            // Validate required fields
            String[] requiredFields = {"job_id", "office_cd", "customer_cd", "chain_store_cd", 
                                     "supplier_cd", "order_branch_cd", "extend_cd", "send_mode", 
                                     "search_file", "search_directory", "send_directory", "subject", 
                                     "body_file_path", "attachment_file_path", "mailing_list_id", "update_sys_div"};
            
            for (String field : requiredFields) {
                if (!requestData.containsKey(field) || requestData.get(field) == null) {
                    return createErrorResponse(400, "Bad Request", "Required field missing: " + field);
                }
            }

            try (Connection conn = getDatabaseConnection()) {
                String sql = "INSERT INTO mail_destination_parent_mst " +
                           "(job_id, office_cd, customer_cd, chain_store_cd, supplier_cd, order_branch_cd, " +
                           "extend_cd, destination_name, send_mode, search_file, search_directory, send_directory, " +
                           "subject, body_file_path, attachment_file_path, mailing_list_id, update_sys_div, " +
                           "importer_cd, delete_flag, created_by, created_at, updated_by, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    String currentUser = "API_USER"; // In production, get from JWT token

                    stmt.setString(1, (String) requestData.get("job_id"));
                    stmt.setString(2, (String) requestData.get("office_cd"));
                    stmt.setString(3, (String) requestData.get("customer_cd"));
                    stmt.setString(4, (String) requestData.get("chain_store_cd"));
                    stmt.setString(5, (String) requestData.get("supplier_cd"));
                    stmt.setString(6, (String) requestData.get("order_branch_cd"));
                    stmt.setString(7, (String) requestData.get("extend_cd"));
                    stmt.setString(8, (String) requestData.get("destination_name"));
                    stmt.setString(9, (String) requestData.get("send_mode"));
                    stmt.setString(10, (String) requestData.get("search_file"));
                    stmt.setString(11, (String) requestData.get("search_directory"));
                    stmt.setString(12, (String) requestData.get("send_directory"));
                    stmt.setString(13, (String) requestData.get("subject"));
                    stmt.setString(14, (String) requestData.get("body_file_path"));
                    stmt.setString(15, (String) requestData.get("attachment_file_path"));
                    stmt.setString(16, (String) requestData.get("mailing_list_id"));
                    stmt.setString(17, (String) requestData.get("update_sys_div"));
                    stmt.setString(18, (String) requestData.get("importer_cd"));
                    stmt.setString(19, (String) requestData.getOrDefault("delete_flag", "0"));
                    stmt.setString(20, currentUser);
                    stmt.setTimestamp(21, now);
                    stmt.setString(22, currentUser);
                    stmt.setTimestamp(23, now);

                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        logInfo("Successfully created parent record with job_id: " + requestData.get("job_id"));
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("message", "Mail destination parent record created successfully");
                        response.put("job_id", requestData.get("job_id"));
                        
                        return createCorsResponse(201, objectMapper.writeValueAsString(response));
                    } else {
                        return createErrorResponse(500, "Database Error", "Failed to create record");
                    }
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                logError("Duplicate key error when creating parent record", e);
                return createErrorResponse(409, "Conflict", "Record with this key already exists");
            }
            logError("Database error when creating parent record", e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        } catch (Exception e) {
            logError("Error creating parent record", e);
            return createErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }

    /**
     * GET /mail-destination-parent/{key} - Get specific parent record by composite key
     */
    private APIGatewayProxyResponseEvent handleGetParentByKey(String compositeKey) {
        try {
            logInfo("Fetching parent record with key: " + compositeKey);

            if (compositeKey == null || compositeKey.trim().isEmpty()) {
                return createErrorResponse(400, "Bad Request", "Composite key is required");
            }

            // Parse composite key: job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd
            // Handle potential trailing spaces in extend_cd field (CHAR(10) type)
            String[] keyParts = compositeKey.split("\\|", -1); // -1 to preserve empty trailing parts
            if (keyParts.length != 7) {
                logError("Invalid composite key format. Expected 7 parts, got " + keyParts.length + ". Key: [" + compositeKey + "]", null);
                return createErrorResponse(400, "Bad Request", 
                    "Invalid composite key format. Expected: job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd. Got " + keyParts.length + " parts.");
            }
            
            // Log all parts for debugging
            for (int i = 0; i < keyParts.length; i++) {
                logInfo("Key part " + i + ": [" + keyParts[i] + "] (length: " + keyParts[i].length() + ")");
            }

            Map<String, Object> parent = null;

            try (Connection conn = getDatabaseConnection()) {
                String sql = "SELECT job_id, office_cd, customer_cd, chain_store_cd, supplier_cd, order_branch_cd, " +
                           "extend_cd, destination_name, send_mode, search_file, search_directory, send_directory, " +
                           "subject, body_file_path, attachment_file_path, mailing_list_id, update_sys_div, " +
                           "importer_cd, delete_flag, created_by, created_at, updated_by, updated_at " +
                           "FROM mail_destination_parent_mst " +
                           "WHERE job_id = ? AND office_cd = ? AND customer_cd = ? AND chain_store_cd = ? " +
                           "AND supplier_cd = ? AND order_branch_cd = ? AND extend_cd = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < keyParts.length; i++) {
                        stmt.setString(i + 1, keyParts[i]);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            parent = mapResultSetToParent(rs);
                        }
                    }
                }
            }

            if (parent == null) {
                logInfo("Parent record not found with key: " + compositeKey);
                return createErrorResponse(404, "Not Found", "Parent record not found with key: " + compositeKey);
            }

            logInfo("Successfully fetched parent record with key: " + compositeKey);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", parent);

            return createCorsResponse(200, objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            logError("Error fetching parent record with key: " + compositeKey, e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * PUT /mail-destination-parent/{key} - Update parent record
     */
    private APIGatewayProxyResponseEvent handleUpdateParent(String compositeKey, APIGatewayProxyRequestEvent input) {
        try {
            logInfo("Updating parent record with key: " + compositeKey);

            if (compositeKey == null || compositeKey.trim().isEmpty()) {
                return createErrorResponse(400, "Bad Request", "Composite key is required");
            }

            if (input.getBody() == null || input.getBody().trim().isEmpty()) {
                return createErrorResponse(400, "Bad Request", "Request body is required");
            }

            // Parse composite key with proper handling for trailing spaces
            String[] keyParts = compositeKey.split("\\|", -1); // -1 to preserve empty trailing parts
            if (keyParts.length != 7) {
                logError("Invalid composite key format in UPDATE. Expected 7 parts, got " + keyParts.length + ". Key: [" + compositeKey + "]", null);
                return createErrorResponse(400, "Bad Request", 
                    "Invalid composite key format. Expected: job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd. Got " + keyParts.length + " parts.");
            }
            
            // Log all parts for debugging UPDATE operation
            for (int i = 0; i < keyParts.length; i++) {
                logInfo("UPDATE Key part " + i + ": [" + keyParts[i] + "] (length: " + keyParts[i].length() + ")");
            }

            // Check access control before update
            String currentUpdateSysDiv = getCurrentUpdateSysDiv(keyParts);
            if (!canModifyRecord(currentUpdateSysDiv, "UPDATE")) {
                return createErrorResponse(403, "Forbidden", 
                    "Update operation not allowed for update_sys_div: " + currentUpdateSysDiv + 
                    ". This record can only be modified by " + getUpdateSysDivDescription(currentUpdateSysDiv));
            }

            Map<String, Object> requestData = objectMapper.readValue(input.getBody(), Map.class);

            try (Connection conn = getDatabaseConnection()) {
                String sql = "UPDATE mail_destination_parent_mst SET " +
                           "destination_name = ?, send_mode = ?, search_file = ?, search_directory = ?, " +
                           "send_directory = ?, subject = ?, body_file_path = ?, attachment_file_path = ?, " +
                           "mailing_list_id = ?, update_sys_div = ?, importer_cd = ?, delete_flag = ?, " +
                           "updated_by = ?, updated_at = ? " +
                           "WHERE job_id = ? AND office_cd = ? AND customer_cd = ? AND chain_store_cd = ? " +
                           "AND supplier_cd = ? AND order_branch_cd = ? AND extend_cd = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    String currentUser = "API_USER"; // In production, get from JWT token

                    stmt.setString(1, (String) requestData.get("destination_name"));
                    stmt.setString(2, (String) requestData.get("send_mode"));
                    stmt.setString(3, (String) requestData.get("search_file"));
                    stmt.setString(4, (String) requestData.get("search_directory"));
                    stmt.setString(5, (String) requestData.get("send_directory"));
                    stmt.setString(6, (String) requestData.get("subject"));
                    stmt.setString(7, (String) requestData.get("body_file_path"));
                    stmt.setString(8, (String) requestData.get("attachment_file_path"));
                    stmt.setString(9, (String) requestData.get("mailing_list_id"));
                    stmt.setString(10, (String) requestData.get("update_sys_div"));
                    stmt.setString(11, (String) requestData.get("importer_cd"));
                    stmt.setString(12, (String) requestData.getOrDefault("delete_flag", "0"));
                    stmt.setString(13, currentUser);
                    stmt.setTimestamp(14, now);

                    // Where clause parameters
                    for (int i = 0; i < keyParts.length; i++) {
                        stmt.setString(15 + i, keyParts[i]);
                    }

                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        logInfo("Successfully updated parent record with key: " + compositeKey);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("message", "Mail destination parent record updated successfully");
                        response.put("key", compositeKey);
                        
                        return createCorsResponse(200, objectMapper.writeValueAsString(response));
                    } else {
                        return createErrorResponse(404, "Not Found", "Parent record not found with key: " + compositeKey);
                    }
                }
            }

        } catch (Exception e) {
            logError("Error updating parent record with key: " + compositeKey, e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * DELETE /mail-destination-parent/{key} - Soft delete parent record
     */
    private APIGatewayProxyResponseEvent handleDeleteParent(String compositeKey) {
        try {
            logInfo("Deleting parent record with key: " + compositeKey);

            if (compositeKey == null || compositeKey.trim().isEmpty()) {
                return createErrorResponse(400, "Bad Request", "Composite key is required");
            }

            // Parse composite key with proper handling for trailing spaces
            String[] keyParts = compositeKey.split("\\|", -1); // -1 to preserve empty trailing parts
            if (keyParts.length != 7) {
                logError("Invalid composite key format in DELETE. Expected 7 parts, got " + keyParts.length + ". Key: [" + compositeKey + "]", null);
                return createErrorResponse(400, "Bad Request", 
                    "Invalid composite key format. Expected: job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd. Got " + keyParts.length + " parts.");
            }
            
            // Log all parts for debugging DELETE operation
            for (int i = 0; i < keyParts.length; i++) {
                logInfo("DELETE Key part " + i + ": [" + keyParts[i] + "] (length: " + keyParts[i].length() + ")");
            }

            // Check access control before delete
            String currentUpdateSysDiv = getCurrentUpdateSysDiv(keyParts);
            if (!canModifyRecord(currentUpdateSysDiv, "DELETE")) {
                return createErrorResponse(403, "Forbidden", 
                    "Delete operation not allowed for update_sys_div: " + currentUpdateSysDiv + 
                    ". This record can only be modified by " + getUpdateSysDivDescription(currentUpdateSysDiv));
            }

            try (Connection conn = getDatabaseConnection()) {
                String sql = "UPDATE mail_destination_parent_mst SET delete_flag = '1', " +
                           "updated_by = ?, updated_at = ? " +
                           "WHERE job_id = ? AND office_cd = ? AND customer_cd = ? AND chain_store_cd = ? " +
                           "AND supplier_cd = ? AND order_branch_cd = ? AND extend_cd = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    String currentUser = "API_USER"; // In production, get from JWT token

                    stmt.setString(1, currentUser);
                    stmt.setTimestamp(2, now);

                    // Where clause parameters
                    for (int i = 0; i < keyParts.length; i++) {
                        stmt.setString(3 + i, keyParts[i]);
                    }

                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        logInfo("Successfully deleted parent record with key: " + compositeKey);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("message", "Mail destination parent record deleted successfully");
                        response.put("key", compositeKey);
                        
                        return createCorsResponse(200, objectMapper.writeValueAsString(response));
                    } else {
                        return createErrorResponse(404, "Not Found", "Parent record not found with key: " + compositeKey);
                    }
                }
            }

        } catch (Exception e) {
            logError("Error deleting parent record with key: " + compositeKey, e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * Helper methods
     */
    private String extractCompositeKey(Map<String, String> pathParameters) {
        if (pathParameters != null && pathParameters.containsKey("key")) {
            String key = pathParameters.get("key");
            // URL decode the key to handle encoded characters properly
            if (key != null) {
                try {
                    key = java.net.URLDecoder.decode(key, "UTF-8");
                    logInfo("Extracted and decoded composite key: [" + key + "]");
                } catch (Exception e) {
                    logError("Error decoding composite key: " + key, e);
                }
            }
            return key;
        }
        return null;
    }

    private Map<String, Object> mapResultSetToParent(ResultSet rs) throws SQLException {
        Map<String, Object> parent = new HashMap<>();
        parent.put("job_id", rs.getString("job_id"));
        parent.put("office_cd", rs.getString("office_cd"));
        parent.put("customer_cd", rs.getString("customer_cd"));
        parent.put("chain_store_cd", rs.getString("chain_store_cd"));
        parent.put("supplier_cd", rs.getString("supplier_cd"));
        parent.put("order_branch_cd", rs.getString("order_branch_cd"));
        parent.put("extend_cd", rs.getString("extend_cd"));
        parent.put("destination_name", rs.getString("destination_name"));
        parent.put("send_mode", rs.getString("send_mode"));
        parent.put("search_file", rs.getString("search_file"));
        parent.put("search_directory", rs.getString("search_directory"));
        parent.put("send_directory", rs.getString("send_directory"));
        parent.put("subject", rs.getString("subject"));
        parent.put("body_file_path", rs.getString("body_file_path"));
        parent.put("attachment_file_path", rs.getString("attachment_file_path"));
        parent.put("mailing_list_id", rs.getString("mailing_list_id"));
        parent.put("update_sys_div", rs.getString("update_sys_div"));
        parent.put("importer_cd", rs.getString("importer_cd"));
        parent.put("delete_flag", rs.getString("delete_flag"));
        parent.put("created_by", rs.getString("created_by"));
        parent.put("created_at", rs.getTimestamp("created_at"));
        parent.put("updated_by", rs.getString("updated_by"));
        parent.put("updated_at", rs.getTimestamp("updated_at"));
        return parent;
    }

    /**
     * Authentication methods (similar to other handlers)
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
            
            String environment = System.getenv("AUTH_MODE");
            if ("MOCK".equalsIgnoreCase(environment)) {
                logInfo("Mock mode: accepting token for local development");
                return token.startsWith("eyJ") || token.contains("mock") || token.contains("dummy");
            }

            return validateCognitoToken(token);

        } catch (Exception e) {
            logError("Authentication error", e);
            return false;
        }
    }

    private boolean validateCognitoToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                logInfo("Invalid JWT format");
                return false;
            }

            logInfo("Token validation passed (simplified)");
            return true;

        } catch (Exception e) {
            logError("Token validation failed", e);
            return false;
        }
    }

    private Connection getDatabaseConnection() throws Exception {
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        if (dbUrl == null) {
            dbUrl = "jdbc:postgresql://localhost:5432/nais";
        }
        if (dbUser == null) {
            dbUser = "postgres";
        }
        if (dbPassword == null) {
            dbPassword = "password";
        }

        logInfo("Attempting to connect to database: " + dbUrl);
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        logInfo("Successfully connected to database");
        return conn;
    }

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

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String error, String message) {
        try {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", error);
            errorResponse.put("message", message);
            return createCorsResponse(statusCode, objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            return createCorsResponse(statusCode, "{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
        }
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
                    "service", "mail-destination-parent-api",
                    "error", e != null ? e.getMessage() : null
            );
            return objectMapper.writeValueAsString(logData);
        } catch (Exception ex) {
            return "{\"level\":\"" + level + "\",\"message\":\"" + message + "\",\"service\":\"mail-destination-parent-api\",\"error\":\"" + (e != null ? e.getMessage() : "") + "\"}";
        }
    }

    /**
     * Access control methods based on update_sys_div
     */
    private String getCurrentUpdateSysDiv(String[] keyParts) {
        try (Connection conn = getDatabaseConnection()) {
            String sql = "SELECT update_sys_div FROM mail_destination_parent_mst " +
                       "WHERE job_id = ? AND office_cd = ? AND customer_cd = ? AND chain_store_cd = ? " +
                       "AND supplier_cd = ? AND order_branch_cd = ? AND extend_cd = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < keyParts.length; i++) {
                    stmt.setString(i + 1, keyParts[i]);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("update_sys_div");
                    }
                }
            }
        } catch (Exception e) {
            logError("Error getting current update_sys_div", e);
        }
        return null;
    }

    /**
     * Check if the current API request can modify records with the given update_sys_div
     * 0: 制限なし (No restrictions) - API can modify
     * 1: 他システム連携のみ (Other system integration only) - API cannot modify
     * 2: 宛先サービスのみ (Destination service only) - API cannot modify
     */
    private boolean canModifyRecord(String updateSysDiv, String operation) {
        if (updateSysDiv == null) {
            logInfo("Unknown update_sys_div, allowing operation: " + operation);
            return true; // If we can't determine the value, allow the operation
        }

        // Get the calling system type from environment or headers
        String callingSystem = getCurrentCallingSystem();
        
        switch (updateSysDiv) {
            case "0": // 制限なし (No restrictions)
                logInfo("No restrictions (update_sys_div=0), allowing " + operation);
                return true;
                
            case "1": // 他システム連携のみ (Other system integration only)
                boolean isOtherSystem = "OTHER_SYSTEM".equals(callingSystem);
                logInfo("Other system integration only (update_sys_div=1), " + 
                       "calling system: " + callingSystem + ", allowing: " + isOtherSystem);
                return isOtherSystem;
                
            case "2": // 宛先サービスのみ (Destination service only)
                boolean isDestinationService = "DESTINATION_SERVICE".equals(callingSystem);
                logInfo("Destination service only (update_sys_div=2), " + 
                       "calling system: " + callingSystem + ", allowing: " + isDestinationService);
                return isDestinationService;
                
            default:
                logError("Unknown update_sys_div value: " + updateSysDiv + ", denying " + operation, null);
                return false;
        }
    }

    /**
     * Get the current calling system type
     * This can be determined from:
     * 1. Environment variables
     * 2. HTTP headers
     * 3. JWT token claims
     * 4. API key identification
     */
    private String getCurrentCallingSystem() {
        // For now, assume this is the main API service
        // In production, this would be determined by:
        // - Checking X-Calling-System header
        // - Analyzing JWT token claims
        // - Environment configuration
        String callingSystem = System.getenv("CALLING_SYSTEM");
        if (callingSystem != null) {
            return callingSystem;
        }
        
        // Default to API service (which should have no restrictions for update_sys_div=0)
        return "API_SERVICE";
    }

    /**
     * Get human-readable description of update_sys_div restrictions
     */
    private String getUpdateSysDivDescription(String updateSysDiv) {
        switch (updateSysDiv) {
            case "0": return "no restrictions (制限なし)";
            case "1": return "other system integration only (他システム連携のみ)";
            case "2": return "destination service only (宛先サービスのみ)";
            default: return "unknown restriction type";
        }
    }
}