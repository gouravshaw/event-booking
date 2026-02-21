module "vpc" {
  source       = "./modules/vpc"
  project_name = var.project_name
  environment  = var.environment
}

module "iam" {
  source       = "./modules/iam"
  project_name = var.project_name
  environment  = var.environment
}

module "ecr" {
  source       = "./modules/ecr"
  project_name = var.project_name
}

module "s3" {
  source       = "./modules/s3"
  project_name = var.project_name
  environment  = var.environment
}

module "sqs" {
  source       = "./modules/sqs"
  project_name = var.project_name
  environment  = var.environment
}

module "ssm" {
  source               = "./modules/ssm"
  project_name         = var.project_name
  environment          = var.environment
  firebase_api_key     = var.firebase_api_key
  firebase_auth_domain = var.firebase_auth_domain
  firebase_project_id  = var.firebase_project_id
  google_maps_api_key  = var.google_maps_api_key
  admin_secret         = var.admin_secret
  s3_bucket_name       = module.s3.bucket_name
}

module "monitoring" {
  source          = "./modules/monitoring"
  project_name    = var.project_name
  environment     = var.environment
  alert_email     = var.alert_email
  ec2_instance_id = module.ec2.instance_id
  sqs_dlq_arn     = module.sqs.dlq_arn
}

module "ec2" {
  source            = "./modules/ec2"
  project_name      = var.project_name
  environment       = var.environment
  instance_type     = var.ec2_instance_type
  key_name          = var.ec2_key_name
  subnet_id         = module.vpc.public_subnet_id
  security_group_id = module.vpc.ec2_security_group_id
  instance_profile  = module.iam.instance_profile_name
  aws_region        = var.aws_region
  ecr_registry      = module.ecr.registry_url
}
