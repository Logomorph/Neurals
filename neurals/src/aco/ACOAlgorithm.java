package aco;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import base.Base;

import util.Initializer;
import aco.entities.Bin;
import aco.entities.Item;
import aco.entities.Pheromones;
import aco.entities.Resource;

public class ACOAlgorithm {

    public static final double ALPHA = 1.0d;
    public static final double BETA = 2.0d;
    // heuristic parameters
    public static final double Q = 0.0001d; // somewhere between 0 and 1
    public static final double PHEROMONE_EVAPORATION = 0.3d; // between 0 and 1
    public static final double INITIAL_PHEROMONES = 0.8d; // can be anything
    public static final double G_CONST = 2.0d; // can be anything
    public static final int NB_OF_CYCLES = 2;
    public static final int NB_OF_ANTS = 5;
    public static int NB_OF_BINS;
    private int nbOfItems;
    private static double THRESHOLD = 0.3d;
    private double[][] pheromones;
    private double tauMax;
    private double tauMin;
    private double[][] deltaTauBest;
    private List<Bin> bins = new ArrayList<Bin>(NB_OF_BINS);
    private List<Bin> adjustedBins = new ArrayList<Bin>(NB_OF_BINS);
    private List<Item> items = new ArrayList<Item>(nbOfItems);
    private int[][] bestCycleSolution;
    private int[][] globalBestSolution;
    private int[][] x;
    private int bestNumberOfUsedBins;
    private double[][] probabilities;
    private double[][] niu;
    private List<Item> leftoverItems;
    private List<Item> itemsToMigrate;
    private int[] availableResources;

    public double[][] initializeNiu() {
        double[][] temp = new double[items.size()][bins.size()];
        int[] capacityVector;
        int[] binLoadVector;
        int[] demandVector;
        int i;
        int j;
        int l;
        for (Item item : items) {
            for (Bin bin : bins) {
                capacityVector = bin.getValueSet();
                binLoadVector = bin.getBinLoadVector();
                demandVector = item.getValueSet();
                l = capacityVector.length;
                i = items.indexOf(item);
                j = bins.indexOf(bin);
                for (int k = 0; k < l; k++) {
                    temp[i][j] = 1.0 / (Math.abs(capacityVector[k]
                            - (binLoadVector[k] + demandVector[k])));
                }
            }
        }
        return temp;
    }

    private double adjustNiu(int itemIndex, int binIndex) {
        int[] capacityVector = bins.get(binIndex).getValueSet();
        int[] binLoadVector = bins.get(binIndex).getBinLoadVector();
        int[] demandVector = items.get(itemIndex).getValueSet();

        int l = capacityVector.length;
        int[] vector = new int[l];
        for (int k = 0; k < l; k++) {
            vector[k] = (Math.abs(capacityVector[k]
                    - (binLoadVector[k] + demandVector[k])));
        }

        double value = 0.0;
        for (int k = 0; k < l; k++) {
            value = value + vector[k] * vector[k];
        }
        value = 1.0 / Math.sqrt(value);
        return value;
    }

    public void init() {
        pheromones = Initializer.initializePheromones(nbOfItems, NB_OF_BINS,
                INITIAL_PHEROMONES);
        bestCycleSolution = Initializer.initializeIndividualAntMatrix(
                nbOfItems, NB_OF_BINS);
        probabilities = Initializer.initializeMatrices(nbOfItems, NB_OF_BINS);
        deltaTauBest = Initializer.initializeDeltaTau(nbOfItems, NB_OF_BINS,
                INITIAL_PHEROMONES);
        niu = initializeNiu();
        availableResources = Initializer.initializeAvailableResources(
                bins.get(0), NB_OF_BINS);
    }

    public void adjustBinLoad(List<Item> leftoverItems) {
        int index;
        if (leftoverItems != null) {
            for (Item item : leftoverItems) {
                // if (!itemsToMigrate.contains(item)) {
                // System.out.println("Don't migrate item!");
                index = item.getDeploymentBin().getId();
                x[items.indexOf(item)][index] = 1;
                bins.get(index).setBinLoadVector(computeBinLoadVector(index));
                bins.get(index).setStatus(Bin.IS_ON);
            }
        }
    }

    public void run() {
       // System.out.println("aco runs");
        int q;
        int a;
        int v;
        int i;
        List<Item> copyOfItemSet = new ArrayList<Item>();
        List<Item> setOfQualifiedItems = new ArrayList<Item>();
//		int index;
        int[] binLoadVector;
        if (leftoverItems != null) {
            for (Item item : leftoverItems) {
                System.out.println("leftover item " + items.indexOf(item)
                        + " : "
                        + item.getResourceDemand()[Resource.CPU.getIndex()] + ", "
                        + item.getResourceDemand()[Resource.RAM.getIndex()] + ", "
                        + item.getResourceDemand()[Resource.STORAGE.getIndex()] + ", "
                        + item.getResourceDemand()[Resource.NETWORK_TRANSFER_SPEED.getIndex()] + ", "
                        + item.getResourceDemand()[Resource.RUN_TIME.getIndex()]);
            }
        }
        for (q = 0; q < NB_OF_CYCLES; q++) {
            x = Initializer.initializeIndividualAntMatrix(nbOfItems,
                    NB_OF_BINS);
            for (Bin bin : bins) {
                for (int k = 0; k < bin.getBinLoadVector().length; k++) {
                    bin.getBinLoadVector()[k] = 0;
                }
                bin.setStatus(Bin.IS_OFF);
            }

            adjustBinLoad(leftoverItems);

            for (a = 0; a < NB_OF_ANTS; a++) {
                copyOfItemSet.clear();
                for (Item item : items) {
                    copyOfItemSet.add(item);
                }
                v = 0;

                while (copyOfItemSet.size() > 0 && v < NB_OF_BINS) {
                    setOfQualifiedItems = determineSetOfQualifiedItems(v,
                            copyOfItemSet);

                    if (setOfQualifiedItems.size() > 0) {
                        double sum = 0.0;
                        for (Item item : setOfQualifiedItems) {
                            i = items.indexOf(item);
                            sum = sum
                                    + (Math.pow(pheromones[i][v], ALPHA) * Math
                                    .pow(niu[i][v], BETA));
                        }
                        i = chooseItemWithLargestProbability(sum, v,
                                setOfQualifiedItems);
                        if (bins.get(v).getStatus() == Bin.IS_OFF) {
                            bins.get(v).turnON();
                        }
                        x[i][v] = 1;
                        x[items.size()][v] = 1;
                        niu[i][v] = adjustNiu(i, v);
                        copyOfItemSet.remove(items.get(i));
                        setOfQualifiedItems.remove(items.get(i));
                        binLoadVector = computeBinLoadVector(v);
                        bins.get(v).setBinLoadVector(binLoadVector);
                    } else {
                        v++;
                    }
                }
            }

            // calculate best cycle solution matrix
            bestCycleSolution = calculateBestCycleSolution(x, bestCycleSolution);

            if (q == 0 || isGlobalBest(bestCycleSolution)) {
                globalBestSolution = bestCycleSolution.clone();
            }

            for (int item = 0; item < items.size(); item++) {
                for (int b = 0; b < bins.size(); b++) {
                    if (x[item][b] == 0) {
                        deltaTauBest[item][b] = 0;
                    } else {
                        deltaTauBest[item][b] = 1.0 / bestNumberOfUsedBins;
                    }
                }
            }
            pheromones = adjustPheromones();

        }

        for (int col = 0; col < bins.size(); col++) {
            binLoadVector = new int[Resource.values().length - 1];
            for (i = 0; i < binLoadVector.length; i++) {
                binLoadVector[i] = 0;
            }
            for (int row = 0; row < items.size(); row++) {
                if (globalBestSolution[row][col] != 0) {
                    for (i = 0; i < binLoadVector.length; i++) {
                        binLoadVector[i] += items.get(row).getResourceDemand()[i];
                    }
                }
            }
            bins.get(col).setBinLoadVector(binLoadVector);
            if (underThreshold(bins.get(col))) {
                bins.get(col).setMigrateTrigger(true);
            }
        }

        determineAvailableResources();
        boolean flag;
        for (i = 0; i < items.size(); i++) {
            flag = false;
            for (int b = 0; b < bins.size(); b++) {

                if (x[items.size()][b] == 0) {
                    bins.get(b).turnOff();
                }
                if (globalBestSolution[i][b] != 0) {
                    flag = true;
                }
            }
            if (!flag && (!Base.overflowItems.contains(items.get(i)))) {
                Base.overflowItems.add(items.get(i));
                System.out.println("Overflowed item "
                        + i
                        + " added to queue: "
                        + items.get(i).getResourceDemand()[Resource.CPU
                        .getIndex()]);
            }
        }

    }

    private void determineAvailableResources() {
        availableResources = new int[Resource.values().length - 1];
        for (Bin bin : bins) {
            availableResources[Resource.CPU.getIndex()] += (bin
                    .getResourceCapacity()[Resource.CPU.getIndex()] - bin
                    .getBinLoadVector()[Resource.CPU.getIndex()]);
            availableResources[Resource.NETWORK_TRANSFER_SPEED.getIndex()] += (bin
                    .getResourceCapacity()[Resource.NETWORK_TRANSFER_SPEED
                    .getIndex()] - bin.getBinLoadVector()[Resource.NETWORK_TRANSFER_SPEED
                    .getIndex()]);
            availableResources[Resource.RAM.getIndex()] += (bin
                    .getResourceCapacity()[Resource.RAM.getIndex()] - bin
                    .getBinLoadVector()[Resource.RAM.getIndex()]);
            availableResources[Resource.STORAGE.getIndex()] += (bin
                    .getResourceCapacity()[Resource.STORAGE.getIndex()] - bin
                    .getBinLoadVector()[Resource.STORAGE.getIndex()]);
        }

    }

    private double[][] adjustPheromones() {
        tauMax = Pheromones.calculateTauMax(bestNumberOfUsedBins,
                PHEROMONE_EVAPORATION);
        tauMin = Pheromones.calculateTauMin(tauMax, G_CONST);
        for (int i = 0; i < items.size(); i++) {
            for (int b = 0; b < bins.size(); b++) {
                pheromones[i][b] = (1 - PHEROMONE_EVAPORATION)
                        * pheromones[i][b] + deltaTauBest[i][b];
                if (pheromones[i][b] > tauMax) {
                    tauMax = pheromones[i][b];
                }
                if (pheromones[i][b] < tauMin) {
                    tauMin = pheromones[i][b];
                }
            }
        }
        return pheromones;

    }

    private int[][] calculateBestCycleSolution(int[][] antSolution,
            int[][] bestCycleSolution) {
        int total1 = 0;
        int total2 = 0;
        for (int i = 0; i < bins.size(); i++) {
            total1 += antSolution[items.size()][i];
            total2 += bestCycleSolution[items.size()][i];
        }
        if (total1 <= total2 || total2 == 0) {
            bestCycleSolution = antSolution.clone();
            bestNumberOfUsedBins = total1;
        }
//                else if (total1 == total2) {
//			bestCycleSolution = antSolution.clone();
//		}

        return bestCycleSolution;
    }

    private boolean isGlobalBest(int[][] bestCycleSolution) {
        int[][] temp = calculateBestCycleSolution(globalBestSolution,
                bestCycleSolution);
        for (int i = 0; i < items.size(); i++) {
            for (int j = 0; j < bins.size(); j++) {
                if (globalBestSolution[i][j] != temp[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private int chooseItemWithLargestProbability(double sum, int binIndex,
            List<Item> setOfQualifiedItems) {
        int i;
        double max = 0.0;
        int maxIndex = 0;
        for (Item item : setOfQualifiedItems) {
            i = items.indexOf(item);
            probabilities[i][binIndex] = (Math.pow(pheromones[i][binIndex],
                    ALPHA) * Math.pow(niu[i][binIndex], BETA)) / sum;
            if (max < probabilities[i][binIndex]) {
                max = probabilities[i][binIndex];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private List<Item> determineSetOfQualifiedItems(int binIndex,
            List<Item> copyOfItems) {
        List<Item> result = new ArrayList<Item>();
        int i;
        boolean assigned;
        Bin bin = bins.get(binIndex);

        for (Item item : copyOfItems) {
            if (!result.contains(item) && checkCapacityConstraint(bin, item)) {
                i = items.indexOf(item);

                assigned = false;
                for (int b = 0; b < bins.size() && !assigned; b++) {
                    if (x[i][b] == 1) {
                        assigned = true;
                    }
                }

                if (!assigned) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    public int[] computeBinLoadVector(int binIndex) {
        Bin bin = bins.get(binIndex);
        int l = bin.getResourceCapacity().length;
        int[] vector = new int[l];
        int[] demandVector = new int[l];

        for (int i = 0; i < nbOfItems; i++) {
            if (x[i][binIndex] == 1) {
                demandVector = items.get(i).getValueSet();
                for (int j = 0; j < l; j++) {
                    vector[j] += demandVector[j];
                }
            }
        }
        return vector;
    }

    public boolean checkCapacityConstraint(Bin bin, Item item) {
        int[] capacityVector = bin.getValueSet();
        int[] binLoadVector = bin.getBinLoadVector();
        int[] demandVector = item.getValueSet();
        int j = 0;
        int l = capacityVector.length;

        for (j = 0; j < l; j++) {
            if (binLoadVector[j] + demandVector[j] > capacityVector[j]) {
                return false;
            }
        }
        return true;
    }

    public List<Bin> initalizeBinsData(int[] resourceCapacity) {
        Bin bin;
        for (int i = 0; i < NB_OF_BINS; i++) {
            bin = new Bin();
            bin.setId(i);
            bin.setResourceCapacity(resourceCapacity);
            bins.add(bin);
        }
        return bins;
    }

    public boolean underThreshold(Bin bin) {
        boolean belowThreshold = true;
        int[] resourceCapacity = bin.getResourceCapacity();
        int[] binLoad = bin.getBinLoadVector();
        for (int r = 0; r < Resource.values().length - 1 && belowThreshold; r++) {
            if (binLoad[r] > resourceCapacity[r] * (1 - THRESHOLD)
                    || (binLoad[r] == 0 && r != Resource.values().length - 1)) {
                belowThreshold = false;
            }
        }
        return belowThreshold;
    }

    public List<Bin> getBins() {
        return bins;
    }

    public void setBins(List<Bin> bins) {
        this.bins = bins;
    }

    public double[][] getPheromones() {
        return pheromones;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    /**
     * @return the globalBestSolution
     */
    public int[][] getGlobalBestSolution() {
        return globalBestSolution;
    }

    /**
     * @param globalBestSolution the globalBestSolution to set
     */
    public void setGlobalBestSolution(int[][] globalBestSolution) {
        this.globalBestSolution = globalBestSolution;
    }

    /**
     * @return the availableResources
     */
    public int[] getAvailableResources() {
        return availableResources;
    }

    /**
     * @param availableResources the availableResources to set
     */
    public void setAvailableResources(int[] availableResources) {
        this.availableResources = availableResources;
    }

    /**
     * @return the nB_OF_ITEMS
     */
    public int getNbOfItems() {
        return nbOfItems;
    }

    /**
     * @param nB_OF_ITEMS the nB_OF_ITEMS to set
     */
    public void setNbOfItems(int nbOfItems) {
        this.nbOfItems = nbOfItems;
    }

    /**
     * @return the leftoverItems
     */
    public List<Item> getLeftoverItems() {
        return leftoverItems;
    }

    /**
     * @param leftoverItems the leftoverItems to set
     */
    public void setLeftoverItems(List<Item> leftoverItems) {
        this.leftoverItems = leftoverItems;
    }

    public List<Item> getItemsToMigrate() {
        return itemsToMigrate;
    }

    public void setItemsToMigrate(List<Item> itemsToMigrate) {
        this.itemsToMigrate = itemsToMigrate;
    }

    /**
     * @return the adjustedBins
     */
    public List<Bin> getAdjustedBins() {
        return adjustedBins;
    }

    /**
     * @param adjustedBins the adjustedBins to set
     */
    public void setAdjustedBins(List<Bin> adjustedBins) {
        this.adjustedBins = adjustedBins;
    }
}
