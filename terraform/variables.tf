variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "event-booking"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "prod"
}

variable "ec2_instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.medium"
}

variable "ec2_key_name" {
  description = "Name of the SSH key pair for EC2 access"
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "CIDR block allowed to SSH into EC2 (your IP)"
  type        = string
  default     = "0.0.0.0/0"
}

variable "alert_email" {
  description = "Email address for CloudWatch alarm notifications"
  type        = string
}

variable "firebase_api_key" {
  description = "Firebase API key for frontend"
  type        = string
  sensitive   = true
}

variable "firebase_auth_domain" {
  description = "Firebase auth domain"
  type        = string
  sensitive   = true
}

variable "firebase_project_id" {
  description = "Firebase project ID"
  type        = string
  sensitive   = true
}

variable "google_maps_api_key" {
  description = "Google Maps API key"
  type        = string
  sensitive   = true
}

variable "admin_secret" {
  description = "Admin secret for user registration — must be set explicitly, no default"
  type        = string
  sensitive   = true
}
