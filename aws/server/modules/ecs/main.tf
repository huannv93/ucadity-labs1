####ALB#####
resource "aws_lb" "alb" {
  name                       = "${var.prefix_name}-alb"
  internal                   = false
  load_balancer_type         = "application"
  security_groups            = [var.alb_sg]
  subnets                    = var.public_subnet
  enable_deletion_protection = false

  tags = {
    Name        = "${var.prefix_name}-alb"
    Terraform   = "true"
  }
}
###frontend ALB####
resource "aws_lb_target_group" "frontend" {
  name                 = "${var.prefix_name}-frontend-tg"
  port                 = 80
  protocol             = "HTTP"
  vpc_id               = var.vpc_id
  depends_on           = [aws_lb.alb]
  deregistration_delay = 60
  health_check {
    path                = "/"
    healthy_threshold   = 5
    unhealthy_threshold = 2
    timeout             = 5
    protocol            = "HTTP"
    port                = "traffic-port"
    interval            = 30
  }

  tags = {
    Name        = "${var.prefix_name}-tg"
    Terraform   = "true"
  }
}

resource "aws_lb_listener" "alb-https-frontend" {
  load_balancer_arn = aws_lb.alb.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = var.ssl_certificate_id

  default_action {
    target_group_arn = aws_lb_target_group.frontend.arn
    type             = "forward"
  }
}
####################
######Backend ALB####
resource "aws_lb_target_group" "backend" {
  name                 = "${var.prefix_name}-backend-tg"
  port                 = 80
  protocol             = "HTTP"
  vpc_id               = var.vpc_id
  depends_on           = [aws_lb.alb]
  deregistration_delay = 60
  health_check {
    path                = "/api/status"
    healthy_threshold   = 5
    unhealthy_threshold = 2
    timeout             = 5
    protocol            = "HTTP"
    port                = "traffic-port"
    interval            = 30
  }

  tags = {
    Name        = "${var.prefix_name}-backend-tg"
    Terraform   = "true"
  }
}

resource "aws_lb_listener" "alb-https-backend" {
  load_balancer_arn = aws_lb.alb.arn
  port              = 3030
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = var.ssl_certificate_id

  default_action {
    target_group_arn = aws_lb_target_group.backend.arn
    type             = "forward"
  }
}
#############

####cluster####
data "aws_ami" "latest_ecs" {
  most_recent = true

  filter {
    name   = "name"
    values = ["amzn2-ami-ecs-hvm-2*x86_64-ebs"]
  }

  owners = ["591542846629"]
}
####
resource "aws_launch_configuration" "cluster-lc" {
  name_prefix          = "${var.prefix_name}-cluster-lc"
  security_groups      = concat([var.ecs_sg])
  image_id             = data.aws_ami.latest_ecs.id
  instance_type        = var.instance_type
  #  key_name             = var.instance_key_pair
  iam_instance_profile = var.instance_profile
  user_data            = templatefile("${path.module}/templates/ecs.config.tpl", {
    ecs_cluster = aws_ecs_cluster.cluster.name
  })

  lifecycle {
    create_before_destroy = true
  }
}
#####
resource "aws_ecs_cluster" "cluster" {
  name = "${var.prefix_name}-cluster"
}
####cluster####
resource "aws_autoscaling_group" "cluster" {
  name                      = "${var.prefix_name}-ecs-asg"
  vpc_zone_identifier       = var.public_subnet
  min_size                  = var.instance_min
  max_size                  = var.instance_max
  desired_capacity          = var.instance_min
  launch_configuration      = aws_launch_configuration.cluster-lc.name
  health_check_grace_period = 120
  default_cooldown          = 300
  wait_for_capacity_timeout = "3m"
  lifecycle {
    create_before_destroy = true
    ignore_changes        = [desired_capacity]
  }
  tag {
    key                 = "Name"
    value               = "${var.prefix_name}-ecs-asg"
    propagate_at_launch = true
  }
}
####cluster####
resource "aws_autoscaling_policy" "low_cpu_policy" {
  name                   = "${var.prefix_name}-low-cpu-asg-policy"
  adjustment_type        = "ChangeInCapacity"
  autoscaling_group_name = aws_autoscaling_group.cluster.name
  policy_type            = "StepScaling"

  step_adjustment {
    scaling_adjustment          = 1
    metric_interval_lower_bound = 0
  }
}
####cluster logs###
resource "aws_cloudwatch_metric_alarm" "used-cpu-trigger" {
  alarm_name          = "${var.prefix_name}-too-low-cpu-asg-alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "5"
  datapoints_to_alarm = "5"
  treat_missing_data  = "notBreaching"
  metric_name         = "CPUReservation"
  namespace           = "AWS/ECS"
  period              = "60"
  statistic           = "Average"
  threshold           = "70"

  dimensions = {
    ClusterName = aws_ecs_cluster.cluster.name
  }

  alarm_description = "This metric monitors too low CPU for new instance"
  alarm_actions     = [aws_autoscaling_policy.low_cpu_policy.arn]
}
##############

######task definition and service ###########
resource "aws_ecs_task_definition" "frontend" {
  family                = "${var.prefix_name}-frontend"
  execution_role_arn    = var.execution_role
  task_role_arn         = var.execution_role
  container_definitions = templatefile("${path.module}/templates/task_frontend.tpl", {
    image_repo                = "${var.dockerhub}/${var.image_name_frontend}"
  })
}

resource "aws_ecs_service" "frontend" {
  name                               = "frontend"
  task_definition                    = aws_ecs_task_definition.frontend.family
  desired_count                      = 1
  cluster                            = aws_ecs_cluster.cluster.id
  deployment_minimum_healthy_percent = 100
  health_check_grace_period_seconds  = 300


  load_balancer {
    target_group_arn = aws_lb_target_group.frontend.arn
    container_name   = "frontend"
    container_port   = "3000"
  }
  lifecycle {
    ignore_changes = [desired_count]
  }
}
######
resource "aws_ecs_task_definition" "backend" {
  family                = "${var.prefix_name}-backend"
  execution_role_arn    = var.execution_role
  task_role_arn         = var.execution_role
  container_definitions = templatefile("${path.module}/templates/task_backend.tpl", {
    image_repo                = "${var.dockerhub}/${var.image_name_backend}"
  })
}

resource "aws_ecs_service" "backend" {
  name                               = "backend"
  task_definition                    = aws_ecs_task_definition.backend.family
  desired_count                      = 1
  cluster                            = aws_ecs_cluster.cluster.id
  deployment_minimum_healthy_percent = 100
  health_check_grace_period_seconds  = 300


  load_balancer {
    target_group_arn = aws_lb_target_group.backend.arn
    container_name   = "backend"
    container_port   = "3030"
  }
  lifecycle {
    ignore_changes = [desired_count]
  }
}
######

###auto scaling container####
resource "aws_appautoscaling_target" "app_target" {
  service_namespace  = "ecs"
  resource_id        = "service/${aws_ecs_cluster.cluster.name}/${aws_ecs_service.frontend.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  role_arn           = var.autoscale_role
  min_capacity       = 1
  max_capacity       = var.app_count_max
}

resource "aws_appautoscaling_policy" "app_scaling_policy" {
  name               = "${var.prefix_name}-app-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.app_target.resource_id
  scalable_dimension = aws_appautoscaling_target.app_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.app_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    target_value       = 70
    scale_in_cooldown  = 300
    scale_out_cooldown = 120
  }
}
###auto scaling container####





