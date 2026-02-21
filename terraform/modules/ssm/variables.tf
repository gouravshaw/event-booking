variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

# NOTE: Sensitive values (Firebase keys, Maps key, admin secret) are NOT
# managed by Terraform. They are pushed to SSM directly by CI/CD using
# GitHub Secrets. Terraform only creates the parameter paths with placeholders.

variable "s3_bucket_name" {
  type = string
}
