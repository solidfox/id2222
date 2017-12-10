package se.kth.jabeja;

import org.apache.log4j.Logger;
import se.kth.jabeja.annealing.AnnealingStrategy;
import se.kth.jabeja.annealing.NoAnnealing;
import se.kth.jabeja.config.Config;
import se.kth.jabeja.config.NodeSelectionPolicy;
import se.kth.jabeja.io.FileIO;
import se.kth.jabeja.rand.RandNoGenerator;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

public class Jabeja {
  final static Logger logger = Logger.getLogger(Jabeja.class);
  private final Config config;
  private final HashMap<Integer/*id*/, Node/*neighbors*/> entireGraph;
  private final List<Integer> nodeIds;
  private int numberOfSwaps;
  private int convergedAtRound = 0;
  private long startTime;
  private int secondsToConvergence;
  private int minimalEdgeCut = Integer.MAX_VALUE;
  private Integer _edgeCut;
  private int round;
  private Queue<Integer> recentSwaps = new LinkedList<>();
  private AnnealingStrategy annealingMethod;
  private boolean resultFileCreated = false;

  //---------------------------------------------------Number of nodes that have changed the initial color----------------
  public Jabeja(HashMap<Integer, Node> graph, Config config) {
    this.entireGraph = graph;
    this.nodeIds = new ArrayList(entireGraph.keySet());
    this.round = 0;
    this.numberOfSwaps = 0;
    this.config = config;
    this.annealingMethod = config.getAnnealingMethod()
            .getStrategy(config.getTemperature(), config.getAnnealingSpeed());
  }


  //-------------------------------------------------------------------
  public void startJabeja() throws IOException {
    startTime = System.currentTimeMillis()/1000;
    for (round = 0; round < config.getRounds() + config.getCooldownRounds(); round++) {
      _edgeCut = null;
      int nSwaps0 = numberOfSwaps;
      for (int id : entireGraph.keySet()) {
        sampleAndSwap(id);
      }
      int nSwapsInRound = numberOfSwaps - nSwaps0;
      recentSwaps.add(nSwapsInRound);
      while (recentSwaps.size() > 5) {
        recentSwaps.remove();
      }
      Double averageRecentSwaps = recentSwaps.stream().collect(Collectors.averagingInt(Integer::intValue));
      if (averageRecentSwaps > 1) {
        convergedAtRound = round;
        secondsToConvergence = (int) (System.currentTimeMillis()/1000 - startTime);
      }
      //one cycle for all nodes have completed.
      //reduce the temperature
      minimalEdgeCut = Math.min(minimalEdgeCut, edgeCut());
      saCoolDown();
      report();
      if (round == config.getRounds()-1) {
        annealingMethod = new NoAnnealing();
      }
    }
    appendSummaryToCSVReport();
  }

  /**
   * Simulated analealing cooling function
   */
  private void saCoolDown(){
    // TODO for second task
    annealingMethod.anneal();
  }

  /**
   * Sample and swap algorith at node p
   * @param nodeId
   */
  private void sampleAndSwap(int nodeId) {
    Optional<Node> partner = Optional.empty();
    Node nodep = entireGraph.get(nodeId);

    if (config.getNodeSelectionPolicy() == NodeSelectionPolicy.HYBRID
            || config.getNodeSelectionPolicy() == NodeSelectionPolicy.LOCAL) {
      // swap with random neighbors
      Integer[] neighbors = getNeighbors(nodep);
      partner = findPartner(nodeId, neighbors);
    }

    if (config.getNodeSelectionPolicy() == NodeSelectionPolicy.HYBRID
            || config.getNodeSelectionPolicy() == NodeSelectionPolicy.RANDOM) {
      // if local policy fails then randomly sample the entire graph
      Integer[] sample = getSample(nodeId);
      partner = findPartner(nodeId, sample);
    }

    // swap the colors
    if (partner.isPresent()) {
      int partnerColor = partner.get().getColor();
      partner.get().setColor(nodep.getColor());
      nodep.setColor(partnerColor);
      numberOfSwaps++;
    }
  }

  public Optional<Node> findPartner(int nodeId, Integer[] nodes){

    Node nodep = entireGraph.get(nodeId);

    Optional<Node> bestPartner = Optional.empty();
    double highestBenefit = 0;

    for (int i = 0; i < nodes.length; i++) {
      Node partnerNode = entireGraph.get(nodes[i]);
      double possibleBenefit = (double) (nodep.getDegree() + partnerNode.getDegree());
      double currentBenefit = (getBenefit(nodep) + getBenefit(partnerNode)) / possibleBenefit;
      double swappedBenefit = (getBenefit(nodep, partnerNode.getColor()) + getBenefit(partnerNode, nodep.getColor())) / possibleBenefit;
      if (swappedBenefit > highestBenefit && annealingMethod.accept(currentBenefit, swappedBenefit)) {
        bestPartner = Optional.of(partnerNode);
        highestBenefit = swappedBenefit;
      }
    }

    return bestPartner;
  }

  /**
   * The the benefit on the node based on color
   * @param node
   * @param colorId
   * @return how many neighbors of the node have color == colorId
   */
  private int getBenefit(Node node, int colorId){
    int sameColorNeighbors = 0;
    for(int neighborId : node.getNeighbours()){
      Node neighbor = entireGraph.get(neighborId);
      if(neighbor.getColor() == colorId){
        sameColorNeighbors++;
      }
    }
    return sameColorNeighbors;
  }

  /**
   * The benefit based on node's own color.
   * @param node
   * @return how many neighbors of node have same color as node.
   */
  private int getBenefit(Node node) {
    return getBenefit(node, node.getColor());
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
    ArrayList<Integer> rndIds = new ArrayList<>();

    while (count > 0) {
      rndId = nodeIds.get(RandNoGenerator.nextInt(size));
      if (rndId != currentNodeId && !rndIds.contains(rndId)) {
        rndIds.add(rndId);
        count--;
      }
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
    int migrations = migrations();

    int edgeCut = edgeCut();

    logger.info("round: " + round +
            ", edge cut:" + edgeCut +
            ", swaps: " + numberOfSwaps +
            ", migrations: " + migrations);

    saveToFile(edgeCut, migrations);
  }

  /**
   * @return Number of nodes that have changed the initial color.
   */
  private int migrations() {
    int migrations = 0;

    for (int i : entireGraph.keySet()) {
      Node node = entireGraph.get(i);
      int nodeColor = node.getColor();

      if (nodeColor != node.getInitColor()) {
        migrations++;
      }
    }
    return migrations;
  }

  private int edgeCut() {
    if (_edgeCut == null) {
      int grayLinks = 0;

      for (int i : entireGraph.keySet()) {
        Node node = entireGraph.get(i);
        int nodeColor = node.getColor();
        ArrayList<Integer> nodeNeighbours = node.getNeighbours();

        if (nodeNeighbours != null) {
          for (int n : nodeNeighbours) {
            Node p = entireGraph.get(n);
            int pColor = p.getColor();

            if (nodeColor != pColor)
              grayLinks++;
          }
        }
      }

      _edgeCut = grayLinks / 2;
    }
    return _edgeCut;
  }

  private void saveToFile(int edgeCuts, int migrations) throws IOException {
    String delimiter = "\t\t";
    String outputFilePath;

    //output file name
    File inputFile = new File(config.getGraphFilePath());
    outputFilePath = config.getOutputDir() +
            File.separator +
            inputFile.getName() + "_" +
            "ANNEAL" + "_" + config.getAnnealingMethod() + "_" +
            "NORMALIZED" + "_" +
            "NS" + "_" + config.getNodeSelectionPolicy() + "_" +
            "GICP" + "_" + config.getGraphInitialColorPolicy() + "_" +
            "temperature" + "_" + config.getTemperature() + "_" +
            "D" + "_" + config.getAnnealingSpeed() + "_" +
            "RNSS" + "_" + config.getRandomNeighborSampleSize() + "_" +
            "URSS" + "_" + config.getUniformRandomSampleSize() + "_" +
            "A" + "_" + config.getAlpha() + "_" +
            "R" + "_" + config.getRounds() + ".txt";

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

  private void appendSummaryToCSVReport() throws IOException {
    File inputFile = new File(config.getGraphFilePath());
    File outputDir = new File(config.getOutputDir());
    if (!outputDir.exists()) {
      if (!outputDir.mkdir()) {
        throw new IOException("Unable to create the output directory");
      }
    }
    String outputFilePath = outputDir.toURI().getPath() + File.separator + "report.csv";
    File outputFile = new File(outputFilePath);
    String delimiter = ",";
    if (outputFile.createNewFile()) {
      // File didn't exist
      List<String> headers = Arrays.asList(
              "Graph",
              "Annealing strategy",
              "Initial temperature",
              "Annealing speed",
              "Modification",
              "Total swaps",
              "Rounds to converge",
              "Seconds to converge",
              "Minimum edge cut"
      );
      String header = headers.stream().collect(Collectors.joining(delimiter)) + "\n";
      FileIO.write(header, outputFilePath);
    }
    DecimalFormatSymbols.getInstance().setDecimalSeparator('.');
    DecimalFormat decimalFormat = new DecimalFormat("0");
    decimalFormat.setMaximumFractionDigits(5);
    List<String> data = Arrays.asList(
            inputFile.getName(),
            config.getAnnealingMethod().toString(),
            decimalFormat.format(config.getTemperature()),
            decimalFormat.format(config.getAnnealingSpeed()),
            "Original",
            Integer.toString(numberOfSwaps),
            Integer.toString(convergedAtRound),
            Integer.toString(secondsToConvergence),
            Integer.toString(minimalEdgeCut)
    );
    FileIO.append(data.stream().collect(Collectors.joining(delimiter)) + "\n", outputFilePath);
  }
}
