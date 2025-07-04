# NAIS API Testing Guide

This guide provides comprehensive instructions for testing all API endpoints using curl commands, including the business flow and purpose of each API.

## System Overview

The NAIS (Mail Destination Master Management System) provides a comprehensive suite of APIs for managing email destination configurations and customer data. The system follows a hierarchical structure where customer data serves as the foundation, and mail destination configurations (parent and child) define how emails are sent to different recipients.

### Business Flow Overview

```
1. Customer Management
   ↓
2. Mail Destination Parent (定義)
   ↓
3. Mail Destination Child (宛先リスト)
   ↓
4. Mail Processing & Configuration
```

### System Architecture

- **Authentication Layer**: Google OAuth with workspace email validation
- **Customer Master**: Base customer information and codes
- **Mail Destination Parent Master**: Email job configurations and sending rules
- **Mail Destination Child Master**: Individual email addresses for each job
- **Mail API Configuration**: Technical settings for mail processing
- **Access Control**: Role-based restrictions via `update_sys_div`

## Base Configuration

```bash
# Set your API base URL (replace with your actual API Gateway endpoint)
export API_BASE_URL="https://your-api-gateway-id.execute-api.region.amazonaws.com/stage"

# Set authentication token (replace with actual token)
export AUTH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# For local development (mock mode)
export AUTH_TOKEN="mock-token-for-testing"
```

## 1. Authentication & Health Check APIs

### Purpose
The authentication layer provides secure access to the NAIS system using Google OAuth with workspace email validation. Only users from approved email domains can access the system.

### Business Flow
1. **Health Check** → Verify system status
2. **Google OAuth Login** → Authenticate with Google workspace account
3. **Token Management** → Maintain session security
4. **Workspace Validation** → Ensure user belongs to approved domain

### Health Check
**Purpose**: Verify that the API is running and accessible
```bash
# Test API health - Returns system status and configuration
curl -X GET "${API_BASE_URL}/auth/health" \
  -H "Content-Type: application/json"

# Response includes:
# - API version and status
# - Workspace authentication settings
# - Allowed email domains
```

### Google OAuth Login
**Purpose**: Authenticate users through Google workspace accounts with domain validation
```bash
# Initiate Google OAuth login - Starts the authentication flow
curl -X POST "${API_BASE_URL}/auth/google/login" \
  -H "Content-Type: application/json" \
  -d '{
    "redirect_uri": "https://your-frontend.com/callback"
  }'

# Get Google login URL - Retrieves the OAuth authorization URL
curl -X GET "${API_BASE_URL}/auth/google/login"

# Response includes:
# - Google OAuth authorization URL
# - Required scope permissions
# - Workspace domain restrictions
```

### Token Management
**Purpose**: Manage authentication tokens for secure API access
```bash
# Refresh authentication token - Extends session without re-login
curl -X POST "${API_BASE_URL}/auth/token/refresh" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "refresh_token": "your-refresh-token"
  }'

# Logout - Invalidates current session
curl -X POST "${API_BASE_URL}/auth/logout" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"
```

## 2. Customer Master APIs

### Purpose
Customer Master APIs provide access to the foundational customer data that serves as the base for all mail destination configurations. Customer information includes office codes, customer codes, company names, and chain store relationships.

### Business Context
- **Foundation Layer**: All mail destination configurations reference customer master data
- **Hierarchical Structure**: Office → Customer → Chain Store relationships
- **Data Integrity**: Ensures valid customer references before creating mail destinations
- **Business Operations**: Supports customer lookup and validation workflows

### Business Flow
1. **List All Customers** → Overview of all registered customers
2. **Search Specific Customer** → Find customer by code for mail destination setup
3. **Validate Customer Codes** → Ensure valid references before creating mail jobs

### Get All Customers
**Purpose**: Retrieve complete list of customers for overview and selection
**Use Case**: Display customer dropdown lists, generate reports, data validation
```bash
# Get all customers - Returns paginated list of all customer records
curl -X GET "${API_BASE_URL}/customer" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Customer codes and office codes
# - Company names (normal_name_kanji)
# - Chain store relationships
# - Audit trail (created/updated timestamps)
```

### Get Specific Customer
**Purpose**: Retrieve detailed information for a specific customer
**Use Case**: Validate customer codes, lookup company details, verify relationships
```bash
# By office_cd-customer_cd format - Most specific lookup
curl -X GET "${API_BASE_URL}/customer/0001-0001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# By customer_cd only - Returns first match across all offices
curl -X GET "${API_BASE_URL}/customer/0001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Complete customer information
# - Office and chain store relationships
# - Company name and codes
# - Creation and update history
```

## 3. Mail Destination Parent Master APIs

### Purpose
Mail Destination Parent Master APIs manage the core email job configurations. Parent records define the technical settings, file paths, and sending parameters for email jobs. Each parent record represents a unique mail delivery configuration.

### Business Context
- **Email Job Configuration**: Defines how emails are processed and sent
- **File Management**: Specifies search directories, file patterns, and output locations
- **Delivery Settings**: Controls email subjects, body templates, and attachment handling
- **System Integration**: Coordinates with external systems through defined interfaces
- **Business Rules**: Enforces sending modes (MPDF/CMP/AUTO/CTLG/PDF) and restrictions

### Business Flow
1. **Create Mail Job** → Define new email delivery configuration
2. **Configure Parameters** → Set file paths, subjects, delivery modes
3. **Manage Lifecycle** → Update settings, soft delete when obsolete
4. **Monitor Jobs** → List and filter active/inactive configurations

### Data Relationships
```
Customer Master → Mail Destination Parent → Mail Destination Child
                                        ↓
                              File Processing & Email Delivery
```

### Get All Parent Records
**Purpose**: Retrieve list of email job configurations for monitoring and management
**Use Case**: Dashboard views, job status monitoring, configuration audits
```bash
# Get all active parent records - Overview of current mail jobs
curl -X GET "${API_BASE_URL}/mail-destination-parent" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Get parent records with filters - Targeted search for specific jobs
curl -X GET "${API_BASE_URL}/mail-destination-parent?job_id=JOB001&customer_cd=0001&delete_flag=0" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Get deleted records - Audit trail of removed configurations
curl -X GET "${API_BASE_URL}/mail-destination-parent?delete_flag=1" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Job identification (job_id, customer relationships)
# - Technical configuration (send_mode, file paths)
# - Email settings (subject, body template, attachments)
# - System controls (update_sys_div, delete_flag)
# - Audit information (created/updated by/at)
```

### Create New Parent Record
**Purpose**: Establish a new email job configuration with all technical parameters
**Use Case**: Setting up new mail delivery workflows, configuring customer-specific email jobs
```bash
# Create new mail job configuration
curl -X POST "${API_BASE_URL}/mail-destination-parent" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "job_id": "JOB001",              # Unique job identifier
    "office_cd": "0001",             # Office code (links to customer master)
    "customer_cd": "0001",           # Customer code (links to customer master)
    "chain_store_cd": "001",         # Chain store identifier
    "supplier_cd": "SUP001",         # Supplier relationship
    "order_branch_cd": "BR001",      # Order processing branch
    "extend_cd": "EXT001",           # Extension/variation code
    "destination_name": "Monthly Reports", # Human-readable description
    "send_mode": "MPDF",             # Send mode: MPDF/CMP/AUTO/CTLG/PDF
    "search_file": "*.csv",          # File pattern to search for
    "search_directory": "/data/input", # Where to look for input files
    "send_directory": "/data/output",  # Where to place processed files
    "subject": "Monthly Sales Report", # Email subject template
    "body_file_path": "/templates/monthly_report.txt", # Email body template
    "attachment_file_path": "/files/report_template.pdf", # Attachment template
    "mailing_list_id": "ML001",      # Associated mailing list
    "update_sys_div": "0",           # Access control: 0=no restrictions
    "importer_cd": "USER001"         # User who created this configuration
  }'

# Business Impact:
# - Creates new email delivery pipeline
# - Establishes file processing workflow
# - Enables automated email generation
# - Links customer data to delivery configuration
```

### Get Specific Parent Record
**Purpose**: Retrieve detailed configuration for a specific email job
**Use Case**: Job configuration review, troubleshooting, detailed configuration display
```bash
# Composite key format: job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd
curl -X GET "${API_BASE_URL}/mail-destination-parent/JOB001|0001|0001|001|SUP001|BR001|EXT001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Complete job configuration details
# - File processing parameters
# - Email template settings
# - Access control information
# - Full audit trail
```

### Update Parent Record
**Purpose**: Modify existing email job configuration parameters
**Use Case**: Updating file paths, changing email templates, adjusting delivery settings
**Access Control**: Subject to update_sys_div restrictions
```bash
# Update mail job configuration - Requires appropriate access level
curl -X PUT "${API_BASE_URL}/mail-destination-parent/JOB001|0001|0001|001|SUP001|BR001|EXT001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "destination_name": "Updated Monthly Reports", # Changed description
    "send_mode": "CMP",                    # Changed delivery mode
    "search_file": "*.xlsx",               # Updated file pattern
    "search_directory": "/data/input/v2",  # New input location
    "send_directory": "/data/output/v2",   # New output location
    "subject": "Updated Monthly Sales Report", # New email subject
    "body_file_path": "/templates/v2/monthly_report.txt", # Updated template
    "attachment_file_path": "/files/v2/report_template.pdf", # New attachment
    "mailing_list_id": "ML002",            # Changed mailing list
    "update_sys_div": "0",                 # Maintained access level
    "importer_cd": "USER002"               # Updated by different user
  }'

# Business Impact:
# - Modifies existing email workflow
# - Updates file processing behavior
# - Changes email content and delivery
# - Maintains audit trail of changes
```

### Delete Parent Record (Soft Delete)
**Purpose**: Deactivate email job configuration while preserving audit trail
**Use Case**: Discontinuing email jobs, temporary suspension, compliance requirements
**Access Control**: Subject to update_sys_div restrictions
```bash
# Soft delete - Sets delete_flag=1, preserves record for audit
curl -X DELETE "${API_BASE_URL}/mail-destination-parent/JOB001|0001|0001|001|SUP001|BR001|EXT001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Business Impact:
# - Deactivates email job processing
# - Stops automated file processing
# - Preserves configuration for audit
# - Maintains data relationships
```

## 4. Mail Destination Child Master APIs

### Purpose
Mail Destination Child Master APIs manage the individual email recipients for each email job. Child records contain the actual email addresses that will receive emails based on the parent job configuration. This creates the mailing list for each delivery job.

### Business Context
- **Recipient Management**: Stores individual email addresses for each mail job
- **Mailing Lists**: Creates recipient lists linked to parent job configurations
- **Address Validation**: Ensures valid email addresses for delivery
- **Serial Management**: Supports multiple recipients per job with unique serial numbers
- **Dynamic Lists**: Allows adding/removing recipients without changing job configuration

### Business Flow
1. **Define Parent Job** → Create email job configuration (Parent Master)
2. **Add Recipients** → Specify individual email addresses (Child Master)
3. **Manage Lists** → Add/remove recipients as needed
4. **Process Emails** → System uses both parent config + child addresses for delivery

### Data Relationships
```
Mail Destination Parent (1) → Mail Destination Child (Many)
         ↓                              ↓
   Job Configuration              Email Addresses
   (How to send)                 (Who receives)
```

### Get All Child Records
**Purpose**: Retrieve list of email recipients across all jobs or specific jobs
**Use Case**: Mailing list management, recipient audits, email address verification
```bash
# Get all active child records - Overview of all recipients
curl -X GET "${API_BASE_URL}/mail-destination-child" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Get child records with filters - Specific job recipients
curl -X GET "${API_BASE_URL}/mail-destination-child?job_id=JOB001&customer_cd=0001&delete_flag=0" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Email addresses for each job
# - Serial numbers for recipient ordering
# - Job relationship information
# - Access control settings
# - Audit trail information
```

### Create New Child Record
**Purpose**: Add a new email recipient to an existing mail job configuration
**Use Case**: Expanding mailing lists, adding new subscribers, building recipient databases
**Prerequisite**: Parent record must exist with matching composite key (excluding serial_number)
```bash
# Add new recipient to existing mail job
curl -X POST "${API_BASE_URL}/mail-destination-child" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "job_id": "JOB001",              # Must match existing parent record
    "office_cd": "0001",             # Must match existing parent record
    "customer_cd": "0001",           # Must match existing parent record
    "chain_store_cd": "001",         # Must match existing parent record
    "supplier_cd": "SUP001",         # Must match existing parent record
    "order_branch_cd": "BR001",      # Must match existing parent record
    "extend_cd": "EXT001",           # Must match existing parent record
    "serial_number": "001",          # Unique within this job (sequential)
    "email_address": "manager@customer.com", # Actual recipient email
    "update_sys_div": "0",           # Access control level
    "importer_cd": "USER001"         # User adding this recipient
  }'

# Business Impact:
# - Adds recipient to existing email delivery job
# - Expands mailing list for automated emails
# - Enables targeted communication to specific addresses
# - Maintains relationship to parent job configuration
```

### Get Specific Child Record
```bash
# Composite key format: job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd|serial_number
curl -X GET "${API_BASE_URL}/mail-destination-child/JOB001|0001|0001|001|SUP001|BR001|EXT001|001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"
```

### Update Child Record
```bash
curl -X PUT "${API_BASE_URL}/mail-destination-child/JOB001|0001|0001|001|SUP001|BR001|EXT001|001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "email_address": "updated@example.com",
    "update_sys_div": "MANUAL",
    "importer_cd": "USER002"
  }'
```

### Delete Child Record (Soft Delete)
```bash
curl -X DELETE "${API_BASE_URL}/mail-destination-child/JOB001|0001|0001|001|SUP001|BR001|EXT001|001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"
```

## 5. Mail To Child Master APIs

### Purpose
Mail To Child Master APIs provide access to processed mailing lists that have been generated from the parent and child destination configurations. These represent the final recipient lists used for actual email delivery.

### Business Context
- **Processed Mailing Lists**: Final recipient lists ready for email delivery
- **Delivery Status**: Track which recipients are associated with which delivery jobs
- **Historical Data**: Maintain records of past mailing list configurations
- **Integration Point**: Interface with external email delivery systems

### Get All Mail To Child Records
**Purpose**: Retrieve all processed mailing list entries for monitoring and auditing
**Use Case**: Delivery monitoring, audit trails, system integration verification
```bash
# Get all mail-to-child records - Overview of processed mailing lists
curl -X GET "${API_BASE_URL}/mail-to-child" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Mailing list identifiers
# - Associated email addresses
# - Processing status information
# - Integration timestamps
```

### Get Mail To Child by Mailing List ID
**Purpose**: Retrieve specific mailing list details for a particular delivery job
**Use Case**: Troubleshooting specific deliveries, verifying recipient lists
```bash
# Get specific mailing list details
curl -X GET "${API_BASE_URL}/mail-to-child/ML001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Complete recipient list for this mailing list
# - Email addresses and delivery parameters
# - Associated job configurations
```

## 6. Mail API Config Master APIs

### Purpose
Mail API Config Master APIs manage the technical configuration settings for email processing APIs. These configurations control how the system interfaces with external email services and processing parameters.

### Business Context
- **API Integration**: Configuration for external email service APIs
- **Processing Parameters**: Technical settings for email generation and delivery
- **System Coordination**: Settings that coordinate between different system components
- **Environment Management**: Configuration that varies between development, staging, and production

### Get All Mail API Config Records
**Purpose**: Retrieve all API configuration settings for system administration
**Use Case**: System configuration review, environment setup, troubleshooting integration issues
```bash
# Get all API configuration records
curl -X GET "${API_BASE_URL}/mail-api-config" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - API endpoint configurations
# - Processing parameters
# - Integration settings
# - Environment-specific configurations
```

### Get Mail API Config by Job ID
**Purpose**: Retrieve API configuration specific to a particular job or process
**Use Case**: Job-specific troubleshooting, configuration validation, integration testing
```bash
# Get configuration for specific job
curl -X GET "${API_BASE_URL}/mail-api-config/JOB001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - Job-specific API settings
# - Processing parameters for this job
# - Integration endpoints and credentials
```

## 7. Workspace Management APIs (Admin Access)

### Purpose
Workspace Management APIs provide administrative control over the authentication and access control system. These APIs allow administrators to manage workspace domains and security settings.

### Business Context
- **Security Administration**: Control which email domains can access the system
- **Workspace Management**: Manage Google Workspace integration settings
- **Access Control**: Administrative oversight of user access permissions
- **Compliance**: Ensure only authorized domains have system access

### Get Allowed Email Domains
**Purpose**: Retrieve list of email domains authorized to access the system
**Use Case**: Security audits, domain management, access control verification
**Access Level**: Administrative access required
```bash
# Get workspace domain configuration (admin only)
curl -X GET "${API_BASE_URL}/auth/workspace/domains" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Response includes:
# - List of allowed email domains
# - Workspace authentication settings
# - Domain validation rules
# - Administrative configuration
```

## Complete Business Workflow Example

### Typical Email Campaign Setup Flow

This section demonstrates how to use the APIs together to set up a complete email delivery campaign.

#### Step 1: Authenticate and Verify System
```bash
# 1. Check system health and configuration
curl -X GET "${API_BASE_URL}/auth/health"

# 2. Authenticate with Google OAuth (get token from OAuth flow)
export AUTH_TOKEN="your-actual-token-here"

# 3. Verify allowed domains (if admin)
curl -X GET "${API_BASE_URL}/auth/workspace/domains" -H "X-Auth-Token: ${AUTH_TOKEN}"
```

#### Step 2: Validate Customer Data
```bash
# 4. Find and validate customer information
curl -X GET "${API_BASE_URL}/customer/0001-0001" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Verify the customer exists and get the correct codes for:
# - office_cd: "0001"
# - customer_cd: "0001" 
# - chain_store_cd: "001"
```

#### Step 3: Create Email Job Configuration (Parent)
```bash
# 5. Create mail destination parent record (email job configuration)
curl -X POST "${API_BASE_URL}/mail-destination-parent" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "job_id": "MONTHLY_REPORT_001",
    "office_cd": "0001",
    "customer_cd": "0001", 
    "chain_store_cd": "001",
    "supplier_cd": "SUP001",
    "order_branch_cd": "BR001",
    "extend_cd": "EXT001",
    "destination_name": "Monthly Sales Report Campaign",
    "send_mode": "MPDF",
    "search_file": "monthly_sales_*.csv",
    "search_directory": "/data/reports/input",
    "send_directory": "/data/reports/output",
    "subject": "Monthly Sales Report - {{MONTH}} {{YEAR}}",
    "body_file_path": "/templates/monthly_report_body.html",
    "attachment_file_path": "/templates/sales_report_template.pdf",
    "mailing_list_id": "ML_MONTHLY_SALES",
    "update_sys_div": "0",
    "importer_cd": "ADMIN001"
  }'
```

#### Step 4: Add Email Recipients (Children)
```bash
# 6. Add first recipient to the email job
curl -X POST "${API_BASE_URL}/mail-destination-child" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "job_id": "MONTHLY_REPORT_001",
    "office_cd": "0001",
    "customer_cd": "0001",
    "chain_store_cd": "001", 
    "supplier_cd": "SUP001",
    "order_branch_cd": "BR001",
    "extend_cd": "EXT001",
    "serial_number": "001",
    "email_address": "manager@customer.com",
    "update_sys_div": "0",
    "importer_cd": "ADMIN001"
  }'

# 7. Add second recipient
curl -X POST "${API_BASE_URL}/mail-destination-child" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "job_id": "MONTHLY_REPORT_001",
    "office_cd": "0001",
    "customer_cd": "0001",
    "chain_store_cd": "001",
    "supplier_cd": "SUP001", 
    "order_branch_cd": "BR001",
    "extend_cd": "EXT001",
    "serial_number": "002",
    "email_address": "accounting@customer.com",
    "update_sys_div": "0",
    "importer_cd": "ADMIN001"
  }'
```

#### Step 5: Verify Configuration
```bash
# 8. Verify parent configuration was created correctly
curl -X GET "${API_BASE_URL}/mail-destination-parent/MONTHLY_REPORT_001|0001|0001|001|SUP001|BR001|EXT001" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# 9. Verify all recipients were added
curl -X GET "${API_BASE_URL}/mail-destination-child?job_id=MONTHLY_REPORT_001" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# 10. Check processed mailing list
curl -X GET "${API_BASE_URL}/mail-to-child/ML_MONTHLY_SALES" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"
```

#### Step 6: Monitor and Maintain
```bash
# 11. Update job configuration if needed
curl -X PUT "${API_BASE_URL}/mail-destination-parent/MONTHLY_REPORT_001|0001|0001|001|SUP001|BR001|EXT001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{"subject": "Updated Monthly Sales Report - {{MONTH}} {{YEAR}}"}'

# 12. Add new recipient if needed  
curl -X POST "${API_BASE_URL}/mail-destination-child" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "job_id": "MONTHLY_REPORT_001",
    "office_cd": "0001",
    "customer_cd": "0001", 
    "chain_store_cd": "001",
    "supplier_cd": "SUP001",
    "order_branch_cd": "BR001", 
    "extend_cd": "EXT001",
    "serial_number": "003",
    "email_address": "director@customer.com",
    "update_sys_div": "0",
    "importer_cd": "ADMIN001"
  }'

# 13. Remove recipient if needed (soft delete)
curl -X DELETE "${API_BASE_URL}/mail-destination-child/MONTHLY_REPORT_001|0001|0001|001|SUP001|BR001|EXT001|002" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"
```

### Business Impact of This Workflow
- **Automated Email Campaigns**: System will automatically process files and send emails
- **Targeted Communication**: Specific recipients receive relevant business communications  
- **Audit Trail**: Complete history of who configured what and when
- **Scalable Management**: Easy to add/remove recipients or modify job parameters
- **Integration Ready**: Configuration supports external system integration
- **Compliance**: Access controls ensure proper authorization for changes

## Complete API Endpoint Summary

### Authentication & Health
- `GET /auth/health` - Health check
- `POST /auth/google/login` - Initiate Google OAuth
- `GET /auth/google/login` - Get Google login URL
- `GET /auth/google/callback` - OAuth callback
- `POST /auth/token/refresh` - Refresh token
- `POST /auth/logout` - Logout
- `GET /auth/workspace/domains` - Get workspace domains (admin)

### Customer Master
- `GET /customer` - Get all customers
- `GET /customer/{code}` - Get specific customer

### Mail Destination Parent Master
- `GET /mail-destination-parent` - Get all parent records
- `POST /mail-destination-parent` - Create parent record
- `GET /mail-destination-parent/{key}` - Get specific parent record
- `PUT /mail-destination-parent/{key}` - Update parent record
- `DELETE /mail-destination-parent/{key}` - Delete parent record

### Mail Destination Child Master
- `GET /mail-destination-child` - Get all child records
- `POST /mail-destination-child` - Create child record
- `GET /mail-destination-child/{key}` - Get specific child record
- `PUT /mail-destination-child/{key}` - Update child record
- `DELETE /mail-destination-child/{key}` - Delete child record

### Mail To Child Master
- `GET /mail-to-child` - Get all mail-to-child records
- `GET /mail-to-child/{mailing_list_id}` - Get by mailing list ID

### Mail API Config Master
- `GET /mail-api-config` - Get all config records
- `GET /mail-api-config/{job_id}` - Get config by job ID

## Access Control (update_sys_div)

### Overview
The Mail Destination Master APIs implement access control based on the `update_sys_div` field value:

- **0**: 制限なし (No restrictions) - All systems can modify
- **1**: 他システム連携のみ (Other system integration only) - Only other systems can modify  
- **2**: 宛先サービスのみ (Destination service only) - Only destination service can modify

### System Identification
The calling system is determined by:
1. Environment variable `CALLING_SYSTEM`
2. HTTP header `X-Calling-System` (future enhancement)
3. JWT token claims (future enhancement)

### Testing Access Control
```bash
# Set calling system type for testing
export CALLING_SYSTEM="API_SERVICE"      # Default API service
export CALLING_SYSTEM="OTHER_SYSTEM"     # Other system integration
export CALLING_SYSTEM="DESTINATION_SERVICE"  # Destination service

# Test updating record with update_sys_div=1 (should fail for API_SERVICE)
curl -X PUT "${API_BASE_URL}/mail-destination-parent/JOB001|0001|0001|001|SUP001|BR001|EXT001" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{
    "destination_name": "Updated Name",
    "update_sys_div": "1"
  }'

# Expected response for unauthorized access:
# {
#   "error": "Forbidden",
#   "message": "Update operation not allowed for update_sys_div: 1. This record can only be modified by other system integration only (他システム連携のみ)"
# }
```

### Access Control Error Responses
- **403 Forbidden**: When trying to modify records with incompatible `update_sys_div`
- Error message includes the current restriction and allowed system type

## Testing Tips

### 1. Environment Setup
```bash
# For local testing
export API_BASE_URL="http://localhost:3000"
export AUTH_TOKEN="mock-token"

# For staging
export API_BASE_URL="https://staging-api.nais.com/dev"
export AUTH_TOKEN="$(aws cognito-idp admin-initiate-auth ...)"

# For production
export API_BASE_URL="https://api.nais.com/prod"
export AUTH_TOKEN="your-production-token"
```

### 2. Response Format
All APIs return JSON responses in this format:
```json
{
  "status": "success|error",
  "data": {...},
  "count": 123,
  "message": "Optional message",
  "error": "Error details if applicable"
}
```

### 3. Error Handling
- `400` - Bad Request (invalid input)
- `401` - Unauthorized (invalid/missing token)
- `404` - Not Found (resource doesn't exist)
- `409` - Conflict (duplicate key)
- `500` - Internal Server Error

### 4. Composite Key Format
- **Parent**: `job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd`
- **Child**: `job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd|serial_number`

### 5. Query Parameters
- `job_id` - Filter by job ID
- `customer_cd` - Filter by customer code
- `delete_flag` - Filter by deletion status (0=active, 1=deleted)

## Practical CRUD Examples with Real Data

This section provides complete, ready-to-use curl examples based on actual database records for comprehensive testing of all CRUD operations.

### Mail Destination Parent CRUD Examples

#### Create New Parent Record
**Based on existing customer data (OFC3/CUST)**
```bash
# Create new quarterly report job
curl -k -X POST \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-parent" \
  -d '{
    "job_id": "JOB004",
    "office_cd": "OFC3",
    "customer_cd": "CUST",
    "chain_store_cd": "CHNR",
    "supplier_cd": "SUPP",
    "order_branch_cd": "03",
    "extend_cd": "EXT004    ",
    "destination_name": "Quarterly Report",
    "send_mode": "AUTO",
    "search_file": "*.docx",
    "search_directory": "/data/quarterly/",
    "send_directory": "/data/send/",
    "subject": "Quarterly Business Report - %DATE%",
    "body_file_path": "/templates/quarterly_report.html",
    "attachment_file_path": "/attachments/quarterly_%DATE%.docx",
    "mailing_list_id": "ML004",
    "update_sys_div": "1",
    "importer_cd": "IMP03",
    "delete_flag": "0"
  }'
```

#### Update Existing Parent Record
**Update JOB001 with new configuration**
```bash
# Update existing daily sales report job
# Composite key: JOB001|OFC1|CUST|CHNR|SUPP|01|EXT001    
# Note: The handler now properly handles URL decoding and trailing spaces
curl -k -X PUT \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-parent/JOB001%7COFC1%7CCUST%7CCHNR%7CSUPP%7C01%7CEXT001%20%20%20%20" \
  -d '{
    "destination_name": "Updated Daily Sales Report",
    "send_mode": "MANU",
    "search_file": "*.xlsx",
    "search_directory": "/data/reports/updated/",
    "send_directory": "/data/send/updated/",
    "subject": "Updated Daily Sales Report - %DATE%",
    "body_file_path": "/templates/updated_daily_report.html",
    "attachment_file_path": "/attachments/updated_sales_%DATE%.xlsx",
    "mailing_list_id": "ML001",
    "update_sys_div": "0",
    "importer_cd": "IMP01",
    "delete_flag": "0"
  }'

# Alternative format using pipe characters (also works with improved parsing):
curl -k -X PUT \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-parent/JOB001|OFC1|CUST|CHNR|SUPP|01|EXT001    " \
  -d '{
    "destination_name": "Updated Daily Sales Report v2",
    "send_mode": "AUTO",
    "update_sys_div": "0",
    "importer_cd": "IMP01"
  }'
```

#### Delete Parent Record (Soft Delete)
**Soft delete JOB002 (sets delete_flag=1)**
```bash
# Soft delete weekly summary job
# Composite key: JOB002|OFC2|CUST|CHNR|SUPP|02|EXT002    
curl -k -X DELETE \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-parent/JOB002%7COFC2%7CCUST%7CCHNR%7CSUPP%7C02%7CEXT002%20%20%20%20"
```

### Mail Destination Child CRUD Examples

#### Create New Child Records
**Add recipients to ML004 mailing list**
```bash
# Create first recipient for quarterly reports
curl -k -X POST \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-child" \
  -d '{
    "mailing_list_id": "ML004",
    "destination_seq": 1,
    "destination_address": "executive@company.com",
    "destination_note": "Executive Team",
    "status_div": "1",
    "importer_cd": "IMP03"
  }'

# Create second recipient for quarterly reports
curl -k -X POST \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-child" \
  -d '{
    "mailing_list_id": "ML004",
    "destination_seq": 2,
    "destination_address": "finance@company.com",
    "destination_note": "Finance Team",
    "status_div": "1",
    "importer_cd": "IMP03"
  }'

# Create third recipient for quarterly reports
curl -k -X POST \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-child" \
  -d '{
    "mailing_list_id": "ML004",
    "destination_seq": 3,
    "destination_address": "audit@company.com",
    "destination_note": "Audit Department",
    "status_div": "1", 
    "importer_cd": "IMP03"
  }'
```

#### Update Existing Child Record
**Update email address for ML001 recipient**
```bash
# Update sales manager email address
# Composite key: ML001|1
curl -k -X PUT \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-child/ML001%7C1" \
  -d '{
    "destination_address": "updated.manager@company.com",
    "destination_note": "Updated Sales Manager",
    "status_div": "1",
    "importer_cd": "IMP01"
  }'
```

#### Delete Child Record (Soft Delete)
**Soft delete ML002 recipient (sets status_div=2)**
```bash
# Soft delete CFO from weekly summary list
# Composite key: ML002|2
curl -k -X DELETE \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-child/ML002%7C2"
```

### Complete Workflow: Setting Up New Email Campaign

**Step 1: Verify customer exists**
```bash
# Check if customer OFC3/CUST exists
curl -X GET "${API_BASE_URL}/customer/OFC3-CUST" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"
```

**Step 2: Create parent job configuration**
```bash
# Create the parent record (use the CREATE example above)
curl -k -X POST "${API_BASE_URL}/mail-destination-parent" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  -d '{ ... }' # (Full payload from CREATE example)
```

**Step 3: Add recipients to mailing list**
```bash
# Add recipients (use the child CREATE examples above)
# Execute all three POST requests for ML004
```

**Step 4: Verify configuration**
```bash
# Check parent record was created
curl -X GET "${API_BASE_URL}/mail-destination-parent/JOB004%7COFC3%7CCUST%7CCHNR%7CSUPP%7C03%7CEXT004%20%20%20%20" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"

# Check child records were created
curl -X GET "${API_BASE_URL}/mail-destination-child?mailing_list_id=ML004" \
  -H "X-Auth-Token: ${AUTH_TOKEN}"
```

### Key Points for Testing

**URL Encoding:**
- Pipe characters `|` → `%7C`
- Spaces in extend_cd → `%20`

**Composite Keys:**
- **Parent**: `job_id|office_cd|customer_cd|chain_store_cd|supplier_cd|order_branch_cd|extend_cd`
- **Child**: `mailing_list_id|destination_seq`

**Status Values:**
- **Parent delete_flag**: `0`=active, `1`=deleted
- **Child status_div**: `0`=inactive, `1`=active, `2`=deleted

**Access Control:**
- `update_sys_div=1` means only other system integration can modify
- Check `update_sys_div` value before attempting updates/deletes

**Data References:**
- Child records must reference existing parent `mailing_list_id`
- Parent records must reference existing customer codes

## Troubleshooting Common Issues

### Composite Key Format Errors

**Problem**: "Invalid composite key format" error when calling UPDATE/DELETE operations

**Root Cause**: The extend_cd field is defined as CHAR(10) in the database, which means it's padded with trailing spaces. URL encoding and parsing issues can cause the composite key to be incorrectly formatted.

**Solution**: The Java handlers have been updated to properly handle:
1. **URL Decoding**: Automatically decode URL-encoded characters (`%7C` → `|`, `%20` → space)
2. **Trailing Spaces**: Use `split("\\|", -1)` to preserve empty parts and trailing spaces
3. **Enhanced Logging**: Log all key parts with lengths for debugging

**Working Examples**:
```bash
# Both formats now work correctly:

# Format 1: URL encoded (recommended for production)
curl -X PUT "${API_BASE_URL}/mail-destination-parent/JOB001%7COFC1%7CCUST%7CCHNR%7CSUPP%7C01%7CEXT001%20%20%20%20"

# Format 2: Raw format (works in most terminals)
curl -X PUT "${API_BASE_URL}/mail-destination-parent/JOB001|OFC1|CUST|CHNR|SUPP|01|EXT001    "
```

### Access Control Errors

**Problem**: "Update operation not allowed for update_sys_div: 1" error

**Root Cause**: Records with `update_sys_div=1` can only be modified by specific system types.

**Solution**: 
1. Create test records with `update_sys_div=0` (no restrictions)
2. Set appropriate `CALLING_SYSTEM` environment variable if needed
3. Check the current `update_sys_div` value before attempting updates

### Database Connection Issues

**Problem**: "column 'inputter_cd' does not exist" error

**Root Cause**: Code was using incorrect column name.

**Solution**: All handlers have been updated to use `importer_cd` instead of `inputter_cd`.

### Testing the Composite Key Fix

**Verify the fix works by testing UPDATE operation**:
```bash
# Step 1: Create a test record with update_sys_div=0 (no restrictions)
curl -k -X POST \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-parent" \
  -d '{
    "job_id": "TEST001",
    "office_cd": "OFC1",
    "customer_cd": "CUST",
    "chain_store_cd": "CHNR",
    "supplier_cd": "SUPP",
    "order_branch_cd": "01",
    "extend_cd": "TESTKEY   ",
    "destination_name": "Test Record",
    "send_mode": "AUTO",
    "search_file": "*.pdf",
    "search_directory": "/test/",
    "send_directory": "/test/out/",
    "subject": "Test Subject",
    "body_file_path": "/test/body.txt",
    "attachment_file_path": "/test/attach.pdf",
    "mailing_list_id": "ML001",
    "update_sys_div": "0",
    "importer_cd": "TEST"
  }'

# Step 2: Test UPDATE with the composite key (should now work)
curl -k -X PUT \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: ${AUTH_TOKEN}" \
  "${API_BASE_URL}/mail-destination-parent/TEST001|OFC1|CUST|CHNR|SUPP|01|TESTKEY   " \
  -d '{
    "destination_name": "Updated Test Record",
    "send_mode": "MANU",
    "update_sys_div": "0",
    "importer_cd": "TEST_UPD"
  }'

# Expected: 200 OK with success message (no more "Invalid composite key format" error)
```

## Batch Testing Script

Create a bash script to test multiple endpoints:

```bash
#!/bin/bash
# test_apis.sh

source ./config.sh  # Contains API_BASE_URL and AUTH_TOKEN

echo "Testing NAIS APIs..."

# Test health check
echo "1. Health Check"
curl -s "${API_BASE_URL}/auth/health" | jq .

# Test customer APIs
echo "2. Customer APIs"
curl -s "${API_BASE_URL}/customer" -H "X-Auth-Token: ${AUTH_TOKEN}" | jq '.count'

# Test mail destination parent APIs
echo "3. Mail Destination Parent APIs"
curl -s "${API_BASE_URL}/mail-destination-parent" -H "X-Auth-Token: ${AUTH_TOKEN}" | jq '.count'

# Test mail destination child APIs
echo "4. Mail Destination Child APIs"
curl -s "${API_BASE_URL}/mail-destination-child" -H "X-Auth-Token: ${AUTH_TOKEN}" | jq '.count'

echo "API testing completed!"
```

## Database Schema Reference

### mail_destination_parent_mst
- Primary Key: `job_id, office_cd, customer_cd, chain_store_cd, supplier_cd, order_branch_cd, extend_cd`
- Soft Delete: `delete_flag` (0=active, 1=deleted)

### mail_destination_child_mst  
- Primary Key: `job_id, office_cd, customer_cd, chain_store_cd, supplier_cd, order_branch_cd, extend_cd, serial_number`
- Soft Delete: `delete_flag` (0=active, 1=deleted)
- Email: `email_address` field contains the actual email addresses