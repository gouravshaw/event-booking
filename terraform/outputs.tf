output "ec2_public_ip" {
  description = "Public IP of the EC2 instance"
  value       = module.ec2.public_ip
}

output "ec2_public_dns" {
  description = "Public DNS of the EC2 instance"
  value       = module.ec2.public_dns
}

output "ecr_repositories" {
  description = "ECR repository URLs for each service"
  value       = module.ecr.repository_urls
}

output "s3_bucket_name" {
  description = "S3 bucket name for event images"
  value       = module.s3.bucket_name
}

output "cloudwatch_dashboard_url" {
  description = "CloudWatch dashboard URL"
  value       = module.monitoring.dashboard_url
}

output "sqs_queue_url" {
  description = "SQS queue URL for ticket updates"
  value       = module.sqs.queue_url
}

output "app_url" {
  description = "Application URL"
  value       = "http://${module.ec2.public_ip}"
}
