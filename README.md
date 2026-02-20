# Event Booking System - Cloud-Native Microservices on AWS

A full-stack event booking application built with microservices architecture, deployed on AWS using Infrastructure as Code (Terraform), CI/CD pipelines (GitHub Actions), and containerization (Docker).

![Architecture](EventBookingSystemFlow.png)

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Frontend** | React, React Router, Bootstrap, Axios |
| **Backend** | Spring Boot 3.4, Java 21, Maven |
| **Database** | MongoDB |
| **Authentication** | Firebase Auth |
| **Containerization** | Docker, Docker Compose |
| **IaC** | Terraform (modular) |
| **CI/CD** | GitHub Actions |
| **Cloud (AWS)** | EC2, ECR, S3, IAM, SSM, CloudWatch, SNS, VPC |
| **Security** | Trivy image scanning, SSM secrets, least-privilege IAM |
| **External APIs** | Google Maps, Frankfurter (currency), Wikipedia |

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                        AWS Cloud                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │                  VPC (10.0.0.0/16)               │    │
│  │  ┌─────────────────────────────────────────┐    │    │
│  │  │          Public Subnet                   │    │    │
│  │  │  ┌─────────────────────────────────┐    │    │    │
│  │  │  │      EC2 (t3.medium)            │    │    │    │
│  │  │  │  ┌─────────┐  ┌─────────────┐  │    │    │    │
│  │  │  │  │ Frontend │  │ User Svc    │  │    │    │    │
│  │  │  │  │ (Nginx)  │  │ (Spring)    │  │    │    │    │
│  │  │  │  ├─────────┤  ├─────────────┤  │    │    │    │
│  │  │  │  │ Event   │  │ Booking Svc │  │    │    │    │
│  │  │  │  │ Service  │  │ (Spring)    │  │    │    │    │
│  │  │  │  ├─────────┤  ├─────────────┤  │    │    │    │
│  │  │  │  │External │  │  MongoDB    │  │    │    │    │
│  │  │  │  │API Svc  │  │             │  │    │    │    │
│  │  │  │  └─────────┘  └─────────────┘  │    │    │    │
│  │  │  └─────────────────────────────────┘    │    │    │
│  │  └─────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────┘    │
│                                                          │
│  ECR ──── S3 ──── SSM ──── CloudWatch ──── SNS          │
└─────────────────────────────────────────────────────────┘
         ▲                        │
         │ Push Images            │ Alerts
    GitHub Actions CI/CD     Email Notification
         ▲
         │
    Git Push (main)
```

## Microservices

| Service | Port | Description |
|---------|------|-------------|
| **User Service** | 9090 | User registration, authentication, profile management |
| **Event Service** | 9091 | Event CRUD, search by type/city/date, image management |
| **Booking Service** | 9092 | Ticket booking, cancellation, booking history |
| **External API Service** | 9093 | Currency conversion, Google Maps, city info (Wikipedia) |
| **Frontend** | 80 | React SPA served via Nginx reverse proxy |
| **MongoDB** | 27017 | Shared NoSQL database |

## AWS Services Used (9)

| Service | Purpose |
|---------|---------|
| **VPC** | Isolated network with public subnet, internet gateway, security groups |
| **EC2** | Application host running Docker containers |
| **ECR** | Private Docker image registry (5 repositories) |
| **S3** | Event image storage + Terraform remote state |
| **IAM** | Least-privilege EC2 instance role (ECR, S3, SSM, CloudWatch access) |
| **SSM Parameter Store** | Encrypted secrets management (Firebase keys, API keys) |
| **CloudWatch** | Centralized logging, metrics, CPU/status alarms, dashboard |
| **SNS** | Email notifications for CloudWatch alarms |
| **Security Groups** | Network-level access control (HTTP, HTTPS, SSH) |

## CI/CD Pipeline

### CI (on every push)
1. Build 4 Java services (Maven matrix strategy)
2. Build React frontend
3. Build 5 Docker images
4. Scan images with **Trivy** for vulnerabilities
5. Push to ECR (main branch only)

### CD (after CI on main)
1. Copy production compose file to EC2
2. SSH into EC2, pull latest images from ECR
3. Rolling deployment with `docker compose up -d`
4. Health check all services via `/actuator/health`

## Project Structure

```
event-booking/
├── .github/workflows/       # CI/CD pipelines
│   ├── ci.yml               # Build, scan, push
│   └── cd.yml               # Deploy to EC2
├── terraform/               # Infrastructure as Code
│   ├── main.tf              # Root module
│   ├── variables.tf         # Input variables
│   ├── outputs.tf           # Output values
│   ├── provider.tf          # AWS provider
│   ├── backend.tf           # S3 remote state
│   └── modules/
│       ├── vpc/             # VPC, subnets, security groups
│       ├── ec2/             # EC2 instance + user-data
│       ├── ecr/             # Container registries
│       ├── iam/             # Roles and policies
│       ├── s3/              # Image storage bucket
│       ├── ssm/             # Secrets in Parameter Store
│       └── monitoring/      # CloudWatch, SNS, alarms
├── user/                    # User microservice (Spring Boot)
├── event/                   # Event microservice (Spring Boot)
├── booking/                 # Booking microservice (Spring Boot)
├── external-api/            # External API microservice (Spring Boot)
├── eventbooking-app/        # React frontend
├── docker-compose.yml       # Local development
└── docker-compose.prod.yml  # Production (ECR images + CloudWatch logs)
```

## Getting Started

### Prerequisites
- Java 21 (JDK)
- Node.js 18+
- Docker and Docker Compose
- AWS CLI (for production deployment)
- Terraform 1.0+ (for infrastructure)

### Local Development

```bash
# Build all Java services
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
for svc in user event booking external-api; do
  (cd $svc && ./mvnw clean package -DskipTests -q)
done

# Start everything with Docker Compose
docker compose up --build -d

# App available at http://localhost:3000
```

### Production Deployment (AWS)

```bash
# 1. Configure Terraform variables
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values

# 2. Deploy infrastructure
terraform init
terraform plan
terraform apply

# 3. Set GitHub repository secrets:
#    AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY,
#    AWS_ACCOUNT_ID, EC2_HOST, EC2_SSH_KEY

# 4. Push to main branch - CI/CD handles the rest
git push origin main
```

### Tear Down

```bash
cd terraform
terraform destroy
```

## Environment Variables

See [.env.example](.env.example) for all required configuration.

## Security

- Firebase service account keys excluded from Git via `.gitignore`
- API keys stored in AWS SSM Parameter Store (encrypted)
- Docker images scanned with Trivy in CI pipeline
- IAM role follows least-privilege principle
- SSH access restricted by CIDR in security groups
- ECR repositories have lifecycle policies (keep last 5 images)

## Monitoring

- **CloudWatch Logs**: All container logs streamed via `awslogs` driver
- **CloudWatch Metrics**: CPU, memory, disk, network
- **CloudWatch Alarms**: CPU > 80%, status check failures
- **SNS Alerts**: Email notifications on alarm state changes
- **CloudWatch Dashboard**: Single view of all metrics and logs
- **Health Endpoints**: `/actuator/health` on each service
