#!/bin/bash
# Run this ON YOUR LOCAL MACHINE after each new KodeKloud session.
# Update EC2_IP and ECR_REGISTRY below with fresh values from AWS console / Terraform output.
set -e
EC2_IP="100.30.178.177"
KEY="${HOME}/.ssh/event-booking-key.pem"
APP_DIR="/home/ec2-user/app"

echo "=== Creating app directory on EC2 ==="
ssh -i "$KEY" -o StrictHostKeyChecking=no "ec2-user@${EC2_IP}" "mkdir -p ${APP_DIR}"

echo "=== Copying docker-compose.prod.yml to EC2 ==="
scp -i "$KEY" -o StrictHostKeyChecking=no docker-compose.prod.yml "ec2-user@${EC2_IP}:${APP_DIR}/"

echo "=== Copying serviceAccountKey (if present) ==="
if [ -f user/src/main/resources/serviceAccountKey.json ]; then
  scp -i "$KEY" -o StrictHostKeyChecking=no user/src/main/resources/serviceAccountKey.json "ec2-user@${EC2_IP}:${APP_DIR}/"
else
  echo "No serviceAccountKey.json found - create empty one on EC2 for app to start"
fi

echo "=== Setting env and starting on EC2 ==="
ssh -i "$KEY" -o StrictHostKeyChecking=no "ec2-user@${EC2_IP}" << 'REMOTE'
mkdir -p /home/ec2-user/app && cd /home/ec2-user/app

# NOTE: Update ECR_REGISTRY with your current session's account ID from Terraform output
export ECR_REGISTRY=975050354644.dkr.ecr.eu-west-2.amazonaws.com
export AWS_REGION=eu-west-2
export IMAGE_TAG=latest
export S3_BUCKET_NAME=event-booking-images-prod
export SQS_QUEUE_URL=https://sqs.eu-west-2.amazonaws.com/975050354644/event-booking-ticket-updates
export GOOGLE_MAPS_API_KEY=$(aws ssm get-parameter --name /event-booking/prod/google-maps-api-key --with-decryption --query Parameter.Value --output text 2>/dev/null || echo "")

# Create minimal serviceAccountKey if missing
[ -f serviceAccountKey.json ] || echo '{}' > serviceAccountKey.json

aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin $ECR_REGISTRY
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
echo "Containers starting. Wait ~60s then open app URL from your machine."
REMOTE

echo ""
echo "=== Deploy finished. Open http://${EC2_IP} in your browser (wait ~60s for containers to start). ==="
