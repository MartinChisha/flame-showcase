variable "region" {
  type    = string
  default = "us-east-1"
}

variable "project" {
  type    = string
  default = "flame"
}

variable "hibernate" {
  description = "true parks the stack at minimum cost: no ALB, RDS instance, running tasks or interface endpoints, and the maintenance page is on."
  type        = bool
  default     = false
}

variable "rds_restore_snapshot_id" {
  description = "When resuming from hibernation, the final snapshot to restore RDS from. Null in normal operation."
  type        = string
  default     = null
}

variable "rds_final_snapshot_id" {
  description = "Name of the final snapshot taken when hibernation destroys RDS. Must be unique per hibernation."
  type        = string
  default     = "flame-prod-rds-hibernate-YYYYMMDD"
}

variable "rds_deletion_protection" {
  description = "Lift in a separate apply immediately before hibernating."
  type        = bool
  default     = true
}

variable "db_master_password" {
  description = "Only when creating RDS from scratch (no snapshot). Supply via TF_VAR_db_master_password."
  type        = string
  default     = null
  sensitive   = true
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "alb_security_group_id" {
  type = string
}

variable "ecs_security_group_id" {
  type = string
}

variable "endpoint_security_group_id" {
  type = string
}

variable "rds_security_group_id" {
  type = string
}

variable "certificate_arn" {
  description = "ACM certificate for the API hostname."
  type        = string
}

variable "api_image" {
  description = "Baseline image. CI registers newer task definition revisions, which Terraform ignores."
  type        = string
  default     = "<aws-account-id>.dkr.ecr.us-east-1.amazonaws.com/flame-api:latest"
}

variable "interface_endpoint_services" {
  description = "AWS services the private API reaches without a NAT gateway."
  type        = list(string)
  default     = ["ecr.api", "ecr.dkr", "logs", "secretsmanager", "ssmmessages", "elasticfilesystem", "email-smtp", "sts"]
}

variable "github_repository" {
  description = "owner/name allowed to assume the deploy role through OIDC."
  type        = string
  default     = "example-org/flame-api"
}
