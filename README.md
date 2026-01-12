# Projet Cloud Computing - Université Abdelmalek Essaadi

## 📋 Description

Projet académique de Cloud Computing réalisé dans le cadre du cours du Prof. C. EL AMRANI.

Architecture Cloud complète pour l'Université Abdelmalek Essaadi (92,500 utilisateurs) couvrant :
- Simulation et dimensionnement avec CloudSim
- Déploiement sur OpenStack (MicroStack)
- Automatisation avec Terraform et Ansible
- Surveillance et SLA avec Python

## 🎯 Objectifs du Projet

1. **CloudSim** - Concevoir et simuler une architecture Cloud hybride
2. **OpenStack** - Déployer IaaS (CirrOS) et SaaS (Nginx)
3. **Terraform & Ansible** - Automatiser le déploiement d'infrastructure
4. **SLA** - Surveiller la disponibilité des instances (objectif 99.5%)

## 🏗️ Architecture

- **2 Datacenters** : Tétouan (principal) + Tanger (backup)
- **150 hôtes physiques** : 100 + 50
- **22 VMs** pour 6 applications : APOGEE, Moodle, Messagerie, Bibliothèque, Visioconférence, Portail Admin
- **122,700 tâches simulées** avec CloudSim

## 🛠️ Technologies Utilisées

- **Simulation** : CloudSim 3.0.3, Java JDK 21
- **Infrastructure** : OpenStack (MicroStack), Ubuntu 22.04
- **IaC** : Terraform 1.x
- **Automatisation** : Ansible 2.x
- **Monitoring** : Python 3, OpenStack SDK
- **Services** : Nginx, CirrOS

## 📁 Structure du Projet

```
.
├── cloudsim/                    # Simulation CloudSim
│   └── UniversiteAbdelmalekEssaadiSimulation.java
├── terraform-ansible/           # Infrastructure as Code
│   ├── clouds.yaml
│   ├── provider.tf
│   ├── variables.tf
│   ├── main.tf
│   ├── outputs.tf
│   ├── inventory.ini
│   ├── ansible.cfg
│   └── nginx_playbook.yml
├── sla-monitoring/              # Surveillance SLA
│   ├── sla.txt
│   └── monitor_sla.py
└── rapport/                     # Documentation
    └── Rapport_Cloud_Computing.pdf
```

## 🚀 Installation et Utilisation

### 1. CloudSim

```bash
# Compiler et exécuter la simulation
cd cloudsim/
javac -cp cloudsim-3.0.3.jar UniversiteAbdelmalekEssaadiSimulation.java
java -cp cloudsim-3.0.3.jar:. UniversiteAbdelmalekEssaadiSimulation
```

### 2. OpenStack (MicroStack)

```bash
# Installer MicroStack
sudo snap install microstack --beta
sudo microstack init --auto --control
```

### 3. Terraform

```bash
cd terraform-ansible/
terraform init
terraform plan
terraform apply
```

### 4. Ansible

```bash
ansible webservers -m ping
ansible-playbook nginx_playbook.yml
```

### 5. Monitoring SLA

```bash
cd sla-monitoring/
python3 monitor_sla.py
```

## 📊 Résultats

- **Simulation CloudSim** : 100% de taux de succès, SLA respecté
- **Disponibilité OpenStack** : 99.67% > objectif 99.5%
- **Déploiement automatisé** : VM + Nginx opérationnels
- **Infrastructure validée** pour 92,500 utilisateurs

## 👤 Auteur

**Oumaima Boughdad**  
Faculté des Sciences et Techniques - Tanger  
Département Génie Informatique  
Université Abdelmalek Essaadi

## 📝 Encadrement

**Prof. Chaker EL AMRANI**  
Cloud Computing - 2025/2026

## 📄 Licence

Projet académique - Université Abdelmalek Essaadi

---

*Pour plus de détails, consultez le rapport complet dans le dossier `/rapport`*
