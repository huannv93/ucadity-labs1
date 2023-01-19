resource "aws_security_group" "alb-sg" {
  name        = "${var.prefix_name}-alb-sg"
  vpc_id =  var.vpc_id
  description = "Security Group for ALB of ${var.prefix_name}"

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 3030
    to_port     = 3030
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.prefix_name}-alb-sg"
    Terraform   = "true"
  }
}

resource "aws_security_group" "ecs-sg" {
  name        = "${var.prefix_name}-ecs-sg"
  vpc_id =  var.vpc_id
  description = "Security Group for ECS of ${var.prefix_name} - allow access from ALB"

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.prefix_name}-ecs-sg"
    Terraform   = "true"
  }
}