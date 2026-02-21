# Dead Letter Queue — captures messages that fail 3 processing attempts
resource "aws_sqs_queue" "ticket_updates_dlq" {
  name                      = "${var.project_name}-ticket-updates-dlq"
  message_retention_seconds = 604800 # 7 days to investigate failed messages

  tags = {
    Name        = "${var.project_name}-ticket-updates-dlq"
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "ticket_updates" {
  name                       = "${var.project_name}-ticket-updates"
  message_retention_seconds  = 86400
  visibility_timeout_seconds = 30
  receive_wait_time_seconds  = 5

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.ticket_updates_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name        = "${var.project_name}-ticket-updates"
    Environment = var.environment
  }
}
