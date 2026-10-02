locals {
  live = !var.hibernate
  name = "${var.project}-prod"
}

# ---------------------------------------------------------------------------
# Private connectivity without a NAT gateway. Removed while hibernated: they
# are the largest fixed cost of the live stack.
# ---------------------------------------------------------------------------
resource "aws_vpc_endpoint" "interface" {
  for_each = local.live ? toset(var.interface_endpoint_services) : toset([])

  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.region}.${each.value}"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.endpoint_security_group_id]
  private_dns_enabled = true
}

# ---------------------------------------------------------------------------
# Load balancer: listener and ALB come and go; the target group is kept so the
# ECS service definition never has to change.
# ---------------------------------------------------------------------------
resource "aws_lb" "api" {
  count = local.live ? 1 : 0

  name                       = "${local.name}-alb"
  load_balancer_type         = "application"
  subnets                    = var.public_subnet_ids
  security_groups            = [var.alb_security_group_id]
  drop_invalid_header_fields = true
}

resource "aws_lb_target_group" "api" {
  name        = "${local.name}-api"
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = var.vpc_id

  health_check {
    path                = "/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 30
  }
}

resource "aws_lb_listener" "https" {
  count = local.live ? 1 : 0

  load_balancer_arn = aws_lb.api[0].arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api.arn
  }
}

# ---------------------------------------------------------------------------
# Database: destroyed behind a named final snapshot, restored under the same
# identifier so the JDBC URL stored in Secrets Manager stays valid.
# ---------------------------------------------------------------------------
resource "aws_db_instance" "main" {
  count = local.live ? 1 : 0

  identifier                = "${local.name}-rds"
  engine                    = "postgres"
  engine_version            = "16"
  instance_class            = "db.t4g.micro"
  allocated_storage         = 20
  storage_encrypted         = true
  db_name                   = var.rds_restore_snapshot_id == null ? "league_db" : null
  username                  = var.rds_restore_snapshot_id == null ? "flame_admin" : null
  password                  = var.rds_restore_snapshot_id == null ? var.db_master_password : null
  snapshot_identifier       = var.rds_restore_snapshot_id
  vpc_security_group_ids    = [var.rds_security_group_id]
  publicly_accessible       = false
  backup_retention_period   = 7
  deletion_protection       = var.rds_deletion_protection
  skip_final_snapshot       = false
  final_snapshot_identifier = var.rds_final_snapshot_id

  lifecycle {
    ignore_changes = [snapshot_identifier]
  }
}
