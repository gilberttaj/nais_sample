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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Mail Destination Child Master API Handler
 * メール宛先子マスタAPI - CRUD operations for individual email recipients
 * 
 * Table: mail_destination_child_mst
 * Primary Key: mailing_list_id, destination_seq
 * Purpose: Manage individual email addresses within mailing lists
 */
public class MailDestinationChildHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Status Division Constants
    private static final String STATUS_ACTIVE = "0";           // 有効
    private static final String STATUS_INACTIVE_TEST = "1";    // 無効(テスト運用)
    private static final String STATUS_INACTIVE_DELETED = "2"; // 無効(削除)

    public MailDestinationChildHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Logging
    @Tracing
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            logInfo("Processing Mail Destination Child API request: " + input.getHttpMethod() + " " + input.getPath());

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
            if (path.equals("/mail-destination-child") && "GET".equals(method)) {
                return handleGetAllChildren(input);
            } else if (path.equals("/mail-destination-child") && "POST".equals(method)) {
                return handleCreateChild(input);
            } else if (path.startsWith("/mail-destination-child/") && "GET".equals(method)) {
                return handleGetChildByKey(pathParameters);
            } else if (path.startsWith("/mail-destination-child/") && "PUT".equals(method)) {
                return handleUpdateChild(pathParameters, input);
            } else if (path.startsWith("/mail-destination-child/") && "DELETE".equals(method)) {
                return handleDeleteChild(pathParameters);
            } else {
                logInfo("Endpoint not found: " + method + " " + path);
                return createCorsResponse(404, "{\"error\":\"Not Found\",\"message\":\"Endpoint not found\"}");
            }

        } catch (Exception e) {
            logError("Error in MailDestinationChildHandler", e);
            return createCorsResponse(500, "{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /mail-destination-child - Get all child records with optional filtering
     */
    private APIGatewayProxyResponseEvent handleGetAllChildren(APIGatewayProxyRequestEvent input) {
        try {
            logInfo("Fetching mail destination child records");

            Map<String, String> queryParams = input.getQueryStringParameters();
            String mailingListId = queryParams != null ? queryParams.get("mailing_list_id") : null;
            String statusDiv = queryParams != null ? queryParams.get("status_div") : STATUS_ACTIVE; // Default to active

            List<Map<String, Object>> children = new ArrayList<>();

            try (Connection conn = getDatabaseConnection()) {
                StringBuilder sql = new StringBuilder(
                    "SELECT mailing_list_id, destination_seq, destination_address, destination_note, " +
                    "status_div, importer_cd, created_by, created_at, updated_by, updated_at " +
                    "FROM mail_destination_child_mst WHERE 1=1"
                );

                List<Object> parameters = new ArrayList<>();

                if (mailingListId != null && !mailingListId.trim().isEmpty()) {
                    sql.append(" AND mailing_list_id = ?");
                    parameters.add(mailingListId.trim());
                }
                if (statusDiv != null) {
                    sql.append(" AND status_div = ?");
                    parameters.add(statusDiv);
                }

                sql.append(" ORDER BY mailing_list_id, destination_seq");

                try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                    for (int i = 0; i < parameters.size(); i++) {
                        stmt.setObject(i + 1, parameters.get(i));
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            children.add(mapResultSetToChild(rs));
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", children);
            response.put("count", children.size());

            logInfo("Successfully fetched " + children.size() + " child records");
            return createCorsResponse(200, objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            logError("Error fetching child records", e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * POST /mail-destination-child - Create new child record
     */
    private APIGatewayProxyResponseEvent handleCreateChild(APIGatewayProxyRequestEvent input) {
        try {
            logInfo("Creating new mail destination child record");

            if (input.getBody() == null || input.getBody().trim().isEmpty()) {
                return createErrorResponse(400, "Bad Request", "Request body is required");
            }

            Map<String, Object> requestData = objectMapper.readValue(input.getBody(), Map.class);
            
            // Validate required fields
            String[] requiredFields = {"mailing_list_id", "destination_address", "status_div"};
            for (String field : requiredFields) {
                if (!requestData.containsKey(field) || requestData.get(field) == null) {
                    return createErrorResponse(400, "Bad Request", "Required field missing: " + field);
                }
            }

            String mailingListId = (String) requestData.get("mailing_list_id");
            String destinationAddress = (String) requestData.get("destination_address");
            String statusDiv = (String) requestData.get("status_div");

            // Validate data
            APIGatewayProxyResponseEvent validationError = validateChildData(mailingListId, destinationAddress, statusDiv);
            if (validationError != null) {
                return validationError;
            }

            try (Connection conn = getDatabaseConnection()) {
                // Determine next destination_seq for this mailing list
                int nextSeq = getNextDestinationSeq(conn, mailingListId);

                String sql = "INSERT INTO mail_destination_child_mst " +
                           "(mailing_list_id, destination_seq, destination_address, destination_note, " +
                           "status_div, importer_cd, created_by, created_at, updated_by, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    String currentUser = "API_USER"; // In production, get from JWT token

                    stmt.setString(1, mailingListId);
                    stmt.setInt(2, nextSeq);
                    stmt.setString(3, destinationAddress);
                    stmt.setString(4, (String) requestData.get("destination_note"));
                    stmt.setString(5, statusDiv);
                    stmt.setString(6, (String) requestData.get("importer_cd"));
                    stmt.setString(7, currentUser);
                    stmt.setTimestamp(8, now);
                    stmt.setString(9, currentUser);
                    stmt.setTimestamp(10, now);

                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        logInfo("Successfully created child record: " + mailingListId + "/" + nextSeq);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("message", "Mail destination child record created successfully");
                        response.put("mailing_list_id", mailingListId);
                        response.put("destination_seq", nextSeq);
                        
                        return createCorsResponse(201, objectMapper.writeValueAsString(response));
                    } else {
                        return createErrorResponse(500, "Database Error", "Failed to create record");
                    }
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                logError("Duplicate key error when creating child record", e);
                return createErrorResponse(409, "Conflict", "Record with this key already exists");
            }
            logError("Database error when creating child record", e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        } catch (Exception e) {
            logError("Error creating child record", e);
            return createErrorResponse(500, "Internal Server Error", e.getMessage());
        }
    }

    /**
     * GET /mail-destination-child/{mailing_list_id}/{destination_seq} - Get specific child record
     */
    private APIGatewayProxyResponseEvent handleGetChildByKey(Map<String, String> pathParameters) {
        try {
            String mailingListId = pathParameters.get("mailing_list_id");
            String destSeqStr = pathParameters.get("destination_seq");

            logInfo("Fetching child record: " + mailingListId + "/" + destSeqStr);

            if (mailingListId == null || destSeqStr == null) {
                return createErrorResponse(400, "Bad Request", "Both mailing_list_id and destination_seq are required");
            }

            int destinationSeq;
            try {
                destinationSeq = Integer.parseInt(destSeqStr);
            } catch (NumberFormatException e) {
                return createErrorResponse(400, "Bad Request", "destination_seq must be a valid integer");
            }

            Map<String, Object> child = null;

            try (Connection conn = getDatabaseConnection()) {
                String sql = "SELECT mailing_list_id, destination_seq, destination_address, destination_note, " +
                           "status_div, importer_cd, created_by, created_at, updated_by, updated_at " +
                           "FROM mail_destination_child_mst " +
                           "WHERE mailing_list_id = ? AND destination_seq = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, mailingListId);
                    stmt.setInt(2, destinationSeq);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            child = mapResultSetToChild(rs);
                        }
                    }
                }
            }

            if (child == null) {
                logInfo("Child record not found: " + mailingListId + "/" + destinationSeq);
                return createErrorResponse(404, "Not Found", "Child record not found");
            }

            logInfo("Successfully fetched child record: " + mailingListId + "/" + destinationSeq);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", child);

            return createCorsResponse(200, objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            logError("Error fetching child record", e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * PUT /mail-destination-child/{mailing_list_id}/{destination_seq} - Update child record
     */
    private APIGatewayProxyResponseEvent handleUpdateChild(Map<String, String> pathParameters, APIGatewayProxyRequestEvent input) {
        try {
            String mailingListId = pathParameters.get("mailing_list_id");
            String destSeqStr = pathParameters.get("destination_seq");

            logInfo("Updating child record: " + mailingListId + "/" + destSeqStr);

            if (mailingListId == null || destSeqStr == null) {
                return createErrorResponse(400, "Bad Request", "Both mailing_list_id and destination_seq are required");
            }

            if (input.getBody() == null || input.getBody().trim().isEmpty()) {
                return createErrorResponse(400, "Bad Request", "Request body is required");
            }

            int destinationSeq;
            try {
                destinationSeq = Integer.parseInt(destSeqStr);
            } catch (NumberFormatException e) {
                return createErrorResponse(400, "Bad Request", "destination_seq must be a valid integer");
            }

            Map<String, Object> requestData = objectMapper.readValue(input.getBody(), Map.class);

            // Validate updatable fields if provided
            if (requestData.containsKey("destination_address")) {
                String email = (String) requestData.get("destination_address");
                if (!isValidEmail(email)) {
                    return createErrorResponse(400, "Bad Request", "Invalid email format: " + email);
                }
            }

            if (requestData.containsKey("status_div")) {
                String statusDiv = (String) requestData.get("status_div");
                if (!isValidStatusDiv(statusDiv)) {
                    return createErrorResponse(400, "Bad Request", "Invalid status_div. Must be 0, 1, or 2");
                }
            }

            try (Connection conn = getDatabaseConnection()) {
                String sql = "UPDATE mail_destination_child_mst SET " +
                           "destination_address = COALESCE(?, destination_address), " +
                           "destination_note = COALESCE(?, destination_note), " +
                           "status_div = COALESCE(?, status_div), " +
                           "importer_cd = COALESCE(?, importer_cd), " +
                           "updated_by = ?, updated_at = ? " +
                           "WHERE mailing_list_id = ? AND destination_seq = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    String currentUser = "API_USER"; // In production, get from JWT token

                    stmt.setString(1, (String) requestData.get("destination_address"));
                    stmt.setString(2, (String) requestData.get("destination_note"));
                    stmt.setString(3, (String) requestData.get("status_div"));
                    stmt.setString(4, (String) requestData.get("importer_cd"));
                    stmt.setString(5, currentUser);
                    stmt.setTimestamp(6, now);
                    stmt.setString(7, mailingListId);
                    stmt.setInt(8, destinationSeq);

                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        logInfo("Successfully updated child record: " + mailingListId + "/" + destinationSeq);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("message", "Mail destination child record updated successfully");
                        response.put("mailing_list_id", mailingListId);
                        response.put("destination_seq", destinationSeq);
                        
                        return createCorsResponse(200, objectMapper.writeValueAsString(response));
                    } else {
                        return createErrorResponse(404, "Not Found", "Child record not found");
                    }
                }
            }

        } catch (Exception e) {
            logError("Error updating child record", e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * DELETE /mail-destination-child/{mailing_list_id}/{destination_seq} - Soft delete child record
     */
    private APIGatewayProxyResponseEvent handleDeleteChild(Map<String, String> pathParameters) {
        try {
            String mailingListId = pathParameters.get("mailing_list_id");
            String destSeqStr = pathParameters.get("destination_seq");

            logInfo("Deleting child record: " + mailingListId + "/" + destSeqStr);

            if (mailingListId == null || destSeqStr == null) {
                return createErrorResponse(400, "Bad Request", "Both mailing_list_id and destination_seq are required");
            }

            int destinationSeq;
            try {
                destinationSeq = Integer.parseInt(destSeqStr);
            } catch (NumberFormatException e) {
                return createErrorResponse(400, "Bad Request", "destination_seq must be a valid integer");
            }

            try (Connection conn = getDatabaseConnection()) {
                String sql = "UPDATE mail_destination_child_mst SET status_div = ?, " +
                           "updated_by = ?, updated_at = ? " +
                           "WHERE mailing_list_id = ? AND destination_seq = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    String currentUser = "API_USER"; // In production, get from JWT token

                    stmt.setString(1, STATUS_INACTIVE_DELETED); // Set to inactive/deleted
                    stmt.setString(2, currentUser);
                    stmt.setTimestamp(3, now);
                    stmt.setString(4, mailingListId);
                    stmt.setInt(5, destinationSeq);

                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        logInfo("Successfully deleted child record: " + mailingListId + "/" + destinationSeq);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("message", "Mail destination child record deleted successfully");
                        response.put("mailing_list_id", mailingListId);
                        response.put("destination_seq", destinationSeq);
                        
                        return createCorsResponse(200, objectMapper.writeValueAsString(response));
                    } else {
                        return createErrorResponse(404, "Not Found", "Child record not found");
                    }
                }
            }

        } catch (Exception e) {
            logError("Error deleting child record", e);
            return createErrorResponse(500, "Database Error", e.getMessage());
        }
    }

    /**
     * Helper methods
     */
    private int getNextDestinationSeq(Connection conn, String mailingListId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(destination_seq), 0) + 1 as next_seq " +
                   "FROM mail_destination_child_mst WHERE mailing_list_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mailingListId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("next_seq");
                }
            }
        }
        return 1; // Start with 1 if no records exist
    }

    private APIGatewayProxyResponseEvent validateChildData(String mailingListId, String destinationAddress, String statusDiv) {
        // Validate mailing list ID
        if (mailingListId == null || mailingListId.trim().isEmpty()) {
            return createErrorResponse(400, "Bad Request", "mailing_list_id cannot be empty");
        }
        if (mailingListId.length() > 320) {
            return createErrorResponse(400, "Bad Request", "mailing_list_id exceeds maximum length (320)");
        }

        // Validate email address
        if (!isValidEmail(destinationAddress)) {
            return createErrorResponse(400, "Bad Request", "Invalid email format: " + destinationAddress);
        }

        // Validate status division
        if (!isValidStatusDiv(statusDiv)) {
            return createErrorResponse(400, "Bad Request", "Invalid status_div. Must be 0 (active), 1 (test inactive), or 2 (deleted)");
        }

        return null; // No validation errors
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (email.length() > 320) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private boolean isValidStatusDiv(String statusDiv) {
        return STATUS_ACTIVE.equals(statusDiv) || 
               STATUS_INACTIVE_TEST.equals(statusDiv) || 
               STATUS_INACTIVE_DELETED.equals(statusDiv);
    }

    private Map<String, Object> mapResultSetToChild(ResultSet rs) throws SQLException {
        Map<String, Object> child = new HashMap<>();
        child.put("mailing_list_id", rs.getString("mailing_list_id"));
        child.put("destination_seq", rs.getInt("destination_seq"));
        child.put("destination_address", rs.getString("destination_address"));
        child.put("destination_note", rs.getString("destination_note"));
        child.put("status_div", rs.getString("status_div"));
        child.put("status_description", getStatusDescription(rs.getString("status_div")));
        child.put("importer_cd", rs.getString("importer_cd"));
        child.put("created_by", rs.getString("created_by"));
        child.put("created_at", rs.getTimestamp("created_at"));
        child.put("updated_by", rs.getString("updated_by"));
        child.put("updated_at", rs.getTimestamp("updated_at"));
        return child;
    }

    private String getStatusDescription(String statusDiv) {
        switch (statusDiv) {
            case STATUS_ACTIVE: return "有効";
            case STATUS_INACTIVE_TEST: return "無効(テスト運用)";
            case STATUS_INACTIVE_DELETED: return "無効(削除)";
            default: return "不明";
        }
    }

    /**
     * Authentication methods (same as parent handler)
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
                    "service", "mail-destination-child-api",
                    "error", e != null ? e.getMessage() : null
            );
            return objectMapper.writeValueAsString(logData);
        } catch (Exception ex) {
            return "{\"level\":\"" + level + "\",\"message\":\"" + message + "\",\"service\":\"mail-destination-child-api\",\"error\":\"" + (e != null ? e.getMessage() : "") + "\"}";
        }
    }
}