provider "aws" {
  region = var.region
}

terraform {
  backend "s3" {
    bucket         = "tfstate-bucket-t5ad"
    key            = "env/dev/terraform.tfstate"
    region         = "ap-southeast-1"
    encrypt        = true
  }
}


resource "aws_security_group" "simbus_security_terraform" {
  name        = "app-security-group"
  description = "Allow SSH, frontend and backend traffic"
  vpc_id      = var.vpc_id

  ingress {
    description = "Allow SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow frontend HTTP"
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow backend HTTP"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}


resource "aws_instance" "simbus" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.simbus_security_terraform.id]
  associate_public_ip_address = true

  tags = {
    Name = "simbus-instance"
  }
}
