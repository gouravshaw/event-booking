# Uncomment this block after creating the S3 bucket for state storage.
# Run 'terraform init' again after uncommenting to migrate state.
#
# terraform {
#   backend "s3" {
#     bucket  = "event-booking-terraform-state"
#     key     = "prod/terraform.tfstate"
#     region  = "eu-west-2"
#     encrypt = true
#   }
# }
