# AWS Information
aws_region   = "us-west-2"
#aws_key_pair = "bpc-dev"

# Networking Module
vpc_name = "vpc-ecs"
vpc_cidr = "10.10.0.0/16"

# Security groups:
prefix_name = "NodeJS-ECS"

# Route53 Module
hosted_zone        = "huannguyen7651.info"
ssl_certificate_id = "arn:aws:acm:us-west-2:405253367546:certificate/e528c8e4-0724-4017-a9f9-a8b1b2fbc237"

# ECS
dockerhub  = "huannv93"
image_name_frontend = "udacity-awsdevops-project03-1_frontend"
image_name_backend = "udacity-awsdevops-project03-1_backend"
app_count_max = 2
instance_type = "t2.medium"
instance_min  = 1
instance_max  = 2
prefix_tag = "web"

