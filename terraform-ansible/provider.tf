terraform {
  required_version = ">= 0.14.0"
  required_providers {
    openstack = {
      source  = "terraform-provider-openstack/openstack"
      version = "~> 1.51.0"
    }
  }
}

provider "openstack" {
  auth_url            = "https://10.0.2.15:5000/v3"
  user_name           = "admin"
  password            = "uM6LNAw7Euh2pPbiii7docuLOTtgMrSt"
  tenant_name         = "admin"
  user_domain_name    = "Default"
  project_domain_name = "Default"
  region              = "microstack"
  cacert_file         = "/var/snap/microstack/common/etc/ssl/certs/cacert.pem"
  insecure            = false
}
