output "instance_id" {
  description = "ID de l'instance créée"
  value       = openstack_compute_instance_v2.ubuntu_instance.id
}

output "instance_name" {
  description = "Nom de l'instance"
  value       = openstack_compute_instance_v2.ubuntu_instance.name
}

output "instance_status" {
  description = "Statut de l'instance"
  value       = openstack_compute_instance_v2.ubuntu_instance.power_state
}

output "network_test_ip" {
  description = "IP sur le réseau test"
  value       = openstack_compute_instance_v2.ubuntu_instance.network[0].fixed_ip_v4
}

output "network_external_ip" {
  description = "IP sur le réseau external"
  value       = length(openstack_compute_instance_v2.ubuntu_instance.network) > 1 ? openstack_compute_instance_v2.ubuntu_instance.network[1].fixed_ip_v4 : "N/A"
}

output "all_networks" {
  description = "Toutes les IPs"
  value       = openstack_compute_instance_v2.ubuntu_instance.network
}

output "ssh_command_test" {
  description = "Commande SSH (réseau test)"
  value       = "ssh ubuntu@${openstack_compute_instance_v2.ubuntu_instance.network[0].fixed_ip_v4}"
}

output "ssh_command_external" {
  description = "Commande SSH (réseau external)"
  value       = length(openstack_compute_instance_v2.ubuntu_instance.network) > 1 ? "ssh ubuntu@${openstack_compute_instance_v2.ubuntu_instance.network[1].fixed_ip_v4}" : "N/A"
}

output "login_info" {
  description = "Informations de connexion"
  value = {
    username = "ubuntu"
    password = "pass1234"
    sudo     = "yes (NOPASSWD)"
  }
  sensitive = true
}
