output "mode" {
  value = var.hibernate ? "hibernated" : "live"
}

output "alb_dns_name" {
  value = local.live ? aws_lb.api[0].dns_name : null
}

output "db_endpoint" {
  value     = local.live ? aws_db_instance.main[0].address : null
  sensitive = true
}

output "cloudfront_domain" {
  value = aws_cloudfront_distribution.web.domain_name
}
