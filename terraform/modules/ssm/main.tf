resource "aws_ssm_parameter" "firebase_api_key" {
  name  = "/${var.project_name}/${var.environment}/firebase-api-key"
  type  = "SecureString"
  value = var.firebase_api_key

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_ssm_parameter" "firebase_auth_domain" {
  name  = "/${var.project_name}/${var.environment}/firebase-auth-domain"
  type  = "SecureString"
  value = var.firebase_auth_domain

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_ssm_parameter" "firebase_project_id" {
  name  = "/${var.project_name}/${var.environment}/firebase-project-id"
  type  = "SecureString"
  value = var.firebase_project_id

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_ssm_parameter" "google_maps_api_key" {
  name  = "/${var.project_name}/${var.environment}/google-maps-api-key"
  type  = "SecureString"
  value = var.google_maps_api_key

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_ssm_parameter" "admin_secret" {
  name  = "/${var.project_name}/${var.environment}/admin-secret"
  type  = "SecureString"
  value = var.admin_secret

  tags = {
    Environment = var.environment
    Project     = var.project_name
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
