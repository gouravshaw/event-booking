variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "alert_email" {
  type = string
}

variable "ec2_instance_id" {
  type = string
}

variable "sqs_dlq_arn" {
  type        = string
  description = "ARN of the SQS Dead Letter Queue to monitor"
}
