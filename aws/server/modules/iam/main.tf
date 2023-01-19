# iam.tf | IAM Role Policies
resource "aws_iam_role" "ecsTaskExecutionRole" {
  name               = "${var.prefix_name}-execution-task-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
  tags = {
    Name        = "${var.prefix_name}-iam-role"
  }
}

data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy_attachment" "ecsTaskExecutionRole_policy" {
  role       = aws_iam_role.ecsTaskExecutionRole.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}
####role for ec2 instance
resource "aws_iam_role" "ecs-ec2-role" {
  name               = "${var.prefix_name}-ecs-ec2-role"
  assume_role_policy = file("${path.module}/policies/ecs-ec2-role.json")
}
resource "aws_iam_role_policy" "ecs-ec2-role-policy" {
  name   = "${var.prefix_name}-ecs-ec2-role-policy"
  policy = file("${path.module}/policies/ecs-ec2-role-policy.json")
  role   = aws_iam_role.ecs-ec2-role.name
}
resource "aws_iam_instance_profile" "ecs-ec2-role" {
  name = "${var.prefix_name}-ecs-ec2-role"
  role = aws_iam_role.ecs-ec2-role.name
}

####role auto scaling ###
resource "aws_iam_role" "ecs-autoscale-role" {
  name               = "${var.prefix_name}-ecs-autoscale-role"
  assume_role_policy = file("${path.module}/policies/ecs-autoscale-role.json")
}
resource "aws_iam_role_policy" "ecs-autoscale-role-policy" {
  name   = "${var.prefix_name}-ecs-autoscale-role-policy"
  policy = file("${path.module}/policies/ecs-autoscale-role-policy.json")
  role   = aws_iam_role.ecs-autoscale-role.id
}
