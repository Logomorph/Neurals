package base;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nn_patterns.PatternGenerator;

import util.GraphCSVWriter;
import aco.ACOAlgorithm;
import aco.entities.Bin;
import aco.entities.Item;
import aco.entities.Resource;
import dclink_entities.HostData;
import dclink_if.DCLink;
import dclink_if.VMMonitor;

public class Base {

    private DCLink dcMonitor = new DCLink();
    private final static ACOAlgorithm aco = new ACOAlgorithm();
    private Timer predictionTimer;
    private Timer acoTimer;
    private final Lock lock = new ReentrantLock();
    private List<PredictionBox> pboxes;
    private int predEpoch, acoEpoch;
    private PredictionQueue itemsQueue;
    GraphCSVWriter graphCSV;
    PatternGenerator pgen;
    private static int PREDICTION_INTERVAL = 30000; // ms
    private static int ACO_INTERVAL = 10000; // ms
    public static List<Item> overflowItems;
    private List<Item> leftoverItems;
    private List<Item> itemsToMigrate;
    private int[] defaultDemand;

    public Base() {
        itemsToMigrate = new ArrayList<Item>();
        leftoverItems = new ArrayList<Item>();
        graphCSV = new GraphCSVWriter("vms.csv");

        itemsQueue = new PredictionQueue();

//		LinkedList<Integer> numbers = (LinkedList<Integer>) InputReader
//				.readData();

        // /////////////////
        int noBins = 0;
        for (HostData host : dcMonitor.getHosts()) {
            if (!host.getStatus().equals("ERROR")
                    && !host.getStatus().equals("DISABLED")
                    && !host.getStatus().equals("MONITORING_ERROR")) {
                noBins++;

            }
        }
        ACOAlgorithm.NB_OF_BINS = noBins;
        int[] resourceCapacity = new int[Resource.values().length - 1];
        List<HostData> hosts = dcMonitor.getHosts();
        // ACOAlgorithm.NB_OF_BINS = numbers.get(0);

        resourceCapacity[Resource.CPU.getIndex()] = hosts.get(0).getMaxCPU();
        resourceCapacity[Resource.RAM.getIndex()] = (int) Math.ceil(hosts.get(0).getMaxMemory() / 1024);
        resourceCapacity[Resource.STORAGE.getIndex()] = 250; // 250gb
        resourceCapacity[Resource.NETWORK_TRANSFER_SPEED.getIndex()] = 100; //numbers.get(5); // 100 mb/s

        aco.initalizeBinsData(resourceCapacity);

        List<Bin> bins = aco.getBins();
        int j = 0;
        for (int i = 0; i < ACOAlgorithm.NB_OF_BINS;) {
            if (!hosts.get(j).getStatus().equals("ERROR")
                    && !hosts.get(j).getStatus().equals("DISABLED")
                    && !hosts.get(j).getStatus().equals("MONITORING_ERROR")) {

                bins.get(i).setNaturalId(Integer.parseInt(hosts.get(j).getId()));
                i++;
            }
            j++;
        }
        for (Bin bin : bins) {
            System.out.println("Bin "
                    + bin.getNaturalId()
                    + " : "
                    + bin.getResourceCapacity()[Resource.CPU.getIndex()]
                    + ","
                    + bin.getResourceCapacity()[Resource.RAM.getIndex()]
                    + ","
                    + bin.getResourceCapacity()[Resource.STORAGE.getIndex()]
                    + ","
                    + bin.getResourceCapacity()[Resource.NETWORK_TRANSFER_SPEED
                    .getIndex()] + ",");
        }
        // //////////
        aco.setNbOfItems(dcMonitor.getVMs().size());
        List<Item> items = new ArrayList<Item>();
        pboxes = new ArrayList<PredictionBox>();
        Item i;

        List<Integer> vms = dcMonitor.getVMs();
        for (int s = 0; s < vms.size(); s++) {
            VMMonitor vm = dcMonitor.getVMMonitor(vms.get(s));
            int[] resourceDemand = new int[Resource.values().length];
            resourceDemand[Resource.CPU.getIndex()] = vm.getCpu()*100;// numbers.get(s);
            resourceDemand[Resource.RAM.getIndex()] = vm.getRam();// numbers.get(s
            // + 1);
            resourceDemand[Resource.STORAGE.getIndex()] = 1;// numbers.get(s
            // + 2);
            resourceDemand[Resource.NETWORK_TRANSFER_SPEED.getIndex()] = 10; // numbers.get(s
            // +
            // 3);
            resourceDemand[Resource.RUN_TIME.getIndex()] = 30000 + (int) (Math
                    .random() * 10000);// numbers.get(s + 4);

            i = new Item();
            i.setResourceDemand(resourceDemand);
            items.add(i);
            aco.setItems(items);

            PredictionBox pboxBuff = new PredictionBox(i,
                    graphCSV, vm);
            pboxes.add(pboxBuff);
        }
        defaultDemand = items.get(0).getResourceDemand();
        System.out.println("Resource demand ");
        for ( j = 0; j < Resource.values().length; j++) {
            System.out.print(defaultDemand[j] + " ");
        }
        predEpoch = 0;
        acoEpoch = 0;
        overflowItems = new ArrayList<Item>();
    }

    public void start() {
        predictionTimer = new Timer();
        predictionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updatePrediction();
            }
        }, 10000, PREDICTION_INTERVAL);

        acoTimer = new Timer();
        acoTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateACO();
            }
        }, 10000, ACO_INTERVAL);
    }

    public void stop() {
        if (predictionTimer != null) {
            predictionTimer.cancel();
        }
        if (acoTimer != null) {
            acoTimer.cancel();
        }
    }

    private void updatePrediction() {
        lock.lock();
        List<Item> items = new ArrayList<Item>();
        System.out.println("---------------------------------");
        System.out.println("Prediction epoch " + predEpoch);
        System.out.println("---------------------------------");

        // update all the items via neural nets
        if (aco.getItems() != null && aco.getItems().size() > 0) {
            items = aco.getItems();
        }
        checkItemsTimer();
        System.out.println("Items size: " + items.size());
        for (int i = 0; i < pboxes.size(); i++) {
            pboxes.get(i).Update();
        }
        for (int i = 0; i < 10; i++) {
            if (aco.getAvailableResources() != null) {
                //instantiate VM
                if (pboxes.size() > 0 && i < pboxes.size()) {
                    items.add(new Item(pboxes.get(i).getItem()));
                } else {
                    Item item = new Item();
                    item.setResourceDemand(defaultDemand);
                    items.add(new Item(item));
                }

                VMMonitor vmMonitor = dcMonitor.instatiate();

                PredictionBox pboxBuff = new PredictionBox(items.get(items.size() - 1),
                        graphCSV, vmMonitor);
                pboxes.add(pboxBuff);

            }
        }
        itemsQueue.add(items);

        if (predEpoch == 4) {
            predictionTimer.cancel();
        }
        predEpoch++;
        lock.unlock();
    }

    private void updateACO() {
        lock.lock();
        try {
//            checkItemsTimer();
            List<Item> items = new ArrayList<Item>();
            System.out.println("---------------------------------");
            System.out.println("ACO epoch " + acoEpoch);
            System.out.println("---------------------------------");

            if (itemsQueue.hasItems()) {
                List<Item> queueFront = itemsQueue.popFront();
                aco.setItems(queueFront);
            } else {
                items = aco.getItems();
                checkItemsTimer();
                aco.setItems(items);
            }
            int[] originalBinIds = null;
            if (itemsToMigrate != null && itemsToMigrate.size() > 0) {
                aco.getLeftoverItems().removeAll(itemsToMigrate);
                originalBinIds = new int[itemsToMigrate.size()];
                int i = 0;
                for (Item item : itemsToMigrate) {
                    // System.out.println("Items to migrate before aco runs : "
                    // + item.getResourceDemand()[Resource.MIPS.getIndex()]
                    // + " in bin " + item.getDeploymentBin().getId());
                    originalBinIds[i] = item.getDeploymentBin().getId();
                    i++;
                }
            }
            for (Item item : aco.getItems()) {
                System.out.println("ACO item "
                        + aco.getItems().indexOf(item)
                        + " resource demands: "
                        + item.getResourceDemand()[Resource.CPU.getIndex()]
                        + ", "
                        + item.getResourceDemand()[Resource.RAM.getIndex()]
                        + ", "
                        + item.getResourceDemand()[Resource.STORAGE.getIndex()]
                        + ", "
                        + item.getResourceDemand()[Resource.NETWORK_TRANSFER_SPEED
                        .getIndex()] + ", "
                        + item.getResourceDemand()[Resource.RUN_TIME.getIndex()]
                        + ", ");
            }
            aco.setNbOfItems(aco.getItems().size());
            boolean flag = true;

            //	if (aco.getItems().size() != 0) {
            aco.init();
            aco.run();

            for (Item item : itemsToMigrate) {
                if (overflowItems.contains(item)) {
//                    System.out.println("Redo placement");
                    flag = false;
                    break;
                }
            }
//		}
            if (flag) {
                items = aco.getItems();
                int binIdBeforeMigrate, binIdAfterMigrate;

                if (items != null && items.size() > 0
                        && aco.getGlobalBestSolution() != null) {

                    int[][] globalBestSolution = aco.getGlobalBestSolution();
                    List<Bin> bins = aco.getBins();
                    for (int row = 0; row < aco.getNbOfItems(); row++) {
                        for (int col = 0; col < ACOAlgorithm.NB_OF_BINS; col++) {

                            if (globalBestSolution[row][col] != 0) {
                                // deploy VM in corresponding Machine
                                System.out.println("Item " + row
                                        + " assigned to bin " + col);
                                items.get(row).setDeploymentBin(bins.get(col));

                                if (items.get(row).getEndRunTime() == null
                                        || (items.get(row).getEndRunTime() != null && !items
                                        .get(row).getEndRunTime()
                                        .isRunning())) {
                                    pboxes.get(row).getVmm().deploy(items.get(row).getDeploymentBin().getNaturalId());
                                    items.get(row).start();
                                }
                                globalBestSolution[row][col] = 0;
                                break;
                            }
                        }
                    }
                }
                itemsToMigrate = aco.getItemsToMigrate();
                int i = 0;
                for (Item item : itemsToMigrate) {
                    binIdBeforeMigrate = originalBinIds[i++];
                    binIdAfterMigrate = item
                            .getDeploymentBin().getId();

                    if (binIdAfterMigrate != binIdBeforeMigrate) {
                         System.out.println("Migrate item " + items.indexOf(item)
                                + " from " + binIdBeforeMigrate + " to "
                                + binIdAfterMigrate);
                        System.out.println("ACO started migrating item " + items.indexOf(item));
                        items.get(items.indexOf(item)).getEndRunTime().stop();
                        pboxes.get(items.indexOf(item)).getVmm().migrate(item
                                .getDeploymentBin().getNaturalId());
                       
                        items.get(items.indexOf(item)).getEndRunTime().start();
                    } else {
                        // no need to migrate
                        aco.getLeftoverItems().add(item);
                    }
                }
                aco.setItems(items);
            }

            if (acoEpoch == 18) {
                stop();
            }
            acoEpoch++;
        } finally {
            lock.unlock();
        }
    }

    public void checkItemsTimer() {
        List<Item> items = new ArrayList<Item>();
        items = aco.getItems();
        if (leftoverItems != null && leftoverItems.size() > 0) {
            leftoverItems.clear();
        }
        if (itemsToMigrate != null && itemsToMigrate.size() > 0) {
            itemsToMigrate.clear();
        }
        int row = 0;
        while (row < items.size() && items.size() > 0) {
            if (items.get(row).getEndRunTime() != null) {
                if (!items.get(row).getEndRunTime().isRunning()) {
                    System.out
                            .println("Remove item "
                            + row
                            + " after "
                            + items.get(row).getResourceDemand()[Resource.RUN_TIME
                            .getIndex()]);
                    items.remove(row);
                    // delete VM from pool
                    int id = pboxes.get(row).getVmm().getID();
                    System.out.println(1);
                    pboxes.get(row).getVmm().stopMonitoring();
                    System.out.println(2);
                    pboxes.get(row).getVmm().destroy();
                    System.out.println(3);
                    pboxes.remove(row);
                    System.out.println("ACO removed item " + id);


                    if (row == (items.size() + 1)) {
                        row--;
                    }
                } else {
                    leftoverItems.add(items.get(row));
                    if (items.get(row).getDeploymentBin().isMigrateTrigger() == true) {
                        itemsToMigrate.add(items.get(row));
                        System.out.println("Item " + row + " in bin "
                                + items.get(row).getDeploymentBin().getId()
                                + " set to migrate");
                    }
                    row++;
                }
            } else {
                row++;
            }
        }
        aco.setItems(items);
        aco.setLeftoverItems(leftoverItems);
        aco.setItemsToMigrate(itemsToMigrate);
    }
}
