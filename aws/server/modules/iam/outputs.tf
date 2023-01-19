output "ecsTaskExecutionRole" {
  value = aws_iam_role.ecsTaskExecutionRole.arn
}
output "ecs_instance_profile" {
  value = aws_iam_instance_profile.ecs-ec2-role.id
}
output "ecs_autoscale_role" {
  value = aws_iam_role.ecs-autoscale-role.arn
}