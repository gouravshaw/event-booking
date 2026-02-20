output "parameter_arns" {
  value = [
    aws_ssm_parameter.firebase_api_key.arn,
    aws_ssm_parameter.firebase_auth_domain.arn,
    aws_ssm_parameter.firebase_project_id.arn,
    aws_ssm_parameter.google_maps_api_key.arn,
    aws_ssm_parameter.admin_secret.arn,
    aws_ssm_parameter.s3_bucket_name.arn,
  ]
}
