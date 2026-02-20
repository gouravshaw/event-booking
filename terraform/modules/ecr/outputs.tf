output "repository_urls" {
  value = { for k, v in aws_ecr_repository.services : k => v.repository_url }
}

output "registry_url" {
  value = split("/", aws_ecr_repository.services["user-service"].repository_url)[0]
}
