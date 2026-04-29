variable "region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-west-2"
}

variable "cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "dev-platform"
}

variable "cluster_version" {
  description = "Kubernetes version"
  type        = string
  default     = "1.31"
}

variable "node_instance_type" {
  description = "EC2 instance type for worker nodes"
  type        = string
  default     = "t3.medium"
}

variable "node_min_size" {
  type    = number
  default = 1
}

variable "node_max_size" {
  type    = number
  default = 4
}

variable "node_desired_size" {
  type    = number
  default = 2
}

variable "db_password" {
  description = "PostgreSQL password — pass via TF_VAR_db_password env var, never hardcode"
  type        = string
  sensitive   = true
}

variable "app_image_tag" {
  description = "Docker image tag to deploy for backend and frontend"
  type        = string
  default     = "latest"
}

variable "ingress_host" {
  description = "Hostname for the Ingress rule"
  type        = string
  default     = "dev-platform.example.com"
}