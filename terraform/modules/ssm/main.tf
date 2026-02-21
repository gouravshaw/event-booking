# SSM Parameter Store — infrastructure-managed parameters only.
# Sensitive secrets (Firebase, Maps, admin) are pushed by CI/CD from GitHub Secrets.
# Terraform creates the parameter paths with placeholder values; CI overwrites the real values.

resource "aws_ssm_parameter" "firebase_api_key" {
  name  = "/${var.project_name}/${var.environment}/firebase-api-key"
  type  = "SecureString"
  value = "PLACEHOLDER_SET_BY_CI"

  lifecycle {
    ignore_changes = [value]  # CI owns this value — Terraform won't reset it
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "CI"
  }
}

resource "aws_ssm_parameter" "firebase_auth_domain" {
  name  = "/${var.project_name}/${var.environment}/firebase-auth-domain"
  type  = "SecureString"
  value = "PLACEHOLDER_SET_BY_CI"

  lifecycle {
    ignore_changes = [value]
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "CI"
  }
}

resource "aws_ssm_parameter" "firebase_project_id" {
  name  = "/${var.project_name}/${var.environment}/firebase-project-id"
  type  = "SecureString"
  value = "PLACEHOLDER_SET_BY_CI"

  lifecycle {
    ignore_changes = [value]
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "CI"
  }
}

resource "aws_ssm_parameter" "google_maps_api_key" {
  name  = "/${var.project_name}/${var.environment}/google-maps-api-key"
  type  = "SecureString"
  value = "PLACEHOLDER_SET_BY_CI"

  lifecycle {
    ignore_changes = [value]
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "CI"
  }
}

resource "aws_ssm_parameter" "admin_secret" {
  name  = "/${var.project_name}/${var.environment}/admin-secret"
  type  = "SecureString"
  value = "PLACEHOLDER_SET_BY_CI"

  lifecycle {
    ignore_changes = [value]
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "CI"
  }
}

resource "aws_ssm_parameter" "s3_bucket_name" {
  name  = "/${var.project_name}/${var.environment}/s3-bucket-name"
  type  = "String"
  value = var.s3_bucket_name

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}
