variable "prefix_name" {}

variable "alb_sg" {}
variable "public_subnet" {}
variable "vpc_id" {}
variable "ssl_certificate_id" {}

variable "execution_role" {}
variable "dockerhub" {}
variable "image_name_frontend" {}
variable "image_name_backend" {}
variable "ecs_sg" {}
variable "instance_type" {}
variable "instance_min" {}
variable "instance_max" {}
variable "instance_profile" {}
variable "autoscale_role" {}
variable "app_count_max" {}