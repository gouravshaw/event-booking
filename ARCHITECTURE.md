# Event Booking System Architecture

## End-to-End AWS Architecture (Deployment + Runtime)

```mermaid
graph LR
    subgraph External
        User[User Browser]
        Dev[Developer]
        Firebase[Firebase Auth]
        GMaps[Google Maps API]
        Frank[Frankfurter API]
        Wiki[Wikipedia API]
    end
    
    GitHub[GitHub Repository]
    Terraform[Terraform IaC]
    
    subgraph AWS["AWS Cloud"]
        ECR[Amazon ECR]
        S3[S3 Bucket]
        SQS[SQS Queue]
        SSM[SSM Parameter Store]
        CW[CloudWatch]
        SNS[SNS Topic]
        IAM[IAM Role]
        
        subgraph VPC["VPC"]
            subgraph Subnet["Public Subnet"]
                EC2[EC2 t3.medium Docker Host]
                
                subgraph Docker["Docker Containers"]
                    Nginx[Frontend Nginx:80]
                    UserSvc[User Service :9090]
                    EventSvc[Event Service :9091]
                    BookingSvc[Booking Service :9092]
                    ExtAPI[External API :9093]
                    Mongo[(MongoDB :27017)]
                end
            end
        end
    end
    
    Dev -->|push| GitHub
    GitHub -->|CI: build/test/scan| ECR
    GitHub -->|CD: deploy| EC2
    Terraform -.->|provision| ECR
    Terraform -.->|provision| EC2
    Terraform -.->|provision| S3
    Terraform -.->|provision| SQS
    Terraform -.->|provision| SSM
    Terraform -.->|provision| CW
    Terraform -.->|provision| SNS
    Terraform -.->|provision| IAM
    
    User -->|HTTP| Nginx
    Nginx --> UserSvc
    Nginx --> EventSvc
    Nginx --> BookingSvc
    Nginx --> ExtAPI
    
    UserSvc --> Firebase
    UserSvc --> Mongo
    
    EventSvc --> Mongo
    EventSvc -->|store/serve| S3
    SQS -->|consume| EventSvc
    
    BookingSvc --> Mongo
    BookingSvc -->|publish| SQS
    BookingSvc -.->|HTTP fallback| EventSvc
    
    ExtAPI --> Mongo
    ExtAPI --> GMaps
    ExtAPI --> Frank
    ExtAPI --> Wiki
    
    EC2 -->|pull images| ECR
    Nginx -->|logs| CW
    UserSvc -->|logs| CW
    EventSvc -->|logs| CW
    BookingSvc -->|logs| CW
    ExtAPI -->|logs| CW
    
    Nginx -.->|secrets| SSM
    UserSvc -.->|secrets| SSM
    EventSvc -.->|secrets| SSM
    BookingSvc -.->|secrets| SSM
    ExtAPI -.->|secrets| SSM
    
    CW -->|alarms| SNS
    IAM -.->|permissions| EC2
```

## Description

This diagram shows the complete Event Booking System architecture including:
- CI/CD deployment pipeline
- AWS infrastructure provisioned by Terraform
- Runtime architecture with Docker containers on EC2
- Integration with external APIs
