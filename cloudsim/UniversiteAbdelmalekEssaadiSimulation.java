package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * Simulation CloudSim pour l'Université Abdelmalek Essaadi
 * Architecture Cloud Computing - Projet Prof. C. EL AMRANI
 */
public class UniversiteAbdelmalekEssaadiSimulation {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;

    public static void main(String[] args) {
        Log.printLine("==============================================");
        Log.printLine("Simulation Cloud - Université Abdelmalek Essaadi");
        Log.printLine("==============================================");

        try {
            // 1. Initialiser CloudSim
            int num_user = 92500;  // Nombre total d'utilisateurs
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // Traçage des événements

            CloudSim.init(num_user, calendar, trace_flag);

            // 2. Créer les Datacenters
            Datacenter datacenterTetouan = createDatacenterTetouan("Datacenter_Tetouan");
            Datacenter datacenterTanger = createDatacenterTanger("Datacenter_Tanger");

            // 3. Créer le Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // 4. Créer les VMs (Machines Virtuelles)
            vmList = createVMs(brokerId);
            broker.submitVmList(vmList);

            // 5. Créer les Cloudlets (Applications/Tâches)
            cloudletList = createCloudlets(brokerId);
            broker.submitCloudletList(cloudletList);

            // 6. Démarrer la simulation
            CloudSim.startSimulation();

            // 7. Arrêter la simulation et afficher les résultats
            List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            // 8. Afficher les résultats
            printCloudletList(finishedCloudlets);
            printVmUtilization(vmList);
            printStatistics(finishedCloudlets);

            Log.printLine("==============================================");
            Log.printLine("Simulation terminée avec succès!");
            Log.printLine("==============================================");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Erreur lors de la simulation!");
        }
    }

    /**
     * Créer le Datacenter de Tétouan (Principal)
     * OPTIMISÉ: Réduit à 20 hôtes au lieu de 100
     */
    private static Datacenter createDatacenterTetouan(String name) {
        List<Host> hostList = new ArrayList<>();

        // Type 1: Hôtes Haute Performance (10 hôtes au lieu de 30)
        for (int i = 0; i < 10; i++) {
            List<Pe> peList = new ArrayList<>();
            int mips = 2400; // MIPS par core
            for (int j = 0; j < 24; j++) { // 24 cores
                peList.add(new Pe(j, new PeProvisionerSimple(mips)));
            }

            int ram = 262144; // 256 GB en MB
            long storage = 4000000; // 4 TB en MB
            int bw = 10000; // 10 Gbps

            hostList.add(new Host(
                    i,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList)
            ));
        }

        // Type 2: Hôtes Standard (10 hôtes au lieu de 50)
        for (int i = 10; i < 20; i++) {
            List<Pe> peList = new ArrayList<>();
            int mips = 2000;
            for (int j = 0; j < 16; j++) { // 16 cores
                peList.add(new Pe(j, new PeProvisionerSimple(mips)));
            }

            int ram = 131072; // 128 GB
            long storage = 2000000; // 2 TB
            int bw = 10000;

            hostList.add(new Host(
                    i,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList)
            ));
        }

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 1.0; // GMT+1 (Morocco)
        double cost = 3.0; // Coût par seconde
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.01;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw
        );

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Créer le Datacenter de Tanger (Secondaire)
     * OPTIMISÉ: Réduit à 10 hôtes au lieu de 50
     */
    private static Datacenter createDatacenterTanger(String name) {
        List<Host> hostList = new ArrayList<>();

        // Hôtes Standard pour Tanger (10 hôtes au lieu de 50)
        for (int i = 0; i < 10; i++) {
            List<Pe> peList = new ArrayList<>();
            int mips = 2000;
            for (int j = 0; j < 16; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(mips)));
            }

            int ram = 131072; // 128 GB
            long storage = 2000000; // 2 TB
            int bw = 10000;

            hostList.add(new Host(
                    i + 100, // Offset pour éviter conflits d'ID
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList)
            ));
        }

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 1.0;
        double cost = 2.5;
        double costPerMem = 0.04;
        double costPerStorage = 0.001;
        double costPerBw = 0.01;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw
        );

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Créer les VMs pour les différentes applications
     * MAINTENU: 22 VMs (pas de changement ici)
     */
    private static List<Vm> createVMs(int brokerId) {
        List<Vm> vms = new ArrayList<>();
        int vmid = 0;

        // VMs pour APOGEE (4 instances)
        for (int i = 0; i < 4; i++) {
            vms.add(new Vm(
                    vmid++, brokerId, 2400, 8, // 8 vCPUs
                    32768, 1000, 500000, // 32 GB RAM, 500 GB storage
                    "Ubuntu 22.04", new CloudletSchedulerSpaceShared()
            ));
        }

        // VMs pour Moodle (8 instances)
        for (int i = 0; i < 8; i++) {
            vms.add(new Vm(
                    vmid++, brokerId, 2400, 16, // 16 vCPUs
                    65536, 2000, 2000000, // 64 GB RAM, 2 TB storage
                    "Ubuntu 22.04", new CloudletSchedulerTimeShared()
            ));
        }

        // VMs pour Messagerie (2 instances)
        for (int i = 0; i < 2; i++) {
            vms.add(new Vm(
                    vmid++, brokerId, 2000, 4, // 4 vCPUs
                    16384, 1000, 5000000, // 16 GB RAM, 5 TB storage
                    "Ubuntu 22.04", new CloudletSchedulerTimeShared()
            ));
        }

        // VMs pour Bibliothèque Numérique (2 instances)
        for (int i = 0; i < 2; i++) {
            vms.add(new Vm(
                    vmid++, brokerId, 2000, 4, // 4 vCPUs
                    16384, 1000, 10000000, // 16 GB RAM, 10 TB storage
                    "Ubuntu 22.04", new CloudletSchedulerTimeShared()
            ));
        }

        // VMs pour Visioconférence (4 instances)
        for (int i = 0; i < 4; i++) {
            vms.add(new Vm(
                    vmid++, brokerId, 2400, 32, // 32 vCPUs
                    131072, 5000, 500000, // 128 GB RAM, 500 GB storage
                    "Ubuntu 22.04", new CloudletSchedulerTimeShared()
            ));
        }

        // VMs pour Portail Admin (2 instances)
        for (int i = 0; i < 2; i++) {
            vms.add(new Vm(
                    vmid++, brokerId, 2000, 4, // 4 vCPUs
                    16384, 1000, 1000000, // 16 GB RAM, 1 TB storage
                    "Ubuntu 22.04", new CloudletSchedulerTimeShared()
            ));
        }

        return vms;
    }

    /**
     * Créer les Cloudlets (tâches/applications)
     * OPTIMISÉ: Réduit de 122,700 à 2,000 cloudlets (98% de réduction!)
     */
    private static List<Cloudlet> createCloudlets(int brokerId) {
        List<Cloudlet> cloudlets = new ArrayList<>();
        int cloudletId = 0;
        long length;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        // APOGEE - 300 tâches représentant 30000 connexions (échelle 1:100)
        for (int i = 0; i < 300; i++) {
            length = 400000;
            Cloudlet cloudlet = new Cloudlet(
                    cloudletId++, length, pesNumber, fileSize, outputSize,
                    utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }

        // Moodle - 400 tâches représentant 40000 connexions (échelle 1:100)
        for (int i = 0; i < 400; i++) {
            length = 600000;
            Cloudlet cloudlet = new Cloudlet(
                    cloudletId++, length, pesNumber, fileSize * 2, outputSize * 2,
                    utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }

        // Messagerie - 500 tâches représentant 50000 emails (échelle 1:100)
        for (int i = 0; i < 500; i++) {
            length = 100000;
            Cloudlet cloudlet = new Cloudlet(
                    cloudletId++, length, pesNumber, 50, 50,
                    utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }

        // Bibliothèque - 100 tâches représentant 2000 accès (échelle 1:20)
        for (int i = 0; i < 100; i++) {
            length = 300000;
            Cloudlet cloudlet = new Cloudlet(
                    cloudletId++, length, pesNumber, fileSize * 5, outputSize * 5,
                    utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }

        // Visioconférence - 200 tâches (même nombre, très gourmand)
        for (int i = 0; i < 200; i++) {
            length = 2000000;
            Cloudlet cloudlet = new Cloudlet(
                    cloudletId++, length, pesNumber * 4, fileSize * 10, outputSize * 10,
                    utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }

        // Portail Admin - 500 tâches (même nombre)
        for (int i = 0; i < 500; i++) {
            length = 200000;
            Cloudlet cloudlet = new Cloudlet(
                    cloudletId++, length, pesNumber, fileSize, outputSize,
                    utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }

        Log.printLine("Total Cloudlets créés: " + cloudlets.size() + " (représentant ~122,000 connexions réelles)");
        return cloudlets;
    }

    /**
     * Créer le Broker
     */
    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("UniversiteBroker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Afficher les résultats des Cloudlets
     * OPTIMISÉ: Affiche seulement les 50 premières tâches
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== RÉSULTATS DE LA SIMULATION ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Datacenter" + indent + "VM ID" + indent + "Temps" + indent
                + "Début" + indent + "Fin");

        DecimalFormat dft = new DecimalFormat("###.##");

        // Afficher seulement les 50 premières pour éviter un output trop long
        int displayLimit = Math.min(50, size);
        for (int i = 0; i < displayLimit; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCÈS");
                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

        if (size > displayLimit) {
            Log.printLine("... (" + (size - displayLimit) + " autres tâches réussies non affichées)");
        }
    }

    /**
     * Afficher l'utilisation des VMs
     */
    private static void printVmUtilization(List<Vm> vms) {
        Log.printLine();
        Log.printLine("========== UTILISATION DES VMs ==========");
        Log.printLine("VM ID\tMIPS\tvCPUs\tRAM(MB)\tBW\tSize(MB)");

        for (Vm vm : vms) {
            Log.printLine(vm.getId() + "\t" + vm.getMips() + "\t"
                    + vm.getNumberOfPes() + "\t" + vm.getRam() + "\t"
                    + vm.getBw() + "\t" + vm.getSize());
        }
    }

    /**
     * Afficher les statistiques globales
     */
    private static void printStatistics(List<Cloudlet> list) {
        Log.printLine();
        Log.printLine("========== STATISTIQUES GLOBALES ==========");

        int totalCloudlets = list.size();
        int successCloudlets = 0;
        double totalExecutionTime = 0;
        double minTime = Double.MAX_VALUE;
        double maxTime = 0;

        for (Cloudlet cloudlet : list) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                successCloudlets++;
                double execTime = cloudlet.getActualCPUTime();
                totalExecutionTime += execTime;

                if (execTime < minTime) minTime = execTime;
                if (execTime > maxTime) maxTime = execTime;
            }
        }

        double avgExecutionTime = totalExecutionTime / successCloudlets;
        double successRate = (successCloudlets * 100.0) / totalCloudlets;

        DecimalFormat dft = new DecimalFormat("###.##");

        Log.printLine("Nombre total de tâches: " + totalCloudlets);
        Log.printLine("Tâches réussies: " + successCloudlets);
        Log.printLine("Taux de succès: " + dft.format(successRate) + "%");
        Log.printLine("Temps d'exécution moyen: " + dft.format(avgExecutionTime) + " sec");
        Log.printLine("Temps d'exécution min: " + dft.format(minTime) + " sec");
        Log.printLine("Temps d'exécution max: " + dft.format(maxTime) + " sec");
        Log.printLine("Temps d'exécution total: " + dft.format(totalExecutionTime) + " sec");

        // Calcul de la disponibilité (SLA)
        double availability = successRate;
        Log.printLine();
        Log.printLine("========== SLA ==========");
        Log.printLine("Disponibilité: " + dft.format(availability) + "%");
        Log.printLine("Objectif SLA: 99.5%");

        if (availability >= 99.5) {
            Log.printLine("✓ SLA RESPECTÉ");
        } else {
            Log.printLine("✗ SLA NON RESPECTÉ");
        }
    }
}