output "alb_sg" {
  value = aws_security_group.alb-sg.id
}
output "ecs_sg" {
  value = aws_security_group.ecs-sg.id
}