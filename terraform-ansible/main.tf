# Récupérer l'image Ubuntu
data "openstack_images_image_v2" "ubuntu" {
  name        = var.image_name
  most_recent = true
}

# Récupérer le flavor
data "openstack_compute_flavor_v2" "flavor" {
  name = var.flavor_name
}

# Récupérer le réseau test
data "openstack_networking_network_v2" "test" {
  name = var.network_test
}

# Récupérer le réseau external
data "openstack_networking_network_v2" "external" {
  name = var.network_external
}

# Créer un groupe de sécurité
resource "openstack_networking_secgroup_v2" "secgroup" {
  name        = "nginx-secgroup-password"
  description = "Security group for Nginx server with password auth"
}

# Règle pour SSH
resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_ssh" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 22
  port_range_max    = 22
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup.id
}

# Règle pour HTTP
resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_http" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 80
  port_range_max    = 80
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup.id
}

# Règle pour HTTPS
resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_https" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 443
  port_range_max    = 443
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup.id
}

# Règle pour ICMP (ping)
resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_icmp" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "icmp"
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup.id
}

# Instance avec Configuration Drive
resource "openstack_compute_instance_v2" "ubuntu_instance" {
  name            = var.instance_name
  image_id        = data.openstack_images_image_v2.ubuntu.id
  flavor_id       = data.openstack_compute_flavor_v2.flavor.id
  security_groups = [openstack_networking_secgroup_v2.secgroup.name, "default"]
  
  # Configuration Drive activé
  config_drive = true

  network {
    name = var.network_test
  }

  network {
    name = var.network_external
  }

  metadata = {
    purpose = "nginx-webserver"
    project = "cloud-computing"
  }

  # User data pour Configuration Drive - format bash simple
  user_data = file("${path.module}/config_script.sh")
}
