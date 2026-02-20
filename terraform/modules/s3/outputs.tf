output "bucket_name" {
  value = aws_s3_bucket.event_images.bucket
}

output "bucket_arn" {
  value = aws_s3_bucket.event_images.arn
}

output "bucket_domain_name" {
  value = aws_s3_bucket.event_images.bucket_regional_domain_name
}
