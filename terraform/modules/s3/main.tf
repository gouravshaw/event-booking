resource "aws_s3_bucket" "event_images" {
  bucket        = "${var.project_name}-images-${var.environment}"
  force_destroy = true

  tags = {
    Name        = "${var.project_name}-images"
    Environment = var.environment
  }
}

resource "aws_s3_bucket_public_access_block" "event_images" {
  bucket = aws_s3_bucket.event_images.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "event_images_public_read" {
  bucket = aws_s3_bucket.event_images.id

  depends_on = [aws_s3_bucket_public_access_block.event_images]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.event_images.arn}/*"
      }
    ]
  })
}

resource "aws_s3_bucket_cors_configuration" "event_images" {
  bucket = aws_s3_bucket.event_images.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST"]
    allowed_origins = ["*"]
    max_age_seconds = 3000
  }
}
