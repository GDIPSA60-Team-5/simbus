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

data "aws_vpc" "default" {
  default = true
}

resource "aws_security_group" "simbus_security_terraform" {
  name        = "app-security-group"
  description = "Allow SSH, frontend and backend traffic"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Allow SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow HTTP (nginx reverse proxy)"
    from_port   = 80
    to_port     = 80
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
