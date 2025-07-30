variable "region" {
  description = "AWS region"
  type        = string
  default     = "ap-southeast-1"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t2.micro"
}

variable "ami_id" {
  description = "AMI ID for the EC2 instance"
  type        = string
  default     = "ami-02c7683e4ca3ebf58"
}

variable "key_name" {
  description = "AWS key pair name to allow SSH"
  type        = string
  default     = aws-deploy-key
}
