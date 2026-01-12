#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SLA Monitoring Script for MicroStack OpenStack Instances
Adapté à la configuration détectée de votre système
Université Abdelmalek Essaadi - Cloud Computing Project
"""

import os
import sys
import time
import datetime
from collections import defaultdict
from openstack import connection
from openstack import exceptions
import warnings

# Fix encoding pour le terminal
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

# Désactiver les avertissements SSL si nécessaire
warnings.filterwarnings('ignore', message='Unverified HTTPS request')

# Configuration basée sur votre diagnostic
SLA_FILE = "sla.txt"
MONITORING_INTERVAL = 300  # 5 minutes (300 secondes)
SLA_TARGET = 99.5  # Objectif de disponibilité en %
CHECKS_PER_DAY = 288  # 24h * 60min / 5min

# Vos credentials RÉELS détectés
AUTH_URL = "https://10.0.2.15:5000/v3"
USERNAME = "admin"
PASSWORD = "uM6LNAw7Euh2pPbiii7docuLOTtgMrSt"  # Votre vrai password
PROJECT_NAME = "admin"
USER_DOMAIN = "Default"
PROJECT_DOMAIN = "Default"
REGION = "microstack"
CACERT = "/var/snap/microstack/common/etc/ssl/certs/cacert.pem"


class SLAMonitor:
    def __init__(self):
        """Initialize OpenStack connection avec VOS credentials"""
        print("Initialisation de la connexion OpenStack...")
        
        # Vérifier si le certificat existe
        verify_ssl = CACERT if os.path.exists(CACERT) else False
        
        try:
            self.conn = connection.Connection(
                auth_url=AUTH_URL,
                project_name=PROJECT_NAME,
                username=USERNAME,
                password=PASSWORD,
                user_domain_name=USER_DOMAIN,
                project_domain_name=PROJECT_DOMAIN,
                region_name=REGION,
                verify=verify_ssl
            )
            print("OK Connexion initialisée")
        except Exception as e:
            print(f"ERREUR Erreur d'initialisation: {e}")
            raise
        
        self.monitoring_data = defaultdict(list)
        self.start_time = datetime.datetime.now()
        
    def test_connection(self):
        """Test de la connexion OpenStack"""
        try:
            print("\n" + "="*70)
            print("TEST DE CONNEXION OPENSTACK")
            print("="*70)
            
            # Lister les instances
            instances = list(self.conn.compute.servers())
            print(f"OK Connexion réussie!")
            print(f"OK {len(instances)} instance(s) détectée(s):\n")
            
            for inst in instances:
                print(f"  - {inst.name}")
                print(f"    ID: {inst.id}")
                print(f"    Status: {inst.status}")
                print(f"    Image: {inst.image.get('id', 'N/A') if inst.image else 'N/A'}")
                print()
            
            if not instances:
                print("ATTENTION: Aucune instance trouvée!")
                print("   Créez des instances avec: microstack.openstack server create ...")
                return False
            
            # Vérifier si au moins une instance est active
            active_count = sum(1 for inst in instances if inst.status == 'ACTIVE')
            shutoff_count = sum(1 for inst in instances if inst.status == 'SHUTOFF')
            
            print(f"Résumé:")
            print(f"  - Instances ACTIVE: {active_count}")
            print(f"  - Instances SHUTOFF: {shutoff_count}")
            
            if active_count == 0:
                print("\nATTENTION: Toutes les instances sont arrêtées!")
                print("   Démarrez-les avec:")
                for inst in instances:
                    print(f"   microstack.openstack server start {inst.name}")
                print()
            
            return True
            
        except exceptions.HttpException as e:
            print(f"ERREUR HTTP: {e}")
            print("  Vérifiez que les services OpenStack sont actifs")
            return False
        except Exception as e:
            print(f"ERREUR de connexion: {e}")
            print(f"  Type d'erreur: {type(e).__name__}")
            return False
    
    def check_instance_status(self, instance):
        """Vérifie si une instance est disponible (ACTIVE)"""
        try:
            # Récupérer l'état actuel de l'instance
            server = self.conn.compute.get_server(instance.id)
            
            # Une instance est considérée "UP" si elle est ACTIVE
            is_up = server.status == 'ACTIVE'
            
            return is_up, server.status
            
        except exceptions.ResourceNotFound:
            print(f"  ATTENTION Instance {instance.name} non trouvée")
            return False, "NOT_FOUND"
        except Exception as e:
            print(f"  ERREUR pour {instance.name}: {e}")
            return False, "ERROR"
    
    def monitor_instances(self):
        """Surveille toutes les instances"""
        timestamp = datetime.datetime.now()
        print(f"\n{'='*70}")
        print(f"CHECK #{len(self.monitoring_data.get('check_times', [])) + 1}")
        print(f"Timestamp: {timestamp.strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"{'='*70}")
        
        try:
            instances = list(self.conn.compute.servers())
            
            if not instances:
                print("ATTENTION Aucune instance à surveiller")
                return
            
            # Stocker le timestamp du check
            if 'check_times' not in self.monitoring_data:
                self.monitoring_data['check_times'] = []
            self.monitoring_data['check_times'].append(timestamp)
            
            for instance in instances:
                is_available, status = self.check_instance_status(instance)
                
                self.monitoring_data[instance.id].append({
                    'timestamp': timestamp,
                    'name': instance.name,
                    'status': status,
                    'available': is_available
                })
                
                status_symbol = "[OK]" if is_available else "[KO]"
                print(f"  {status_symbol} {instance.name:25} | {status:10} | {'UP' if is_available else 'DOWN'}")
            
            print(f"{'='*70}")
            
        except Exception as e:
            print(f"ERREUR pendant le monitoring: {e}")
    
    def calculate_daily_availability(self, instance_id):
        """Calcule la disponibilité sur les dernières 24h"""
        now = datetime.datetime.now()
        yesterday = now - datetime.timedelta(days=1)
        
        # Filtrer les checks des dernières 24h
        recent_checks = [
            check for check in self.monitoring_data[instance_id]
            if check['timestamp'] >= yesterday
        ]
        
        if not recent_checks:
            return None
        
        total_checks = len(recent_checks)
        up_checks = sum(1 for check in recent_checks if check['available'])
        down_checks = total_checks - up_checks
        
        availability = (up_checks / total_checks) * 100
        downtime_minutes = (down_checks * MONITORING_INTERVAL) / 60
        
        return {
            'instance_name': recent_checks[0]['name'],
            'availability': availability,
            'total_checks': total_checks,
            'up_checks': up_checks,
            'down_checks': down_checks,
            'downtime_minutes': downtime_minutes,
            'compliant': availability >= SLA_TARGET
        }
    
    def generate_report(self):
        """Génère un rapport de disponibilité"""
        now = datetime.datetime.now()
        report_date = now.strftime('%Y-%m-%d %H:%M:%S')
        
        print(f"\n{'='*70}")
        print(f"RAPPORT SLA - {report_date}")
        print(f"{'='*70}")
        print(f"Objectif SLA: {SLA_TARGET}% de disponibilité")
        print(f"Downtime maximum autorisé: 7.2 minutes par jour")
        print(f"{'='*70}\n")
        
        report_lines = []
        
        for instance_id in self.monitoring_data.keys():
            if instance_id == 'check_times':
                continue
            
            checks = self.monitoring_data[instance_id]
            if not checks:
                continue
            
            # Calculer les stats depuis le début du monitoring
            total_checks = len(checks)
            up_checks = sum(1 for check in checks if check['available'])
            down_checks = total_checks - up_checks
            
            if total_checks == 0:
                continue
            
            availability = (up_checks / total_checks) * 100
            downtime_minutes = (down_checks * MONITORING_INTERVAL) / 60
            is_compliant = availability >= SLA_TARGET
            
            instance_name = checks[0]['name']
            status = "COMPLIANT [OK]" if is_compliant else "VIOLATION [KO]"
            
            print(f"Instance: {instance_name}")
            print(f"  Disponibilité: {availability:.2f}%")
            print(f"  Checks total: {total_checks}")
            print(f"  UP / DOWN: {up_checks} / {down_checks}")
            print(f"  Downtime: {downtime_minutes:.2f} minutes")
            print(f"  Statut SLA: {status}")
            print()
            
            # Ligne pour le fichier SLA
            report_line = (
                f"{now.strftime('%Y-%m-%d %H:%M'):<20}| "
                f"{instance_name:20} | "
                f"{availability:6.2f}%     | "
                f"{status:15} | "
                f"{downtime_minutes:6.2f} min"
            )
            report_lines.append(report_line)
        
        # Ajouter au fichier SLA
        if report_lines:
            self.append_to_sla_file(report_lines)
        
        print(f"{'='*70}")
        return report_lines
    
    def append_to_sla_file(self, report_lines):
        """Ajoute les résultats au fichier SLA"""
        try:
            with open(SLA_FILE, 'a', encoding='utf-8') as f:
                for line in report_lines:
                    f.write(line + '\n')
            print(f"OK Rapport ajouté à {SLA_FILE}\n")
        except Exception as e:
            print(f"ERREUR d'écriture dans {SLA_FILE}: {e}")
    
    def run_monitoring(self, duration_minutes=None, num_checks=None):
        """
        Lance le monitoring
        duration_minutes: durée totale en minutes
        num_checks: nombre de checks à effectuer (pour tests)
        """
        print(f"\n{'='*70}")
        print("DÉMARRAGE DU MONITORING SLA")
        print(f"{'='*70}")
        print(f"Intervalle de vérification: {MONITORING_INTERVAL} secondes ({MONITORING_INTERVAL/60:.0f} minutes)")
        print(f"Objectif SLA: {SLA_TARGET}%")
        
        if num_checks:
            print(f"Mode TEST: {num_checks} checks")
        elif duration_minutes:
            print(f"Durée: {duration_minutes} minutes")
        else:
            print("Mode: Continu (Ctrl+C pour arrêter)")
        
        print(f"{'='*70}\n")
        
        check_count = 0
        
        try:
            while True:
                # Effectuer le monitoring
                self.monitor_instances()
                check_count += 1
                
                # Générer un rapport tous les 10 checks (ou à la fin)
                if check_count % 10 == 0 or (num_checks and check_count >= num_checks):
                    self.generate_report()
                
                # Vérifier les conditions d'arrêt
                if num_checks and check_count >= num_checks:
                    print(f"\nOK {num_checks} checks effectués - Fin du test")
                    break
                
                if duration_minutes:
                    elapsed = (datetime.datetime.now() - self.start_time).total_seconds() / 60
                    if elapsed >= duration_minutes:
                        print(f"\nOK Durée de {duration_minutes} minutes atteinte")
                        break
                
                # Attendre avant le prochain check
                print(f"\nProchain check dans {MONITORING_INTERVAL} secondes...")
                time.sleep(MONITORING_INTERVAL)
                
        except KeyboardInterrupt:
            print("\n\nATTENTION Monitoring arrêté par l'utilisateur (Ctrl+C)")
        finally:
            print("\nGénération du rapport final...")
            self.generate_report()
            print("\nOK Monitoring terminé")


def main():
    """Fonction principale"""
    print("="*70)
    print("SYSTÈME DE MONITORING SLA - OPENSTACK MICROSTACK")
    print("Université Abdelmalek Essaadi")
    print("Projet Cloud Computing - Prof. C. EL AMRANI")
    print("="*70)
    
    monitor = SLAMonitor()
    
    # Test de connexion
    if not monitor.test_connection():
        print("\nERREUR Impossible de se connecter à OpenStack")
        print("Vérifiez que les services sont actifs:")
        print("  sudo snap services microstack")
        return
    
    print("\n" + "="*70)
    print("OPTIONS DE MONITORING")
    print("="*70)
    print("1. Test rapide (5 checks, ~5 minutes)")
    print("2. Monitoring 1 heure")
    print("3. Monitoring 24 heures (production)")
    print("4. Monitoring continu")
    print("="*70)
    
    try:
        choice = input("\nChoisissez une option (1-4): ").strip()
        
        if choice == '1':
            print("\n[TEST] Mode TEST - 5 checks")
            monitor.run_monitoring(num_checks=5)
        elif choice == '2':
            print("\n[1H] Monitoring 1 heure")
            monitor.run_monitoring(duration_minutes=60)
        elif choice == '3':
            print("\n[24H] Monitoring 24 heures")
            monitor.run_monitoring(duration_minutes=1440)
        elif choice == '4':
            print("\n[CONTINU] Monitoring continu (Ctrl+C pour arrêter)")
            monitor.run_monitoring()
        else:
            print("Option invalide, mode TEST par défaut")
            monitor.run_monitoring(num_checks=5)
            
    except KeyboardInterrupt:
        print("\n\nArrêt du programme")


if __name__ == "__main__":
    main()
