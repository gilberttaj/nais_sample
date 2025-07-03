package com.nais.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerMasterReplacementHandler implements RequestHandler<S3Event, String> {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private static final int BATCH_SIZE = 10000;

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    public CustomerMasterReplacementHandler() {
        this.s3Client = S3Client.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Logging
    @Tracing
    public String handleRequest(S3Event event, Context context) {
        try {
            logInfo("Starting customer master data replacement process");
            
            for (S3Event.S3EventNotificationRecord record : event.getRecords()) {
                String bucketName = record.getS3().getBucket().getName();
                String objectKey = record.getS3().getObject().getKey();
                
                logInfo("Processing file: " + objectKey + " from bucket: " + bucketName);
                
                processCustomerMasterFile(bucketName, objectKey);
            }
            
            logInfo("Customer master data replacement completed successfully");
            return "SUCCESS";
            
        } catch (Exception e) {
            logError("Error processing customer master data replacement", e);
            throw new RuntimeException("Customer master data replacement failed", e);
        }
    }

    private void processCustomerMasterFile(String bucketName, String objectKey) throws IOException, SQLException {
        logInfo("Starting to process customer master file: " + objectKey);
        
        try (Connection connection = getConnection();
             BufferedReader reader = getCsvReader(bucketName, objectKey)) {
            
            connection.setAutoCommit(false);
            
            try {
                // Delete all existing customer master data
                deleteAllCustomerMasterData(connection);
                logInfo("Deleted all existing customer master data");
                
                // Process CSV file row by row
                processCustomerData(connection, reader);
                
                // Commit the transaction
                connection.commit();
                logInfo("Successfully committed all customer master data");
                
            } catch (Exception e) {
                connection.rollback();
                logError("Error processing customer data, rolling back transaction", e);
                throw e;
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private BufferedReader getCsvReader(String bucketName, String objectKey) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            
            return new BufferedReader(
                    new InputStreamReader(
                            s3Client.getObject(getObjectRequest),
                            StandardCharsets.UTF_8
                    )
            );
        } catch (S3Exception e) {
            logError("Error reading S3 object: " + bucketName + "/" + objectKey, e);
            throw new IOException("Failed to read S3 object", e);
        }
    }

    private void deleteAllCustomerMasterData(Connection connection) throws SQLException {
        String deleteQuery = "DELETE FROM customer_mst";
        
        try (Statement statement = connection.createStatement()) {
            int deletedRows = statement.executeUpdate(deleteQuery);
            logInfo("Deleted " + deletedRows + " existing customer master records");
        }
    }

    private void processCustomerData(Connection connection, BufferedReader reader) throws IOException, SQLException {
        String insertQuery = "INSERT INTO customer_mst (office_cd, customer_cd, normal_name_kanji, " +
                           "chain_store_cd, chain_store_subcd, created_by, created_at, updated_by, updated_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        List<CustomerMasterRecord> batch = new ArrayList<>();
        String line;
        int lineNumber = 0;
        int totalProcessed = 0;
        
        // Skip header row
        String header = reader.readLine();
        if (header != null) {
            logInfo("CSV header: " + header);
            lineNumber++;
        }
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    CustomerMasterRecord record = parseCustomerRecord(line, lineNumber);
                    batch.add(record);
                    
                    if (batch.size() >= BATCH_SIZE) {
                        insertBatch(preparedStatement, batch);
                        totalProcessed += batch.size();
                        logInfo("Processed batch of " + batch.size() + " records. Total processed: " + totalProcessed);
                        batch.clear();
                    }
                    
                } catch (Exception e) {
                    logError("Error parsing line " + lineNumber + ": " + line, e);
                    throw new SQLException("Failed to parse CSV line " + lineNumber, e);
                }
            }
            
            // Process remaining records
            if (!batch.isEmpty()) {
                insertBatch(preparedStatement, batch);
                totalProcessed += batch.size();
                logInfo("Processed final batch of " + batch.size() + " records. Total processed: " + totalProcessed);
            }
            
            logInfo("Successfully processed " + totalProcessed + " customer master records");
        }
    }

    private CustomerMasterRecord parseCustomerRecord(String line, int lineNumber) {
        String[] fields = line.split(",");
        
        if (fields.length < 5) {
            throw new IllegalArgumentException("Invalid CSV format at line " + lineNumber + ": expected 5 fields, got " + fields.length);
        }
        
        return new CustomerMasterRecord(
                fields[0].trim(), // office_cd
                fields[1].trim(), // customer_cd
                fields[2].trim(), // normal_name_kanji
                fields[3].trim(), // chain_store_cd
                fields[4].trim()  // chain_store_subcd
        );
    }

    private void insertBatch(PreparedStatement preparedStatement, List<CustomerMasterRecord> batch) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        String systemUser = "BATCH_SYSTEM";
        
        for (CustomerMasterRecord record : batch) {
            preparedStatement.setString(1, record.getOfficeCd());
            preparedStatement.setString(2, record.getCustomerCd());
            preparedStatement.setString(3, record.getNormalNameKanji());
            preparedStatement.setString(4, record.getChainStoreCd());
            preparedStatement.setString(5, record.getChainStoreSubcd());
            preparedStatement.setString(6, systemUser);
            preparedStatement.setObject(7, now);
            preparedStatement.setString(8, systemUser);
            preparedStatement.setObject(9, now);
            
            preparedStatement.addBatch();
        }
        
        int[] results = preparedStatement.executeBatch();
        logInfo("Batch insert completed. Inserted " + results.length + " records");
        preparedStatement.clearBatch();
    }

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
                    "service", "customer-master-replacement",
                    "error", e != null ? e.getMessage() : null
            );
            return objectMapper.writeValueAsString(logData);
        } catch (Exception ex) {
            return "{\"level\":\"" + level + "\",\"message\":\"" + message + "\",\"error\":\"" + (e != null ? e.getMessage() : "") + "\"}";
        }
    }

    private static class CustomerMasterRecord {
        private final String officeCd;
        private final String customerCd;
        private final String normalNameKanji;
        private final String chainStoreCd;
        private final String chainStoreSubcd;

        public CustomerMasterRecord(String officeCd, String customerCd, String normalNameKanji, 
                                  String chainStoreCd, String chainStoreSubcd) {
            this.officeCd = officeCd;
            this.customerCd = customerCd;
            this.normalNameKanji = normalNameKanji;
            this.chainStoreCd = chainStoreCd;
            this.chainStoreSubcd = chainStoreSubcd;
        }

        public String getOfficeCd() { return officeCd; }
        public String getCustomerCd() { return customerCd; }
        public String getNormalNameKanji() { return normalNameKanji; }
        public String getChainStoreCd() { return chainStoreCd; }
        public String getChainStoreSubcd() { return chainStoreSubcd; }
    }
}