# GitHub Actions Deployment Setup

This directory contains GitHub Actions workflows for automated deployment of the NAIS authentication API.

## Workflow: `deploy-api.yml`

### Trigger Conditions
- **Push to main branch** with changes in the `api/` directory
- **Manual dispatch** via GitHub Actions UI

### Required GitHub Secrets

You must configure the following secrets in your GitHub repository settings (`Settings > Secrets and variables > Actions`):

#### AWS Configuration
- `AWS_ACCESS_KEY_ID` - AWS access key for deployment
- `AWS_SECRET_ACCESS_KEY` - AWS secret key for deployment

#### AWS Infrastructure
- `VPC_ID` - VPC ID where resources will be deployed
- `SUBNET_ID_1` - First private subnet ID
- `SUBNET_ID_2` - Second private subnet ID
- `API_GATEWAY_VPC_ENDPOINT_ID` - VPC endpoint for API Gateway

#### Cognito Configuration
- `COGNITO_USER_POOL_ID` - Cognito User Pool ID
- `COGNITO_USER_POOL_CLIENT_ID` - Cognito App Client ID
- `COGNITO_USER_POOL_CLIENT_SECRET` - Cognito App Client Secret

#### Google OAuth Configuration
- `GOOGLE_CLIENT_ID` - Google OAuth Client ID
- `GOOGLE_CLIENT_SECRET` - Google OAuth Client Secret

### Optional GitHub Secrets (with defaults)

- `STAGE` - Deployment stage (default: 'dev')
- `ALLOWED_EMAIL_DOMAINS` - Comma-separated allowed domains (default: 'nais.com,company.com')
- `ALLOWED_EMAILS` - Comma-separated specific allowed emails (default: '')
- `WORKSPACE_AUTH_STRICT` - Enable strict workspace auth (default: 'true')
- `COGNITO_DOMAIN_URL` - Cognito domain URL (default: 'https://nais-stage.auth.ap-northeast-1.amazoncognito.com')
- `DB_HOST` - Database host (default: 'localhost')
- `DB_PORT` - Database port (default: '5432')
- `DB_NAME` - Database name (default: 'nais')
- `DB_USER` - Database username (default: 'postgres')
- `DB_PASSWORD` - Database password (default: 'password')
- `FRONTEND_URL` - Frontend application URL (default: 'https://vzxbmw2j9r.ap-northeast-1.awsapprunner.com')

## Setup Instructions

### 1. Create IAM User for GitHub Actions
Create an IAM user with the following policies:
- `AWSCloudFormationFullAccess`
- `IAMFullAccess`
- `AWSLambdaFullAccess`
- `AmazonAPIGatewayAdministrator`
- `SecretsManagerReadWrite`
- `AmazonS3FullAccess`
- `CloudWatchLogsFullAccess`

### 2. Configure GitHub Secrets
1. Go to your repository settings
2. Navigate to `Secrets and variables > Actions`
3. Add all required secrets listed above

### 3. Verify AWS Resources
Ensure the following AWS resources exist:
- VPC with private subnets
- API Gateway VPC endpoint
- Cognito User Pool and App Client
- Google OAuth credentials configured

## Workflow Features

- **Path-based triggering**: Only runs when `api/` directory changes
- **Java 11 setup**: Configures correct Java runtime for Lambda
- **Maven caching**: Speeds up builds by caching dependencies
- **Container builds**: Uses SAM container builds for consistency
- **Automatic S3 bucket**: SAM automatically creates and manages deployment bucket
- **Health check**: Tests API endpoint after deployment
- **Error handling**: Graceful failure handling with informative messages

## Manual Deployment

To trigger deployment manually:
1. Go to `Actions` tab in your repository
2. Select `Deploy API to AWS` workflow
3. Click `Run workflow` button
4. Choose the branch and click `Run workflow`

## Monitoring

Check deployment status:
- **GitHub Actions**: Monitor workflow runs in the Actions tab
- **AWS CloudFormation**: Check stack deployment status
- **CloudWatch Logs**: Monitor Lambda function logs

## Troubleshooting

Common issues:
1. **Missing secrets**: Ensure all required secrets are configured
2. **S3 bucket access**: Verify SAM deployment bucket exists and is accessible
3. **IAM permissions**: Ensure GitHub Actions user has sufficient permissions
4. **VPC configuration**: Verify VPC and subnet IDs are correct
5. **Resource limits**: Check AWS service limits if deployment fails