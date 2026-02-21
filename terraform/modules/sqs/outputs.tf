output "queue_url" {
  value = aws_sqs_queue.ticket_updates.url
}

output "queue_arn" {
  value = aws_sqs_queue.ticket_updates.arn
}

output "dlq_arn" {
  value = aws_sqs_queue.ticket_updates_dlq.arn
}
