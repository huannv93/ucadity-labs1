terraform {
  backend "local" {
  }
}

locals {
  prefix_name = "nodejs-ecs"
}

module "networking" {
  source              = "./modules/networking"
  vpc_name            = var.vpc_name
  vpc_cidr            = var.vpc_cidr
  aws_region          = var.aws_region
}

module "security_groups" {
  source          = "./modules/security_groups"
  prefix_name     = local.prefix_name
  vpc_id          = module.networking.vpc_id
}

module "iam" {
  source      = "./modules/iam"
  prefix_name = local.prefix_name
}

module "ecs" {
  source                    = "./modules/ecs"
  prefix_name               = local.prefix_name
  alb_sg                    = module.security_groups.alb_sg
  public_subnet             = module.networking.public_subnets
  vpc_id                    = module.networking.vpc_id
  ssl_certificate_id        = var.ssl_certificate_id
  dockerhub                 = var.dockerhub
  image_name_frontend       = var.image_name_frontend
  image_name_backend        = var.image_name_backend
  ecs_sg                    = module.security_groups.ecs_sg
  instance_type             = var.instance_type
  instance_max              = var.instance_max
  instance_min              = var.instance_min
  execution_role            = module.iam.ecsTaskExecutionRole
  instance_profile          = module.iam.ecs_instance_profile
  app_count_max             = var.app_count_max
  autoscale_role            = module.iam.ecs_autoscale_role
}
module "route53" {
  source       = "./modules/route53"
  hosted_zone  = var.hosted_zone
  prefix_tag   = var.prefix_tag
  alb_dns_name = module.ecs.alb_dns_name
  alb_zone_id  = module.ecs.alb_zone_id
}