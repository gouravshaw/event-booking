resource "aws_sqs_queue" "ticket_updates" {
  name                       = "${var.project_name}-ticket-updates"
  message_retention_seconds  = 86400
  visibility_timeout_seconds = 30
  receive_wait_time_seconds  = 5

  tags = {
    Name        = "${var.project_name}-ticket-updates"
    Environment = var.environment
  }
}
