# output "vpc_id" {
#  value       = module.networking.vpc_id
#  description = "VPC ID"
# }

# output "public_subnets" {
#  value       = module.networking.public_subnets
#  description = "VPC public subnets' IDs list"
# }

# output "private_subnets" {
#  value       = module.networking.private_subnets
#  description = "VPC private subnets' IDs list"
# }

output "ecs_instance_profile" {
  value = module.iam.ecs_instance_profile
}
output "ecsTaskExecutionRole" {
  value = module.iam.ecsTaskExecutionRole
}
output "alb_dns_name" {
  value = module.ecs.alb_dns_name
}
output "alb_zone_id" {
  value = module.ecs.alb_zone_id
}

output "app_route53_record" {
  value = module.route53.app_route53_record
}
###show nhung thong tin can thiet ra console... . doi voi ../module can show nhung output nao muon get tu module nay sang module kia.