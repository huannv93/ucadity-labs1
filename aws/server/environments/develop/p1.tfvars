# AWS Information
aws_region   = "us-west-2"
aws_key_pair = "bpc-dev"

# Game Information
game_tag   = "bpc"
#environment_tag = "" # pass env
#prefix_tag      = "" # pass env
gearinc_ip = ["118.70.129.253/32", "118.70.182.33/32", "101.99.15.12/32", "14.177.64.76/32", "117.2.120.138/32", "14.241.131.45/32"]

# Networking Module
vpc_id              = "vpc-842e1ffc"
availability_zone_1 = "us-west-2b"
availability_zone_2 = "us-west-2c"
app_cidr_1          = "172.31.120.0/24"
app_cidr_2          = "172.31.121.0/24"
redis_sg            = ["sg-03f56a049d46d637c"]

# Route53 Module
hosted_zone        = "dev.bpc.alleylabs.com"
ssl_certificate_id = "arn:aws:acm:us-west-2:636583053830:certificate/ca793830-ace4-498e-9571-5b3193a1ac0f"

# ECS
ecr_url  = "636583053830.dkr.ecr.us-west-2.amazonaws.com"
ecr_name = "bpc-develop"
#dd_api_key = "" # env credentials
dd_tags  = "env:dev"

gear_debug          = "yes"
gear_cdn_url        = "https://cdn.bpc.alleylabs.com/"
gear_worker_amount  = "8"
gear_worker_service = "p1"
gear_worker_version = "1.0.0"
gear_dynamo_prefix  = "bpc-dev-"
gear_redis_default  = "bpc-dev.ddng11.0001.usw2.cache.amazonaws.com"
#gear_firebase_credentials  = "" # env credentials
#gear_pusher_url            = "" # env credentials

# container
worker_count  = 1
app_count_max = 1
app_cpu       = 450
app_mem       = 975
# ASG
instance_type = "t3a.medium"
instance_min  = 1
instance_max  = 1
