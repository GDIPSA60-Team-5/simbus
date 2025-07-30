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

resource "aws_instance" "simbus" {
  ami           = var.ami_id
  instance_type = var.instance_type
  key_name      = var.key_name

  tags = {
    Name = "simbus-instance"
  }
}

