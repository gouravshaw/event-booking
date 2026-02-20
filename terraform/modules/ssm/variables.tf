variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "firebase_api_key" {
  type      = string
  sensitive = true
}

variable "firebase_auth_domain" {
  type      = string
  sensitive = true
}

variable "firebase_project_id" {
  type      = string
  sensitive = true
}

variable "google_maps_api_key" {
  type      = string
  sensitive = true
}

variable "admin_secret" {
  type      = string
  sensitive = true
}

variable "s3_bucket_name" {
  type = string
}
