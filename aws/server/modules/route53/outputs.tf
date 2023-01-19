# output "app_route53_record" {
#   value = aws_route53_record.app.name
# }
###them hostzone de output show full subdomain route53
output "app_route53_record" {
  value = "${aws_route53_record.app.name}.${var.hosted_zone}"
}