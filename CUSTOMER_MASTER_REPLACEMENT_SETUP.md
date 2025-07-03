# Customer Master Data Replacement - Manual Setup Guide

## Overview

This guide provides step-by-step instructions to complete the setup of the Customer Master Data Replacement system after the CloudFormation stack has been deployed successfully.

## Prerequisites

- ✅ CloudFormation stack deployed successfully
- ✅ S3 bucket created: `customer-master-replacement-batch-{stage}-{account-id}`
- ✅ Lambda function created: `Nais-CustomerMasterReplacement-{stage}`
- 🔑 AWS Console access with appropriate permissions

## Manual Setup Required

Due to CloudFormation circular dependency limitations, the S3 bucket event notification must be configured manually after deployment.

### Step 1: Access AWS S3 Console

1. Log in to the **AWS Management Console**
2. Navigate to **S3 service**
3. Find and click on the bucket: `customer-master-replacement-batch-{stage}-{account-id}`

### Step 2: Configure Event Notification

1. **Go to Properties tab**
   - Click on the **Properties** tab in the S3 bucket details

2. **Scroll to Event notifications section**
   - Find the **Event notifications** section
   - Click **Create event notification**

3. **Configure notification settings:**
   
   **General configuration:**
   - **Event name:** `customer-master-csv-upload`
   - **Description:** `Trigger Lambda when CSV files are uploaded`

   **Event types:**
   - ☑️ Check **All object create events**
   - Or specifically select: `s3:ObjectCreated:*`

   **Filters (optional but recommended):**
   - **Prefix:** (leave empty to monitor entire bucket)
   - **Suffix:** `.csv` (only trigger for CSV files)

   **Destination:**
   - **Destination type:** Lambda function
   - **Lambda function:** Select `Nais-CustomerMasterReplacement-{stage}`

4. **Save configuration**
   - Click **Save changes**

### Step 3: Verify Setup

1. **Check notification is created:**
   - You should see the new event notification listed in the Event notifications section
   - Status should show as "Enabled"

2. **Test the integration:**
   - Upload a small test CSV file to the bucket
   - Check CloudWatch logs for the Lambda function: `/aws/lambda/Nais-CustomerMasterReplacement-{stage}`
   - You should see processing logs in JSON format

## CSV File Format

The system expects CSV files with the following format:

```csv
office_cd,customer_cd,normal_name_kanji,chain_store_cd,chain_store_subcd
0001,0001,株式会社サンプル商事,001,001
0002,0002,東京物産株式会社,001,002
```

### Field Specifications:
- **office_cd:** 4-digit office code (CHAR(4))
- **customer_cd:** 4-digit customer code (CHAR(4))
- **normal_name_kanji:** Company name in Japanese (VARCHAR(20))
- **chain_store_cd:** 3-digit chain store code (CHAR(3))
- **chain_store_subcd:** 3-digit chain store sub code (CHAR(3))

## Processing Behavior

When a CSV file is uploaded, the Lambda function will:

1. **🗑️ Delete all existing data** from the `customer_mst` table
2. **📖 Read CSV row by row** to avoid memory overflow
3. **📦 Process in batches** of 10,000 rows for optimal performance
4. **💾 Insert with metadata** (adds `created_by`, `created_at`, `updated_by`, `updated_at`)
5. **🔄 Use transactions** with rollback on any error
6. **📊 Log all operations** in JSON format to CloudWatch

## Monitoring and Troubleshooting

### CloudWatch Logs
- **Log Group:** `/aws/lambda/Nais-CustomerMasterReplacement-{stage}`
- **Log Format:** JSON with timestamps
- **Log Levels:** INFO (normal processing), ERROR (failures)

### Key Metrics to Monitor
- **Processing Time:** Should complete within 15 minutes for 300k rows
- **Batch Processing:** Look for "Processed batch of 10000 records" messages
- **Total Records:** Final count should match your CSV row count
- **Error Handling:** Any errors will trigger transaction rollback

### Sample Log Messages

**Successful processing:**
```json
{
  "timestamp": "2025-07-03T10:30:00",
  "level": "INFO",
  "message": "Processed batch of 10000 records. Total processed: 50000",
  "service": "customer-master-replacement"
}
```

**Error example:**
```json
{
  "timestamp": "2025-07-03T10:30:00",
  "level": "ERROR", 
  "message": "Error parsing line 12345: invalid CSV format",
  "service": "customer-master-replacement",
  "error": "Invalid CSV format at line 12345: expected 5 fields, got 3"
}
```

## Security Considerations

- ✅ **S3 Bucket:** Private access only, no public read/write
- ✅ **Lambda Function:** VPC-enabled with restricted security groups
- ✅ **Database Access:** Uses environment variables for credentials
- ✅ **Logging:** No sensitive data logged, only processing metadata

## File Size Recommendations

- **Optimal:** 300,000 rows (16MB) - tested configuration
- **Maximum:** Limited by Lambda timeout (15 minutes) and memory (1-2GB)
- **Format:** UTF-8 encoded CSV files
- **Compression:** Not required, but .csv extension is mandatory

## Support and Troubleshooting

### Common Issues

1. **Lambda not triggering:**
   - Verify S3 event notification is configured correctly
   - Check Lambda function permissions in IAM
   - Ensure file has .csv extension

2. **Processing failures:**
   - Check CloudWatch logs for specific error messages
   - Verify CSV format matches expected schema
   - Ensure database connectivity from VPC subnets

3. **Partial processing:**
   - Check for transaction rollback messages in logs
   - Verify all CSV rows have exactly 5 fields
   - Look for character encoding issues

### Getting Help

1. **Check CloudWatch Logs** first for detailed error messages
2. **Verify CSV format** using the sample file provided
3. **Test with smaller files** (100-1000 rows) before processing large datasets
4. **Monitor database connections** and VPC network access

## Next Steps

After completing this setup:

1. ✅ **Test with sample data:** Use the provided `sample_customer_master_300k.csv`
2. ✅ **Monitor first run:** Watch CloudWatch logs during initial processing
3. ✅ **Validate data:** Check database to confirm data was inserted correctly
4. ✅ **Document process:** Share this guide with your team
5. ✅ **Schedule regular uploads:** Establish your CSV upload workflow

---

**Note:** This manual setup is only required once per environment. The S3 event notification will persist and continue working for all future CSV uploads.