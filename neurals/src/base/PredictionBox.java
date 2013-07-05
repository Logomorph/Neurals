package base;

import neuralnet.Layer;
import neuralnet.Network;
import nn_data.DataSet;
import nn_data.DataSetRow;
import nn_learning.Learner;
import nn_patterns.PatternGenerator;
import nn_patterns.SineWave;
import nn_transfer.Sin;
import util.GraphCSVWriter;
import aco.entities.Item;
import aco.entities.Resource;
import dclink_if.VMMonitor;

/*
 * Contains all the neural networks needed for predicting data for one VM
 */
public class PredictionBox {

    Item vm;
    // this stuff will actually come from open nebula in real time
    double[] CPU_data;
    double[] RAM_data;
    double[] STORAGE_data;
    double[] BANDWIDTH_data;
    double[] RUN_TIME_data;
    int index;
    boolean hasData;
    PatternGenerator pg;
    // stuff for prediction
    Network cpuNet, ramNet, storeNet, bwNet, runTimeNet;
    GraphCSVWriter graphCSV;
    private VMMonitor vmm;

    public PredictionBox(Item item, 
            GraphCSVWriter graphCSV, VMMonitor vmm) {
        this.vm = item;
        populateData();
        createNeuralNets();
        hasData = true;
        this.graphCSV = graphCSV;
        this.vmm = vmm;
    }

    // Update all the neural networks and the Item
    public void Update() {
        int[] resourceDemand = new int[Resource.values().length];

        // CPU NN
        double[] inMips = {CPU_data[index - 2], CPU_data[index - 1],
            CPU_data[index]};
        cpuNet.setInput(inMips);
        cpuNet.process();

        resourceDemand[Resource.CPU.getIndex()] = (int) (cpuNet.getOutput()[0] * Item.CPU_MAX);

        // RAM NN
        double[] inRam = {RAM_data[index - 2], RAM_data[index - 1],
            RAM_data[index]};
        ramNet.setInput(inRam);
        ramNet.process();

        resourceDemand[Resource.RAM.getIndex()] = (int) (ramNet.getOutput()[0] * Item.RAM_MAX);

        // STORAGE NN
        double[] inStorage = {STORAGE_data[index - 2],
            STORAGE_data[index - 1], STORAGE_data[index]};
        storeNet.setInput(inStorage);
        storeNet.process();

        resourceDemand[Resource.STORAGE.getIndex()] = (int) (storeNet
                .getOutput()[0] * Item.STORAGE_MAX);

        // BANDWIDTH NN
        double[] inBw = {BANDWIDTH_data[index - 2], BANDWIDTH_data[index - 1],
            BANDWIDTH_data[index]};
        bwNet.setInput(inBw);
        bwNet.process();

        resourceDemand[Resource.NETWORK_TRANSFER_SPEED.getIndex()] = (int) (bwNet
                .getOutput()[0] * Item.BANDWIDTH_MAX);

        // RUN_TIME NN
        /*double[] inRunTime = {RUN_TIME_data[index - 2],
            RUN_TIME_data[index - 1], RUN_TIME_data[index]};
        runTimeNet.setInput(inRunTime);
        runTimeNet.process();

        resourceDemand[Resource.RUN_TIME.getIndex()] = (int) (runTimeNet
                .getOutput()[0] * Item.RUN_TIME_MAX);*/
        resourceDemand[Resource.RUN_TIME.getIndex()] = Item.RUN_TIME_MAX;

        this.vm.setResourceDemand(resourceDemand);

        if (graphCSV != null) {
            graphCSV.addLine(vm.getIdentifier(),
                    resourceDemand[Resource.CPU.getIndex()],
                    resourceDemand[Resource.RAM.getIndex()],
                    resourceDemand[Resource.STORAGE.getIndex()],
                    resourceDemand[Resource.NETWORK_TRANSFER_SPEED.getIndex()],
                    resourceDemand[Resource.RUN_TIME.getIndex()]);
        }

        if (index < CPU_data.length) {
            hasData = true;
            index++;
        } else {
            hasData = false;
        }
    }

    public Item getItem() {
        return this.vm;
    }

    public boolean hasData() {
        return this.hasData;
    }

    // all this is temporary
    private void populateData() {
        SineWave swg = new SineWave();
        CPU_data = swg.generatePattern(1000, 0);
        RAM_data = swg.generatePattern(1000, 0);
        STORAGE_data = swg.generatePattern(1000, 0);
        BANDWIDTH_data = swg.generatePattern(1000, 0);
        RUN_TIME_data = swg.generatePattern(1000, 0);
        index = 2;
    }

    private void createNeuralNets() {
        cpuNet = createAndTrainNetwork(CPU_data);
        ramNet = createAndTrainNetwork(RAM_data);
        storeNet = createAndTrainNetwork(STORAGE_data);
        bwNet = createAndTrainNetwork(BANDWIDTH_data);
        runTimeNet = createAndTrainNetwork(RUN_TIME_data);
    }

    private Network createAndTrainNetwork(double[] data) {
        // create the network
        Layer in = new Layer(3, new Sin(), false);
        Layer hidden = new Layer(2, new Sin(), false);
        Layer out = new Layer(1, new Sin(), false);

        in.connectLayers(hidden);
        hidden.connectLayers(out);

        Network net = new Network();
        net.addLayer(in);
        net.addLayer(hidden);
        net.addLayer(out);
        net.setInputNeurons(in.neurons);
        net.setOutputNeurons(out.neurons);

        // train the network with sine data set
        //System.out.println("Started training");
        DataSet ds = createSineWaveDataSet(data);
        Learner l = new Learner(net, 0.8);

        for (int i = 0; i < 50; i++) {
            l.trainNetwork(ds);
        }
        //System.out.println("Done training");

        return net;
    }

    private static DataSet createSineWaveDataSet(double[] data) {
        DataSet ds = new DataSet();

        for (int i = 0; i < data.length - 4; i++) {
            DataSetRow dsr = new DataSetRow();
            double[] inp = new double[3];
            inp[0] = data[i] >= 0 ? data[i] : 0;
            inp[1] = data[i + 1] >= 0 ? data[i + 1] : 0;
            inp[2] = data[i + 2] >= 0 ? data[i + 2] : 0;
            double[] outp = new double[1];
            outp[0] = data[i + 3] >= 0 ? data[i + 3] : 0;

            dsr.inputData = inp;
            dsr.outputData = outp;
            ds.addRow(dsr);
        }
        return ds;
    }

    public VMMonitor getVmm() {
        return vmm;
    }
}
