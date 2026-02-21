#!/bin/bash
# Fully dynamic deploy script — no hardcoded account IDs, regions, or IPs.
# Prerequisites: AWS CLI configured, Terraform already applied, .pem key in ~/.ssh/
set -e

echo "=== Fetching values dynamically from AWS and Terraform ==="

# Derive account ID and region from the active AWS session — no hardcoding needed
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION=$(aws configure get region)
ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Derive EC2 IP from Terraform output — no hardcoding needed
EC2_IP=$(terraform -chdir=terraform output -raw ec2_public_ip 2>/dev/null)
if [ -z "$EC2_IP" ]; then
  echo "ERROR: Could not get EC2 IP from Terraform output. Run 'terraform apply' first."
  exit 1
fi

KEY="${HOME}/.ssh/event-booking-key.pem"
APP_DIR="/home/ec2-user/app"

echo "  Account ID : ${ACCOUNT_ID}"
echo "  Region     : ${AWS_REGION}"
echo "  ECR        : ${ECR_REGISTRY}"
echo "  EC2 IP     : ${EC2_IP}"
echo ""

echo "=== Creating app directory on EC2 ==="
ssh -i "$KEY" -o StrictHostKeyChecking=no "ec2-user@${EC2_IP}" "mkdir -p ${APP_DIR}"

echo "=== Copying docker-compose.prod.yml to EC2 ==="
scp -i "$KEY" -o StrictHostKeyChecking=no docker-compose.prod.yml "ec2-user@${EC2_IP}:${APP_DIR}/"

echo "=== Copying serviceAccountKey (if present) ==="
if [ -f user/src/main/resources/serviceAccountKey.json ]; then
  scp -i "$KEY" -o StrictHostKeyChecking=no user/src/main/resources/serviceAccountKey.json "ec2-user@${EC2_IP}:${APP_DIR}/"
else
  echo "No serviceAccountKey.json found — will create empty placeholder on EC2"
fi

echo "=== Logging into ECR and deploying on EC2 ==="
ssh -i "$KEY" -o StrictHostKeyChecking=no "ec2-user@${EC2_IP}" bash <<REMOTE
set -e
cd ${APP_DIR}

# All values passed dynamically from local machine — nothing hardcoded on EC2
export ECR_REGISTRY=${ECR_REGISTRY}
export AWS_REGION=${AWS_REGION}
export IMAGE_TAG=latest
export S3_BUCKET_NAME=event-booking-images-prod
export SQS_QUEUE_URL=https://sqs.${AWS_REGION}.amazonaws.com/${ACCOUNT_ID}/event-booking-ticket-updates
export GOOGLE_MAPS_API_KEY=\$(aws ssm get-parameter --name /event-booking/prod/google-maps-api-key --with-decryption --query Parameter.Value --output text 2>/dev/null || echo "")

# Create minimal serviceAccountKey if missing
[ -f serviceAccountKey.json ] || echo '{}' > serviceAccountKey.json

aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
echo "Containers starting..."
REMOTE

echo ""
echo "=== Deploy finished. Open http://${EC2_IP} in your browser (wait ~60s). ==="
