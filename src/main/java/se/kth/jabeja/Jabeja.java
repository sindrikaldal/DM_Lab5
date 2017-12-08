package se.kth.jabeja;

import se.kth.jabeja.config.Config;
import se.kth.jabeja.config.NodeSelectionPolicy;
import se.kth.jabeja.io.FileIO;
import se.kth.jabeja.rand.RandNoGenerator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Jabeja {
  final static Logger logger = Logger.getLogger(Jabeja.class);
  private final Config config;
  private final HashMap<Integer/*id*/, se.kth.jabeja.Node/*neighbors*/> entireGraph;
  private final List<Integer> nodeIds;
  private int numberOfSwaps;
  private int round;
  private float T;
  private boolean resultFileCreated = false;

  //-------------------------------------------------------------------
  public Jabeja(HashMap<Integer, se.kth.jabeja.Node> graph, Config config) {
    this.entireGraph = graph;
    this.nodeIds = new ArrayList(entireGraph.keySet());
    this.round = 0;
    this.numberOfSwaps = 0;
    this.config = config;
    this.T = config.getTemperature();
  } 


  //-------------------------------------------------------------------
  public void startJabeja() throws IOException {
    for (round = 0; round < config.getRounds(); round++) {
      for (int id : entireGraph.keySet()) {
        sampleAndSwap(id);
      }

      //one cycle for all nodes have completed.
      //reduce the temperature
      report();
    }
  }

  private double acceptance_probability(double oldCost, double newCost, Node node) {
    return oldCost == newCost ? 0 : Math.exp((newCost - oldCost) / node.getTemp());
  }

  private double cost(Node node, Node partner) {
    return Math.pow(getDegree(node, partner.getColor()), config.getAlpha())
            + Math.pow(getDegree(partner, node.getColor()), config.getAlpha());
  }

  /**
   * Simulated analealing cooling function
   */
  private void saCoolDown(Node node){
    // TODO for second task
   /*if (T > 1)
      T -= config.getDelta();
    if (T < 1)
      T = 1;*/
    Double temp = node.getTemp();
    temp *= 0.9;
    if (temp < 0.01) {
      node.setTemp(0.01);
    } else {
      node.setTemp(temp);
    }
  }

  /**
   * Sample and swap algorith at node p
   * @param nodeId
   */
  private void sampleAndSwap(int nodeId) {
    Node partner = null;
    Node nodep = entireGraph.get(nodeId);
    Node temp_partner = null;

    if (config.getNodeSelectionPolicy() == NodeSelectionPolicy.HYBRID
            || config.getNodeSelectionPolicy() == NodeSelectionPolicy.LOCAL) {
      temp_partner = findPartner(nodeId, getNeighbors(nodep));
    }

  /*if (config.getNodeSelectionPolicy() == NodeSelectionPolicy.HYBRID
          || config.getNodeSelectionPolicy() == NodeSelectionPolicy.RANDOM) {*/
    // if local policy fails then randomly sample the entire graph



    for (int i = 0; i < 100; i++) {
      Node random_partner = findPartner(nodeId, getSample(nodeId));

      if (temp_partner == null && random_partner != null) {
        partner = random_partner;
        temp_partner = random_partner;
      }
      else if (random_partner != null){
        partner = temp_partner;
        if (random_partner.getBenefit() > partner.getBenefit()) {
          partner = random_partner;
        }
      }
    }

    // }

    saCoolDown(nodep);


    // swap the colors
    if (partner != null && partner.getColor() != nodep.getColor()) {
        int nodeColor = nodep.getColor();
        int partnerColor = partner.getColor();
        nodep.setColor(partnerColor);
        partner.setColor(nodeColor);
        this.numberOfSwaps++;
    }
  }

  public Node findPartner(int nodeId, Integer[] nodes){
    Node nodep = entireGraph.get(nodeId);

    Node bestPartner = null;
    double highestBenefit = 0;


    for(int partnerId : nodes) {
      Node potentialPartner = entireGraph.get(partnerId);
      double oldDegree =  Math.pow(getDegree(potentialPartner, potentialPartner.getColor()), config.getAlpha())
              + Math.pow(getDegree(nodep, nodep.getColor()), config.getAlpha());
      double newDegree = Math.pow(getDegree(nodep, potentialPartner.getColor()), config.getAlpha())
                        + Math.pow(getDegree(potentialPartner, nodep.getColor()), config.getAlpha());

      double acceptance_probability = acceptance_probability(oldDegree, newDegree, nodep);

      if (1 > acceptance_probability && acceptance_probability > 0) {
        System.out.println(acceptance_probability);
      } else {
        System.out.println("SINNDREEEEEHH");
      }

      if(acceptance_probability > Math.random() && newDegree > highestBenefit) {
        highestBenefit = newDegree;
        bestPartner = potentialPartner;
        bestPartner.setBenefit(highestBenefit);
      }
    }

    return bestPartner;
  }

  /**
   * The the degreee on the node based on color
   * @param node
   * @param colorId
   * @return how many neighbors of the node have color == colorId
   */
  private int getDegree(Node node, int colorId){
    int degree = 0;
    for(int neighborId : node.getNeighbours()){
      Node neighbor = entireGraph.get(neighborId);
      if(neighbor.getColor() == colorId){
        degree++;
      }
    }
    return degree;
  }

  /**
   * Returns a uniformly random sample of the graph
   * @param currentNodeId
   * @return Returns a uniformly random sample of the graph
   */
  private Integer[] getSample(int currentNodeId) {
    int count = config.getUniformRandomSampleSize();
    int rndId;
    int size = entireGraph.size();
    ArrayList<Integer> rndIds = new ArrayList<Integer>();

    while (true) {
      rndId = nodeIds.get(RandNoGenerator.nextInt(size));
      if (rndId != currentNodeId && !rndIds.contains(rndId)) {
        rndIds.add(rndId);
        count--;
      }

      if (count == 0)
        break;
    }

    Integer[] ids = new Integer[rndIds.size()];
    return rndIds.toArray(ids);
  }

  /**
   * Get random neighbors. The number of random neighbors is controlled using
   * -closeByNeighbors command line argument which can be obtained from the config
   * using {@link Config#getRandomNeighborSampleSize()}
   * @param node
   * @return
   */
  private Integer[] getNeighbors(Node node) {
    ArrayList<Integer> list = node.getNeighbours();
    int count = config.getRandomNeighborSampleSize();
    int rndId;
    int index;
    int size = list.size();
    ArrayList<Integer> rndIds = new ArrayList<Integer>();

    if (size <= count)
      rndIds.addAll(list);
    else {
      while (true) {
        index = RandNoGenerator.nextInt(size);
        rndId = list.get(index);
        if (!rndIds.contains(rndId)) {
          rndIds.add(rndId);
          count--;
        }

        if (count == 0)
          break;
      }
    }

    Integer[] arr = new Integer[rndIds.size()];
    return rndIds.toArray(arr);
  }


  /**
   * Generate a report which is stored in a file in the output dir.
   *
   * @throws IOException
   */
  private void report() throws IOException {
    int grayLinks = 0;
    int migrations = 0; // number of nodes that have changed the initial color
    int size = entireGraph.size();

    for (int i : entireGraph.keySet()) {
      Node node = entireGraph.get(i);
      int nodeColor = node.getColor();
      ArrayList<Integer> nodeNeighbours = node.getNeighbours();

      if (nodeColor != node.getInitColor()) {
        migrations++;
      }

      if (nodeNeighbours != null) {
        for (int n : nodeNeighbours) {
          Node p = entireGraph.get(n);
          int pColor = p.getColor();

          if (nodeColor != pColor)
            grayLinks++;
        }
      }
    }

    int edgeCut = grayLinks / 2;

    logger.info("round: " + round +
            ", edge cut:" + edgeCut +
            ", swaps: " + numberOfSwaps +
            ", migrations: " + migrations);

    saveToFile(edgeCut, migrations);
  }

  private void saveToFile(int edgeCuts, int migrations) throws IOException {
    String delimiter = "\t\t";
    String outputFilePath;

    //output file name
    File inputFile = new File(config.getGraphFilePath());
    outputFilePath = config.getOutputDir() +
            File.separator +
            inputFile.getName() + "_plot.txt";/* +
            "NS" + "_" + config.getNodeSelectionPolicy() + "_" +
            "GICP" + "_" + config.getGraphInitialColorPolicy() + "_" +
            "T" + "_" + config.getTemperature() + "_" +
            "D" + "_" + config.getDelta() + "_" +
            "RNSS" + "_" + config.getRandomNeighborSampleSize() + "_" +
            "URSS" + "_" + config.getUniformRandomSampleSize() + "_" +
            "A" + "_" + config.getAlpha() + "_" +
            "R" + "_" + config.getRounds() + ".txt";*/

    if (!resultFileCreated) {
      File outputDir = new File(config.getOutputDir());
      if (!outputDir.exists()) {
        if (!outputDir.mkdir()) {
          throw new IOException("Unable to create the output directory");
        }
      }
      // create folder and result file with header
      String header = "# Migration is number of nodes that have changed color.";
      header += "\n\nRound" + delimiter + "Edge-Cut" + delimiter + "Swaps" + delimiter + "Migrations" + delimiter + "Skipped" + "\n";
      FileIO.write(header, outputFilePath);
      resultFileCreated = true;
    }

    FileIO.append(round + delimiter + (edgeCuts) + delimiter + numberOfSwaps + delimiter + migrations + "\n", outputFilePath);
  }
}
