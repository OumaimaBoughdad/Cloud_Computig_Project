variable "instance_name" {
  description = "Nom de l'instance Ubuntu"
  type        = string
  default     = "ubuntu-nginx-password"
}

variable "image_name" {
  description = "Nom de l'image Ubuntu"
  type        = string
  default     = "ubuntu-22.04"
}

variable "flavor_name" {
  description = "Flavor de l'instance"
  type        = string
  default     = "m1.small"
}

variable "network_test" {
  description = "Nom du réseau test"
  type        = string
  default     = "test"
}

variable "network_external" {
  description = "Nom du réseau external"
  type        = string
  default     = "external"
}

variable "ubuntu_password" {
  description = "Mot de passe pour l'utilisateur ubuntu"
  type        = string
  default     = "pass1234"
}
